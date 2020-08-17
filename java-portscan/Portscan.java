import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Portscan {

	public static void main(String[] args) throws Exception {
		// port scan the target...
		String target = args[0];
		System.out.println("Scanning " + target);

		long starttime = System.currentTimeMillis();

		Selector selector = Selector.open();
		
		// Now put in everything we want to try to connect to...
		for(int port=1;port<65536;port++) {
			InetSocketAddress isa = new InetSocketAddress(target, port);
			addConnection(selector, isa);
		}

		int total = selector.keys().size();
		int done = 0;

		// Wait for all of them to complete...
		long now = System.currentTimeMillis();
		
		while(true) {
			long timeLeft = 500 - (System.currentTimeMillis() - now);
			if (timeLeft <= 0) break;

			int n = selector.select(timeLeft);	// No timeout, just wait...

			Set t = selector.selectedKeys();
			Iterator i = t.iterator();
			while(i.hasNext()) {
				SelectionKey selk = (SelectionKey) i.next();
                SocketChannel ssc = (SocketChannel) selk.channel();
				i.remove();

				if (selk.isValid()) {
					if (selk.isConnectable()) {
						try {
							ssc.finishConnect();
							System.out.println("OPEN " + ssc);
						} catch(Exception e) {}
						done++;
					}
				}
				selk.cancel();
            }

			if (done==total) break;
		}

		long endtime = System.currentTimeMillis();
		System.out.println("Took " + (endtime - starttime) + "ms");
	}
	
	public static void addConnection(Selector selector, InetSocketAddress target) throws Exception {
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.socket().setSoTimeout(500);
		SelectionKey sk = sc.register(selector, SelectionKey.OP_CONNECT);
		if (sc.connect(target)) {
			System.out.println("OPEN " + sc);
			sk.cancel();
		}
	}
}
