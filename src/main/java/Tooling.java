import com.ensarsarajcic.neovim.java.corerpc.client.StdIoRpcConnection;

import org.gradle.tooling.nvim.NvimConnection;

/** Entry point into the remote plugin. */
public class Tooling {
	public static void main(String[] args) {
		final var rpcConnection = new StdIoRpcConnection();
		NvimConnection.establish().attach(rpcConnection);
	}
}
