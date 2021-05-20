package pa1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import peer.Peer;

/*
 * args
 * 0: [peerid]
 * 
 */

public class TestProg {

	public static void main(String[] args) {
		
		int peerId = Integer.parseInt(args[0]);
		// test the socket connections.
		// testSocket(peerId);
		
		if(peerId == 0) {
			Peer p = new Peer(peerId, true);
			p.setFileList("./files/flist.txt");
			p.setCentralServerIp("192.168.5.12");
			System.out.println("---P0 getting s.1.0.txt from P1..---");
			p.obtain("s.1.0.txt");
			System.out.println("---P0 obtain() returned---");
			p.exit();
		}else if(peerId == 1){
			Peer p = new Peer(peerId, true);
			p.setFileList("./files/flist.txt");
			p.setCentralServerIp("192.168.5.12");
			System.out.println("Idle, waiting request.");
			p.exit();
		}
		
		
		
		
		
		return;
		
		// p0.registerFiles();
		
		// System.out.println(p0.getPeer("tf1.txt"));


		
	}
	
	public static void testSocket(int peerId) {
		Socket client = null;
		ServerSocket server = null;
		Socket s = null;
		
		if(peerId == 0) { // ip: 192.168.5.14
			try {
				client = new Socket("192.168.5.16", 6789);
				System.out.printf("p0 client connected, socket - %s\n", client);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else if (peerId == 1) { // ip: 192.168.5.16
			try {
				server = new ServerSocket(6789);
				s = server.accept();
				System.out.printf("p1 server connected, returned socket - %s\n", s);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			if(client != null) client.close();
			if(server != null) server.close();
			if(s != null) s.close();
		}catch(Exception e) {
			
		}
	}

}
	