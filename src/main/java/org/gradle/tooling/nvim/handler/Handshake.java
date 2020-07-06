package org.gradle.tooling.nvim.handler;

import com.ensarsarajcic.neovim.java.handler.errors.NeovimRequestException;

import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

/** Perform a handshake with Neovim to confirm that the connection is working.
 * <p>
 * A handshake request does not do anything useful, it only performs the bare minimum to confirm
 * that the connection has been established and that requests are handled properly. Use it to verify
 * that everything is connected properly first when diagnosing an issue.
 */
public class Handshake extends RequestHandler {
	public Handshake(NvimConnection connection) {
		super(connection);
	}

	@Override
	public Object handle(String request, Object ... args) throws NeovimRequestException {
		return "OK";
	}
}
