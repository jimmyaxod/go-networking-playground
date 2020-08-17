import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Portscan_mt extends Thread {
	String target;
	int range_start;
	int range_end;
	
	public Portscan_mt(String t, int start, int end) {
		target = t;
		range_start = start;
		range_end = end;
	}
	
	public static void main(String[] args) throws Exception {
		// port scan the target...
		String targ = args[0];
		System.out.println("Scanning " + targ);

		long starttime = System.currentTimeMillis();

		// Let's setup 4 threads...
		Portscan_mt ps1 = new Portscan_mt(targ, 1, 16384);
		Portscan_mt ps2 = new Portscan_mt(targ, 16384, 32768);
		Portscan_mt ps3 = new Portscan_mt(targ, 32768, 49152);
		Portscan_mt ps4 = new Portscan_mt(targ, 49152, 65536);
		ps1.start();
		ps2.start();
		ps3.start();
		ps4.start();

		// Wait until they all finish...
		ps1.join();
		ps2.join();
		ps3.join();
		ps4.join();
		
		long endtime = System.currentTimeMillis();
		System.out.println("Took " + (endtime - starttime) + "ms");
		
	}
	
	public void run() {
		try {
			Selector selector = Selector.open();
			
			// Now put in everything we want to try to connect to...
			for(int port=range_start;port<range_end;port++) {
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
		} catch(Exception e) {
			System.err.println("Exception " + e);	
		}
	}
	
	public static void addConnection(Selector selector, InetSocketAddress isa) throws Exception {
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.socket().setSoTimeout(500);
		SelectionKey sk = sc.register(selector, SelectionKey.OP_CONNECT);
		if (sc.connect(isa)) {
			System.out.println("OPEN " + sc);
			sk.cancel();
		}
	}
}
