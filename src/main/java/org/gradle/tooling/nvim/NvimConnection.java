package org.gradle.tooling.nvim;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ensarsarajcic.neovim.java.corerpc.client.RpcClient;
import com.ensarsarajcic.neovim.java.corerpc.message.RequestMessage;
import com.ensarsarajcic.neovim.java.handler.NeovimHandlerManager;
import com.ensarsarajcic.neovim.java.handler.annotations.NeovimRequestHandler;
import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.model.GradleTask;

/** A connection to the controlling Neovim instance.
 * <p>
 * The process needs to be able to receive and send messages from and to
 * Neovim; this class implements the communication layer between the two.
 * Communication between Neovim and Gradle happens through this class.
 * <p>
 * The main purpose of this class is to handle requests and notifications from
 * Neovim by passing them on to the appropriate Gradle methods.
 */
public class NvimConnection {
	/** Connection to a Gradle instance; will have access to this Neovim
	 * connection.
	 */
	private QueryServer queryServer;

	/** RPC client implementation which the JVM process can use for
	 * communication with Neovim.
	 * <p>
	 * This is the part which will be exposed to the outside.
	 */
	private RpcClient rpcClient;

	private NvimConnection() {
		this.rpcClient = RpcClient.getDefaultAsyncInstance();
		final var neovimHandlerManager = new NeovimHandlerManager();
		queryServer = new QueryServer(this);

		neovimHandlerManager.registerNeovimHandler(this);
		neovimHandlerManager.attachToStream(rpcClient);
	}

	/** Establishes a new connection to Neovim from the current process.
	 *
	 * @param rpcConnection  The connection object which is used for
	 * communication with Neovim.
	 *
	 * @return The RPC client object.
	 */
	public static RpcClient establish() {
		final var nvimConnection = new NvimConnection();
		return nvimConnection.rpcClient;
	}

	/** Return a list of task specifications to Neovim.
	 * <p>
	 * A task specification is a list of strings; the order of strings must be
	 * the same across all specifications. The details are yet to be decided
	 * upon.
	 *
	 * @param request The request object, is ignores.
	 * @return A list of task specifications.
	 */
	@NeovimRequestHandler("get-tasks")
	public List<List<String>> getTasks(RequestMessage request) throws NeovimRequestException {
		final Function<GradleTask, List<String>> taskToSpec = task -> {
			final var name = task.getName();
			final var desc = Optional.of(task).map(GradleTask::getDescription).orElse("");
			final var path = task.getPath();
			final var group = task.getGroup();
			return List.of(name, desc, path, group);
		};
		try {
			final var projectPath = (String) request.getArguments().get(0);
			return queryServer
				.getTasks(projectPath)
				.stream()
				.filter(GradleTask::isPublic)
				.map(taskToSpec)
				.collect(Collectors.toList());
		} catch (Exception e) {
			throw new NeovimRequestException(e.getMessage());
		}
	}

	@NeovimRequestHandler("run-task")
	public void runTask(RequestMessage request) throws NeovimRequestException {
		try {
			final var projectPath = (String) request.getArguments().get(0);
			final var taskName    = (String) request.getArguments().get(1);

			queryServer.runTask(projectPath, taskName);
		} catch (Exception e) {
			throw new NeovimRequestException(e.getMessage());
		}
	}

	/** Perform a handshake with Neovim to confirm that the connection is
	 * working.
	 * <p>
	 * A handshake request does not do anything useful, it only performs the
	 * bare minimum to confirm that the connection has been established and
	 * that requests are handled properly. Use it to verify that everything is
	 * connected properly first when diagnosing an issue.
	 *
	 * @param request The request object, will be ignored.
	 * @return The constant string "OK".
	 */
	@NeovimRequestHandler("handshake")
	public String performHandshake(RequestMessage request) throws NeovimRequestException {
		return "OK";
	}

	/** Intentionally throw an exception to Neovim.
	 * <p>
	 * Always throws an exception; useless in production, but good for ensuring
	 * that exceptions are indeed thrown.
	 *
	 * @param request The request object, will be ignored.
	 * @throws NeovimRequestException - Always thrown
	 * @return The constant string "OK".
	 */
	@NeovimRequestHandler("throw-up")
	public String throwUp(RequestMessage request) throws NeovimRequestException {
		final var message = "Exception intentionally thrown.";
		throw new NeovimRequestException(message);
	}
}
