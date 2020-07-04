package org.gradle.tooling.nvim;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

/** A query server which receives Neovim request (and notifications) and
 * returns query results.
 * <p>
 * The purpose of a query server is to maintain the state of the remote plugin
 * and react to messages from Neovim. It manages project connections and
 * communicates with Gradle. Using a query server allows us to isolate Gradle
 * access from the raw Neovim messages.
 */
public class QueryServer {
	/** Connection to Neovim, used for issuing RPC requests.
	 * <p>
	 * At times we might need further information from Neovim. We can use this
	 * object to communicate back to Neovim.
	 */
	private NvimConnection nvimConnection;

	/** Projects currently beings managed by the server.
	 * <p>
	 * Maps the path of a project (as a {@code File} instance) to a project
	 * connection. When a new project needs to be queried add it to the map.
	 * When a project is "closed" (whatever that might mean) we need to close
	 * the connection and remove the entry.
	 */
	private Map<File, ProjectConnection> projects = new HashMap<>();

	public QueryServer(NvimConnection nvimConnection) {
		this.nvimConnection = nvimConnection;
	}

	/** Return a list of Gradle tasks for a given project.
	 *
	 * @param projectPath Absolute path to the project as a string.
	 * @return All tasks defined for the project.
	 */
	public List<? extends GradleTask> getTasks(String projectPath) throws FileNotFoundException {
		return fetchProjectConnection(projectPath)
			.getModel(GradleProject.class)
			.getTasks()
			.getAll();
	}

	/** Run a given task in a given project.
	 *
	 * @param path Absolute path to the project as a string.
	 * @param task Name of the task to run.
	 */
	public void runTask(String path, String task) throws FileNotFoundException {
		final var buildLauncher = fetchProjectConnection(path)
			.newBuild()
			.forTasks(task);

		// TODO: get feedback at the task is running, display it in Neovim
		// Must investigate the more complex options which the API provides.
		buildLauncher.run();
	}

	/** Fetch the project connection object for a given project path.
	 * <p>
	 * If there is no connection to the path a new connection will be opened
	 * and stored for later use. If a connection had already been opened it is
	 * returned.
	 *
	 * @param projectPath Absolute path to the project as a string.
	 *
	 * @return An existing connection, or a new connection if none exists yet.
	 */
	private ProjectConnection fetchProjectConnection(String path) throws FileNotFoundException {
		Objects.requireNonNull(path, "Path string to project must not be null");

		final var project = new File(path);
		if (!project.exists()) {
			throw new FileNotFoundException(String.format("Project '%s' not found", path));
		}

		return projects.computeIfAbsent(project, this::connectToProject);
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
