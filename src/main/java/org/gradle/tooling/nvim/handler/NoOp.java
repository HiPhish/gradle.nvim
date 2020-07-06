package org.gradle.tooling.nvim.handler;

import org.gradle.tooling.nvim.NvimConnection;
import org.gradle.tooling.nvim.RequestHandler;

/**
 * A no-operation request handler, ignores all its arguments and returns {@code null}.
 */
public class NoOp extends RequestHandler {
	public NoOp(NvimConnection connection) {
		super(connection);
	}

	@Override
	public Object handle(String request, Object ... args) {
		return null;
	}
}
