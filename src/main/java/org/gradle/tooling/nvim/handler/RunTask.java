package org.gradle.tooling.nvim.handler;

import java.io.FileNotFoundException;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

public class RunTask extends RequestHandler {
	public RunTask(NvimConnection connection) {
		super(connection);
	}

	@Override
	public Object handle(String request, String ... args) throws NeovimRequestException {
		try {
			final var path = args[0];
			final var task = args[1];

			final var buildLauncher = connection.fetchProjectConnection(path)
				.newBuild()
				.forTasks(task);

			// TODO: get feedback at the task is running, display it in Neovim Must investigate the
			// more complex options which the API provides.
			buildLauncher.run();
		} catch (FileNotFoundException e) {
			throw new NeovimRequestException(e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			throw new NeovimRequestException(e.getMessage());
		}
		return "OK";
	}
}
