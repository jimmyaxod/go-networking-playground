import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.*;

/**
 * Fast nscan
 * At the moment, make sure you have enough open filehandles...
 *
 * Timeout	Success
 * 60000    4753
 * 20000	4122
 * 10000	4040
 *  5000    3243
 *  2000	 753
 * 
 */
public class Netscan extends Thread {
	private static long CONNECT_TIMEOUT = 500;
	private static AtomicLong total_success = new AtomicLong(0);
	
	public LinkedList<InetSocketAddress> targets = new LinkedList();
	
	public Netscan() {
	}
	
	public static void main(String[] args) throws Exception {
		
		for(int i=0;i<args.length;i++) {
			if (args[i].equals("--timeout")) {
				i++;
				CONNECT_TIMEOUT = Long.parseLong(args[i]);	
			}
		}
		
		long starttime = System.currentTimeMillis();

		Netscan[] scanners = new Netscan[4];
		
		for(int i=0;i<scanners.length;i++) {
			scanners[i] = new Netscan();	
		}
		
		// Read stdin
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			int num = 0;
			while(true) {
				String l = reader.readLine();
				if (l==null) break;
				int p = l.indexOf(" ");
				if (p!=-1) {
					String host = l.substring(0, p);
					int port = Integer.parseInt(l.substring(p+1, l.length()));
					InetSocketAddress isa = new InetSocketAddress(host, port);
					scanners[num % scanners.length].targets.add(isa);
					num++;					
				}
			}
		} catch(Exception e) {
			System.err.println("Exception reading input " + e);
		}

		System.out.println("Starting threads...");
		for(int i=0;i<scanners.length;i++) scanners[i].start();
		
		System.out.println("Waiting for threads...");
		for(int i=0;i<scanners.length;i++) scanners[i].join();
		
		long endtime = System.currentTimeMillis();
		System.out.println("Took " + (endtime - starttime) + "ms");
		
		System.out.println("Success " + total_success.longValue());
		
	}
	
	public void run() {
		try {
			Selector selector = Selector.open();

			for(int i=0;i<targets.size();i++) {
				InetSocketAddress isa = targets.get(i);
				addConnection(selector, isa);
			}
	
			int total = selector.keys().size();
			int done = 0;
	
			// Wait for all of them to complete...
			long now = System.currentTimeMillis();
			
			while(true) {
				long timeLeft = CONNECT_TIMEOUT - (System.currentTimeMillis() - now);
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
								total_success.incrementAndGet();
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
