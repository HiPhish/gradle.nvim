package org.gradle.tooling.nvim;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

/** Handler for a request from Neovim. */
public abstract class RequestHandler {

	/** Connection to the current Neovim instance; can send requests and messages back to Neovim. */
	protected NvimConnection connection;

	/** Create a new instance of the request handler for a given Neovim connection.
	 *
	 * @param connection The current Neovim connection.
	 */
	public RequestHandler(NvimConnection connection) {
		this.connection = connection;
	}

	/**
	 * Handle the request which is implemented by this class.
	 *
	 * @param request Name of the request, rarely used by implementations.
	 * @param args Arguments of the request, if any.
	 * @return Result of the request.
	 */
	public abstract Object handle(String request, String ... args) throws NeovimRequestException;
}
