import com.ensarsarajcic.neovim.java.corerpc.client.StdIoRpcConnection;

import org.gradle.tooling.nvim.NvimConnection;

public class Tooling {
	public static void main(String[] args) {
		NvimConnection.establish().attach(new StdIoRpcConnection());
	}
}
