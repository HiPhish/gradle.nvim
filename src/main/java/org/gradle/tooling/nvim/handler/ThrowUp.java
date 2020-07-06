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
	public ThrowUp(NvimConnection connection) {
		super(connection);
	}
	
	@Override
	public Object handle(String request, Object ... args) throws NeovimRequestException {
		final var message = "Exception intentionally thrown.";
		throw new NeovimRequestException(message);
	}
}
