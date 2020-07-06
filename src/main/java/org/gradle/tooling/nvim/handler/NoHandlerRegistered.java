package org.gradle.tooling.nvim.handler;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

/** A fallback handler which can be used when no concrete handler is registered. */
public class NoHandlerRegistered extends RequestHandler {
	public NoHandlerRegistered(NvimConnection connection) {
		super(connection);
	}
	
	/**
	 * Always throws an error saying that there is no request handler.
	 */
	public Object handle(String request, Object ... args) throws NeovimRequestException {
		final var message = String.format("No handler implemented for request '%s'", request);
		throw new NeovimRequestException(message);
	}
}
