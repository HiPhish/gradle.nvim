package org.gradle.tooling.nvim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.ensarsarajcic.neovim.java.corerpc.client.RpcClient;
import com.ensarsarajcic.neovim.java.corerpc.message.NotificationMessage;
import com.ensarsarajcic.neovim.java.corerpc.message.RequestMessage;
import com.ensarsarajcic.neovim.java.handler.NeovimHandlerManager;
import com.ensarsarajcic.neovim.java.handler.annotations.NeovimRequestHandler;
import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.nvim.handler.GetTasks;
import org.gradle.tooling.nvim.handler.Handshake;
import org.gradle.tooling.nvim.handler.NoHandlerRegistered;
import org.gradle.tooling.nvim.handler.NoOp;
import org.gradle.tooling.nvim.handler.RunTask;
import org.gradle.tooling.nvim.handler.ThrowUp;

/** A connection to the controlling Neovim instance.
 * <p>
 * The process needs to be able to receive and send messages from and to Neovim; this class
 * implements the communication layer between the two.  Communication between Neovim and Gradle
 * happens through this class.
 * <p>
 * The main purpose of this class is to handle requests and notifications from Neovim by passing
 * them on to the appropriate Gradle methods.
 */
public class NvimConnection {

	/** RPC client implementation which the JVM process can use for communication with Neovim.
	 * <p>
	 * This is the part which will be exposed to the outside.
	 */
	private RpcClient rpcClient;

	/** Projects currently beings managed by the server.
	 * <p>
	 * Maps the path of a project (as a {@code File} instance) to a project connection. When a new
	 * project needs to be queried add it to the map.  When a project is "closed" (whatever that
	 * might mean) we need to close the connection and remove the entry.
	 */
	private Map<File, ProjectConnection> projects = new HashMap<>();

	private Map<String, Class<? extends RequestHandler>> requestHandlers = Map.of(
		"no-op", NoOp.class,
		"get-tasks", GetTasks.class,
		"handshake", Handshake.class,
		"throw-up", ThrowUp.class,
		"run-task", RunTask.class
	);

	private NvimConnection() {
		rpcClient = RpcClient.getDefaultAsyncInstance();

		final var neovimHandlerManager = new NeovimHandlerManager();
		neovimHandlerManager.registerNeovimHandler(this);
		neovimHandlerManager.attachToStream(rpcClient);
	}

	/** Establishes a new connection to Neovim from the current process.
	 *
	 * @param rpcConnection  The connection object which is used for communication with Neovim.
	 *
	 * @return The RPC client object.
	 */
	public static RpcClient establish() {
		final var nvimConnection = new NvimConnection();
		return nvimConnection.rpcClient;
	}

	/** Handle an incoming Neovim request to Gradle by dispatching dynamically to a suitable request
	 * handler.
	 * <p>
	 * The request handlers must be registered at compile time. The first argument of the incoming
	 * message is the name of the request (a string), the remaining arguments are arguments to the
	 * particular request (all strings).
	 *
	 * @param message  The incoming Neovim message
	 * @return The result object from the handler, will be sent back to Neovim as the response.
	 */
	@NeovimRequestHandler("request")
	public Object handleRequest(RequestMessage message) throws NeovimRequestException {
		try {
			final var args = message.getArguments();
			final var request = (String) args.get(0);
			final var handler = requestHandlers.getOrDefault(request, NoHandlerRegistered.class);

			final var requestArgs =  args.subList(1, args.size()).toArray();
			final var handlerInstance = handler
				.getDeclaredConstructor(NvimConnection.class)
				.newInstance(this);

			return handlerInstance.handle(request, requestArgs);
		} catch (Exception e) {
			throw new NeovimRequestException(e.getMessage());
		}
	}

	/** Fetch the project connection object for a given project path.
	 * <p>
	 * If there is no connection to the path a new connection will be opened and stored for later
	 * use. If a connection had already been opened it is returned.
	 *
	 * @param projectPath Absolute path to the project as a string.
	 *
	 * @return An existing connection, or a new connection if none exists yet.
	 */
	public ProjectConnection fetchProjectConnection(String path) throws FileNotFoundException {
		Objects.requireNonNull(path, "Path string to project must not be null");

		final var project = new File(path);
		if (!project.exists()) {
			throw new FileNotFoundException(String.format("Project '%s' not found", path));
		}

		return projects.computeIfAbsent(project, this::connectToProject);
	}

	/** Send a notification to Neovim.
	 *
	 * @param method  Method of the notification message.
	 * @param args  Arguments of the notification.
	 */
	public void notify(String method, Object ... args) throws IOException {
		final var message = new NotificationMessage.Builder(method)
			.addArguments(new ArrayList<>(Arrays.asList(args)))
			.build();

		rpcClient.send(message);
	}

	/** Establish a connection to a project with the given file path.
	 * <p>
	 * This method tries to respect the user's personal settings.
	 *
	 * @param projectPath Absolute path to the project as a string.
	 *
	 * @return A new connection object.
	 */
	private ProjectConnection connectToProject(File project) {
		Objects.requireNonNull(project, "Project path must be non-null");

		final var connector = GradleConnector.newConnector()
			.forProjectDirectory(project);

		// Respect the user's custom environment variable, see
		// https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_environment_variables
		Optional.ofNullable(System.getenv("GRADLE_USER_HOME"))
			.map(File::new)
			.ifPresent(connector::useGradleUserHomeDir);

		return connector.connect();
	}
}
