package pa1;

import java.util.ArrayList;

import peer.Peer;
import utils.*;

/*
args: 
0:[peerId] 
1:[CentralServerIp] 
2:[debug mode true/false]
3:[path_to_filelist]
4:[regfile flag true/false]
5:[exp type weak/strong_small/strong_medium]
6:[num of peers 1/2/4]
7:[interactive mode none/i]
*/

public class Driver {

	public static void main(String[] args) {

		int peerId;
		String serverIp;
		boolean debugFlag;
		String filelistPath;
		boolean regfileFlag;
		String expType;
		int numPeers;
		
		
		if(args.length == 8) {
			System.out.println("Interactive mode.");
			return;
		}
		if(args.length == 7) {
			peerId = Integer.parseInt(args[0]);
			serverIp = args[1];
			debugFlag = Boolean.parseBoolean(args[2]);
			filelistPath = args[3];
			regfileFlag = Boolean.parseBoolean(args[4]);
			expType = args[5];
			numPeers = Integer.parseInt(args[6]);
		}else {
			System.out.println("Incorrect args.");
			return;
		}
		

		if(peerId >= numPeers) {
			if(debugFlag) {
				System.out.printf("[PeerId(%d)] Not started. Number of peers required: %d\n", peerId, numPeers);
			}
			System.exit(0);
		}
		
		
		
		Peer p = new Peer(peerId, debugFlag);
		p.setCentralServerIp(serverIp);
		
		p.setFileList(filelistPath);

		
		if(regfileFlag) {
			p.registerFiles();
		}else {
			if(debugFlag) {
				System.out.println("Skipping regfile.");
			}
		}
		
		// time for experiments
		long start, end, t;
		ResultStamper stamper = new ResultStamper(peerId);
		// weak: 10k file searches on each peer.
		if(expType.compareTo("weak") == 0) {
			if(debugFlag) {
				System.out.printf("[peerId%d] Starting weak-scaling experiment on %d peers.", peerId, numPeers);
			}
			ArrayList<String> reqFL = new ArrayList<String>();
			for(int i = 0; i < 2500; i++) {
				reqFL.add(String.format("s.0.%d.txt", i));
				reqFL.add(String.format("s.1.%d.txt", i));
				reqFL.add(String.format("s.2.%d.txt", i));
				reqFL.add(String.format("s.3.%d.txt", i));
			}
			// TODO!time this
			start = System.currentTimeMillis();
			for(String fn: reqFL) {
				if(debugFlag) {
					System.out.println(fn);
				}
				p.getPeer(fn);					
			}
			end = System.currentTimeMillis();
			t = end - start;
			// open benchmarking file, save results
			stamper.stampRaw(expType+args[6], t);
		}else if(expType.compareTo("strong_small") == 0) {
			if(debugFlag) {
				System.out.printf("[peerId%d] Starting strong-scaling(small) experiment on %d peers.", peerId, numPeers);
			}
			ArrayList<String> reqFL = new ArrayList<String>();
			if(numPeers==1) {
				for(int i = 0; i < 10000; i++) {
					reqFL.add(String.format("s.0.%d.txt", i));
				}
			}
			
			if(numPeers == 2) {
				for(int i = 0; i < 5000; i++) {
					reqFL.add(String.format("s.0.%d.txt", i));
					reqFL.add(String.format("s.1.%d.txt", i));
				}
			}else if(numPeers == 4) {
				for(int i = 0; i < 2500; i++) {
					reqFL.add(String.format("s.0.%d.txt", i));
					reqFL.add(String.format("s.1.%d.txt", i));
					reqFL.add(String.format("s.2.%d.txt", i));
					reqFL.add(String.format("s.3.%d.txt", i));
				}
			}
			start = System.currentTimeMillis();
			for(String fn: reqFL) {
				if(debugFlag) {
					System.out.printf("[%d] Driver - obtaining file - \'%s\'\n", peerId, fn);
				}
				p.obtain(fn);
				if(debugFlag) {
					System.out.printf("[%d] Driver - Got the file \'%s\', proceeding to next file\n", peerId, fn);
				}
			}
			end = System.currentTimeMillis();
			t = end - start;
			stamper.stampRaw(expType+args[6], t);
		}else if(expType.compareTo("strong_medium") == 0) {
			if(debugFlag) {
				System.out.printf("[peerId%d] Starting strong-scaling(medium) experiment on %d peers.", peerId, numPeers);
			}
			ArrayList<String> reqFL = new ArrayList<String>();
			if(numPeers == 2) {
				for(int i = 0; i < 500; i++) {
					reqFL.add(String.format("m.0.%d.txt", i));
					reqFL.add(String.format("m.1.%d.txt", i));
				}
			}else if(numPeers == 4) {
				for(int i = 0; i < 250; i++) {
					reqFL.add(String.format("m.0.%d.txt", i));
					reqFL.add(String.format("m.1.%d.txt", i));
					reqFL.add(String.format("m.2.%d.txt", i));
					reqFL.add(String.format("m.3.%d.txt", i));
				}
			}
			start = System.currentTimeMillis();
			for(String fn: reqFL) {
				if(debugFlag) {
					System.out.println(fn);
				}
				p.obtain(fn);
			}
			end = System.currentTimeMillis();
			t = end - start;
			stamper.stampRaw(expType+args[6], t);
		}
		
		// experiments done.
		p.exit();
		System.exit(0);
		
		
	}

}
