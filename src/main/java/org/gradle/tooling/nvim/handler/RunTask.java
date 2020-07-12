package org.gradle.tooling.nvim.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.task.TaskProgressEvent;
import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

public class RunTask extends RequestHandler {
	public RunTask(NvimConnection connection) {
		super(connection);
	}

	/** Event listener for a running task. */
	private class RunListener implements ProgressListener {
		private NvimConnection connection;

		public RunListener(NvimConnection connection) {
			this.connection = connection;
		}

		@Override
		public void statusChanged(ProgressEvent event) {
			// For now we just echo out the events, but for the future I would like to pass events
			// in a more structured manner to Neovim. I could accumulate them in an ordered
			// collection and send a list back to Neovim when the task is done. But it would be
			// better to send the events immediately as they keep coming in.
			if (!(event instanceof TaskProgressEvent)) {
				return;
			}

			final var displayName = event.getDisplayName();
			final var time = event.getEventTime();
			final var command = String.format("echom '%d: %s'", time, displayName);

			try {
				connection.notify("nvim_command", command);
				// connection.notify("nvim_call_function", "gradle#OnTaskStep", List.of());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object handle(String request, Object ... args) throws NeovimRequestException {
		try {
			final var path = (String) args[0];
			final var task = (String) args[1];

			final var buildLauncher = connection.fetchProjectConnection(path)
				.newBuild()
				.forTasks(task)
				.addProgressListener(new RunListener(connection));

			// TODO: get feedback at the task is running, display it in Neovim Must investigate the
			// more complex options which the API provides. Methods of interest:
			//   - addProgressListener
			//   - withArguments
			//   - setStandard{Input,Output,Error}

			buildLauncher.run();
		} catch (FileNotFoundException e) {
			throw new NeovimRequestException(e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			throw new NeovimRequestException(e.getMessage());
		}
		return "OK";
	}
}
