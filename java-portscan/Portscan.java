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
//			System.out.println("Scanning " + target + " " + port);
			InetSocketAddress isa = new InetSocketAddress(target, port);
			addConnection(selector, isa);
		}

		int total = selector.keys().size();
		int done = 0;

		// Wait for all of them to complete...
		while(true) {
			int n = selector.select();			// No timeout, just wait...

			LinkedList<SelectionKey> removes = new LinkedList();
			Set t = selector.selectedKeys();
			Iterator i = t.iterator();
			while(i.hasNext()) {
				SelectionKey selk = (SelectionKey) i.next();
		                SocketChannel ssc = (SocketChannel) selk.channel();
				i.remove();
				// Do it...
				if (selk.isValid()) {
					if (selk.isConnectable()) {
						try {
							ssc.finishConnect();
							System.out.println("OPEN " + ssc);
						} catch(Exception e) {
							//
						}
						done++;
					}
				}

				selk.cancel();
            }

//			System.out.println("Waiting " + selector.keys().size() + " " + done + "/" + total);
			if (done==total) break;
		}

		long endtime = System.currentTimeMillis();
		System.out.println("Took " + (endtime - starttime) + "ms");
		// Now check what succeeded
	}
	
	public static void addConnection(Selector selector, InetSocketAddress target) throws Exception {
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		SelectionKey sk = sc.register(selector, SelectionKey.OP_CONNECT);
		if (sc.connect(target)) {
			System.out.println("OPEN " + sc);
		}
	}
}
