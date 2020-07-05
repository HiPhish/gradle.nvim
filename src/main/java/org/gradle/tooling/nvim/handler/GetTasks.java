package org.gradle.tooling.nvim.handler;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

/** Return a list of task specifications to Neovim.
 * <p>
 * A task specification is a list of strings; the order of strings must be the same across all
 * specifications. The details are yet to be decided upon.
 */
public class GetTasks extends RequestHandler {
	public GetTasks(NvimConnection connection) {
		super(connection);
	}

	@Override
	public Object handle(String request, String ... args) throws NeovimRequestException {
		try {
			final var projectPath = (String) args[0];
			return getTasks(projectPath)
				.stream()
				.filter(GradleTask::isPublic)
				.map(this::taskToSpec)
				.collect(Collectors.toList());
		} catch (Exception e) {
			throw new NeovimRequestException(e.getMessage());
		}
	}

	/** Return a list of Gradle tasks for a given project.
	 *
	 * @param projectPath Absolute path to the project as a string.
	 * @return All tasks defined for the project.
	 */
	private List<? extends GradleTask> getTasks(String projectPath) throws FileNotFoundException {
		return connection.fetchProjectConnection(projectPath)
			.getModel(GradleProject.class)
			.getTasks()
			.getAll();
	}

	private List<String> taskToSpec(GradleTask task) {
		final var name = task.getName();
		final var desc = Optional.of(task).map(GradleTask::getDescription).orElse("");
		final var path = task.getPath();
		final var group = task.getGroup();

		return List.of(name, desc, path, group);
	}
}
