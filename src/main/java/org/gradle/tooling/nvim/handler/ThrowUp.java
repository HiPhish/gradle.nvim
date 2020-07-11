package org.gradle.tooling.nvim.handler;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

/** Intentionally throw an exception to Neovim.
 * <p>
 * Always throws an exception; useless in production, but good for ensuring that exceptions are
 * indeed thrown.
 */
public class ThrowUp extends RequestHandler {
	private static final String message =
		"Exception intentionally thrown from Gradle remote plugin.";

	public ThrowUp(NvimConnection connection) {
		super(connection);
	}
	
	@Override
	public Object handle(String request, Object ... args) throws NeovimRequestException {
		throw new NeovimRequestException(message);
	}
}
