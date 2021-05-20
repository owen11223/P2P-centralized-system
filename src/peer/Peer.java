package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import task.*;
import utils.FileListUtils;

public class Peer {
	public boolean debug;
	public int peerId;
	// threading
	public static int NUM_WORKERS = 5;	
	public static int workerIndex = 0;
	public List<Thread> workerList;
	public ListenerThread listener;
	// task Queue
	public static TaskQueue taskQ;
	public static Semaphore fileObtained;
	// Socket related
	public static int CENTRAL_SERVER_REGISTER_PORT = 4444;
	public int CENTRAL_SERVER_SEARCH_PORT = 4445;
	public static int PEER_FILEREQUEST_PORT = 5555;
	public static int PEER_FILEACCEPT_PORT = 6666;
	public String centralServerIp = "localhost";
	
	// Peer available files
	public static List<String> fileList;
	// files directory
	public static String DL_DIR = "./files/dl/";
	public static String ARCHIVE_DIR = "./files/arch/";
	
	public Peer(int peerId, boolean debugFlag) {
		this.debug = debugFlag;
		this.peerId = peerId;
		
		taskQ = new TaskQueue();
		fileObtained = new Semaphore(0);
		
		this.CENTRAL_SERVER_SEARCH_PORT += peerId; // 4445 to 4445+
		
		// worker threads
		this.spawnWorkers(Peer.NUM_WORKERS);
		// listener thread
		this.spawnListener();
		
		
	}
	public int spawnWorkers(int n) {
		// spawn n WorkerThreads
		workerList = new ArrayList<Thread>();
		for(int i = 0; i < n; i++) {
			Thread t = new WorkerThread(taskQ, fileObtained, debug);
			t.setName(String.format("[(%d)Worker%d]", this.peerId, i));
			t.start();
			workerList.add(t);
		}
		return 0;
	}
	public int spawnListener() {
		// spawn a ListenerThread
		this.listener = new ListenerThread(taskQ, debug);
		this.listener.setName(String.format("[(%d)Listener0]", this.peerId));
		this.listener.start();
		return 0;
	}
	
	// print thread states
	public void threadStates() {
		System.out.println("---PRINTING THREAD STATES---");
		for(Thread t: workerList) {
			System.out.printf("%s: %s, state - %s\n" , peerId, t.getName(), t.getState());
		}
		System.out.printf("%s: %s, state - %s\n" , peerId, listener.getName(), listener.getState());
		System.out.println("-----Done----");
	}
	
	public int registerFiles() {
		if(debug) {
			System.out.println("Registering files to central server...");
		}
		Socket s = null;
		ObjectOutputStream ois = null;
		String fns = "";
		for(String fn: Peer.fileList) {
			fns += fn + " ";
		}
		try {
			s = new Socket(this.centralServerIp, Peer.CENTRAL_SERVER_REGISTER_PORT);
			ois = new ObjectOutputStream(s.getOutputStream());
			ois.writeObject(fns);
		} catch (Exception e) {
			System.out.println("-[Error in registerFiles()");
			System.out.println(e);
		} finally {
			try {
				if(s!=null) s.close();
				if(ois!=null) ois.close();
			}catch (Exception e) {
				System.out.println(e);
			}
		}
		if(debug) {
			System.out.println("File register done.");
		}
		return 0; // assumed it worked
	}
	
	public ArrayList<String> getPeer(String filename) {
		ArrayList<String> peerList = new ArrayList<String>();
		Socket socket = null;
		String res = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;		
		try {
			socket = new Socket(this.centralServerIp, this.CENTRAL_SERVER_SEARCH_PORT);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(filename);
			in = new ObjectInputStream(socket.getInputStream());
			res = (String) in.readObject();
			// parse this string to an ArrayList
			String tmp[] = res.split(" ");
			for(String str: tmp) {
				peerList.add(str);
			}
		} catch (Exception e) {
			System.out.println("-[Error in getPeer()] (data streams/socket?)");
			System.out.println(e);
		} finally {
			// close the streams.
			try {
				if(socket!=null) socket.close();
				if(out!=null) out.close();
				if(in!=null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return peerList;
	}
	public void obtain(String filename) {
		// get the peer host name list that has the file
		ArrayList<String> pl;
		pl = getPeer(filename);
		Task rf = new RequestFile(pl, filename);
		taskQ.add(rf);
		// rendezvous with a the worker that finished accepting the file
		try {
			if(debug) {
				System.out.println("***Acquiring the rendezvous lock.");
			}
			fileObtained.acquire();
			if(debug) {
				System.out.println("***file is obtained. proceeding to next file.");
			}
		} catch (InterruptedException e) {
			System.out.println("-[Error in obtain()] (issues with fileObtained semaphore?)");
			e.printStackTrace();
		}
	}
	
	public void setCentralServerIp(String ip) {
		this.centralServerIp = ip;
	}
	public void setFileList(String flistPath) {
		FileListUtils flu = new FileListUtils(flistPath);
		Peer.fileList = flu.getList();
	}
	
	public void exit() {
		// wait for other peers.
		long tw = 0;
		do {
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();} // sleep for a second
			tw = System.currentTimeMillis() - this.listener.lastReqTime ;
			System.out.printf("time waited: %d\n", tw);
		}while (tw < 5000);
		
		// enqueue [worker count] Exit task on the taskQ and close the server socket on listener
		try {
			for( int i = 0; i < Peer.NUM_WORKERS; i++) {
				if(debug) {
					System.out.println("Enqueue exit");
				}
				
				taskQ.add(new Exit()); // add the request file task
			}			
			listener.server.close();
			
			for(Thread t: workerList) {
				t.join();
				if(debug) {
					System.out.printf("%s joined.\n", t.getName());
				}
			}
			listener.join();
			if(debug) {
				System.out.printf("%s joined.\n", listener.getName());
			}
		} catch (Exception e) {
			System.out.println("-[Error when exiting]");
		}
	}
		

}
