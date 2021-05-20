package pa1;

import ftransfer.*;
import task.*;
import utils.FileListUtils;
import utils.ResultStamper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Peer_debug {
	public boolean debug;
	public int peerID;
	public static int NUM_PEERS = 4;
	// threading
	public static int NUM_WORKERS = 5;	
	public static int workerIndex = 0;
	public ArrayList<Thread> workerList;
	public ListenerThread listener;
	// task Queue
	public static int QUEUE_SIZE =10;
	public static Queue<Task> taskQ;
	public static Semaphore taskQMutex;
	public static Semaphore taskAvailable;
	public static Semaphore taskQFull;
	public static Semaphore receivingMutex;
	// Socket related
	public int CENTRAL_SERVER_REGISTER_PORT = 4444;
	public int CENTRAL_SERVER_SEARCH_PORT = 4445;
	public int PEER_FILEREQUEST_PORT = 5555;
	public int PEER_FILEACCEPT_PORT = 6666;
	public String centralServerIp = "localhost";
	
	// Peer available files
	public List<String> fileList;
	// files directory
	public static String FILES_DIR = "./files/";
	
	
	// Peer name
	public String name;
	
	public Peer_debug(int peerID) {
		// initiate taskQ
		Peer_debug.taskQ = new LinkedList<Task>();
		Peer_debug.taskQMutex = new Semaphore(1);
		Peer_debug.taskAvailable = new Semaphore(0);
		Peer_debug.taskQFull = new Semaphore(Peer_debug.QUEUE_SIZE);
		Peer_debug.receivingMutex = new Semaphore(1);
		
		this.peerID = peerID;
		this.CENTRAL_SERVER_SEARCH_PORT += peerID;
		
		this.name = "";
		
		// worker threads
		this.spawnWorkers(Peer_debug.NUM_WORKERS);
		// listener thread
		this.spawnListener();
	}

	public int spawnWorkers(int n) {
		// spawn n WorkerThreads
		workerList = new ArrayList<Thread>();
		for(int i = 0; i < n; i++) {
			Thread t = new WorkerThread(Peer_debug.taskQMutex, Peer_debug.taskAvailable, Peer_debug.taskQFull);
			t.setName(String.format("[%sWorker%d]", this.name, i));
			t.start();
			workerList.add(t);
		}
		return 0;
	}
	class WorkerThread extends Thread {
		Semaphore taskQMutex;
		Semaphore taskAvailable;
		Semaphore taskFull;
		
		public Task curTask;		
		public WorkerThread(Semaphore taskQMutex, Semaphore taskAvailable, Semaphore taskFull) {
			this.taskQMutex = taskQMutex;
			this.taskAvailable = taskAvailable;
			this.taskFull = taskFull;
		}
		@Override
		public void run() {
			System.out.printf("Thread created, %s\n", Thread.currentThread().getName());
			while(true) {
				try {
					// wait until there is a task in queue
					System.out.printf("%s Waiting on task...\n", Thread.currentThread().getName());
					this.taskAvailable.acquire();
					this.taskQMutex.acquire(); // acquire the lock
					// access the queue
					curTask = Peer_debug.taskQ.poll();
					System.out.printf("%s Got mutex! Polled - [%s]\n", 
							Thread.currentThread().getName(), curTask.getClass().getName());
					this.taskQMutex.release();
					this.taskFull.release(); // freed up one space
					// find out what task it is.
					if(curTask.getClass() == RequestFile.class) {
						// run request file
						this.requestFileTask((RequestFile) curTask);
					}else if(curTask.getClass() == SendFile.class) {
						// run send file
						this.sendFileTask((SendFile) curTask);
					}else if(curTask.getClass() == Exit.class) {
						return;
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		private void requestFileTask(RequestFile rft) {
			// We now know which peer has the file. Now we request the file from the peer.
			Socket socket = null;
			try {
				System.out.printf("%s In requestFileTask fn - %s\n", Thread.currentThread().getName(), rft.filename);
				socket = new Socket(rft.peerList.get(0), Peer_debug.this.PEER_FILEREQUEST_PORT);
				System.out.printf("%d%s [%s]: Connected to - %s\n",
						Peer_debug.this.peerID, Thread.currentThread().getName(), rft.getClass().getName(), socket);
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(rft.filename); // tell the peer which file we need
				DataInputStream in = new DataInputStream(socket.getInputStream());
				char response;
				response = in.readChar();
				socket.close(); // got the response. close the connection.
				if(response == 'y') {
					// recieve a file at port 6666
					this.recieve(rft);
				}else {
					// perhaps try another host from the list. maybe later.
				}
			} catch (UnknownHostException e) {
				System.out.println("Host not found?");
				System.out.println(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		private void recieve(RequestFile rft) throws IOException, ClassNotFoundException {
			int bytesWritten = 0;
		    FileOutputStream fos = null;
		    BufferedOutputStream bos = null;
		    ObjectInputStream ois = null;
		    ServerSocket reciever = null;
		    Socket socket = null;
		    String fileFullPath = Peer_debug.FILES_DIR + rft.filename;
		    try {
				reciever = new ServerSocket(Peer_debug.this.PEER_FILEACCEPT_PORT);
				socket = reciever.accept(); // wait for the peer to connect
				System.out.printf("%s [%s]: Server accepted connection - %s\n", 
						Thread.currentThread().getName(), rft.getClass().getName(), socket);
				// receive file
				ois = new ObjectInputStream(socket.getInputStream());
				
				// see how many chunks will be sent.
				Message msg = (Message) ois.readObject();
				int nb = msg.num_blocks;
				int blkCount = 1;
				ArrayList<Message> msgChunks = new ArrayList<Message>();
				msgChunks.add(msg);
				bytesWritten += msg.dataLength;
				while(blkCount < nb) {
					// double check. bad practice probably,
					if(msg.blocks_left == 0) {
						System.out.println("break!?");
						break;
					}
					msg = (Message) ois.readObject();
					msgChunks.add(msg);
					blkCount++;
					bytesWritten += msg.dataLength;
					
				}
				System.out.printf("%s Block recieved, blkCount - %d, nb - %d, bytesWritten - %s\n", 
						Thread.currentThread().getName(), blkCount, nb, bytesWritten);
				// build the file from th msgChunks
				MessageUtils.stitchChunks(msgChunks, fileFullPath);
				System.out.printf("%s File constructed from chunks, path - %s\n", 
						Thread.currentThread().getName(), fileFullPath);
		    } finally {
		    	if (fos != null) fos.close();
		    	if (bos != null) bos.close();
		    	if (reciever != null) reciever.close();
		    	if (socket != null) socket.close();
		    	Peer_debug.receivingMutex.release(); // file is recieved. ready for next obtain.
		    }
			return;
		}
	
		private void sendFileTask(SendFile sft) {
			try {
				this.send(sft);
			} catch (IOException e) {
				System.out.println(e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		private void send(SendFile sft) throws IOException, InterruptedException {
		    BufferedInputStream bis = null;
		    ObjectOutputStream oos = null;
		    Socket socket = null;
		    String fileFullPath = Peer_debug.FILES_DIR + sft.filename; // full path to the file
		    try {
		    	// Make sure the receiver thread is ready.
		    	do{
		    	    try{
		                socket = new Socket(sft.hostname, Peer_debug.this.PEER_FILEACCEPT_PORT); // connect to the file accept port
    		        }catch(ConnectException e){
    		    	    System.out.println("Ouch. We're too fast. Reciever thread is not ready.");
    		    	    try {Thread.sleep(100);} catch (InterruptedException ie){}  // wait for the last few threads to finish their tasks.
    		        }
		    	}while(socket == null);
		    	
		    	try {
	    			System.out.printf("%s [%s]: Connected to - %s\n", 
	    					Thread.currentThread().getName(), sft.getClass().getName(), socket);
	    			// send file, split the file into msgChunks.
	    			ArrayList<Message> msgChunks = MessageUtils.getMsgChunks(fileFullPath);
	    			oos = new ObjectOutputStream(socket.getOutputStream());
	    			System.out.printf("%s Sending %d(%d + %d bytes) Message blocks, totalBytes(%d)...\n", 
	    					Thread.currentThread().getName(), 
	    					msgChunks.size(),  Message.BLOCK_SIZE, msgChunks.get(msgChunks.size()-1).dataLength,
	    					(msgChunks.size()-1)*Message.BLOCK_SIZE+msgChunks.get(msgChunks.size()-1).dataLength);
	    			for(Message msg: msgChunks) {
	    				oos.writeObject(msg);
	    			}
	    			System.out.printf("%s Done.\n", Thread.currentThread().getName());
	    		}
	    		finally {
	    			if (bis != null) bis.close();
	    			if (oos != null) oos.close();
	    		};
		    }finally {
		    	if (socket != null) socket.close();
		    }
		    return;
		}
	}
	
	
	public int spawnListener() {
		// spawn a ListenerThread
		this.listener = new ListenerThread();
		this.listener.setName(String.format("[%sListener0]", this.name));
		this.listener.start();
		return 0;
	}
	class ListenerThread extends Thread{
		public ServerSocket listener;
		public Socket socket;
		public String filename;
		private DataInputStream in;
		private DataOutputStream out;
		public long lastReqTime;
		
		@Override
		public void run() {
			System.out.printf("Thread created, %s\n", Thread.currentThread().getName());
			// opens a server socket, :5555
			try {
				this.listener = new ServerSocket(Peer_debug.this.PEER_FILEREQUEST_PORT);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while(true) {
				try {
					this.socket = listener.accept(); // blocks until other peers connect to this.
					this.lastReqTime = System.currentTimeMillis();
					System.out.printf("%s socket binded - %s\n", Thread.currentThread().getName(), this.socket);
					filename = "";
					in = new DataInputStream(socket.getInputStream());
					filename = in.readUTF();
					// lookup the requested file
					int i = Peer_debug.this.fileList.indexOf(filename);
					out = new DataOutputStream(socket.getOutputStream());
					if(i == -1) {
						out.writeChar('n'); // don't have file
					}else {
						out.writeChar('y');	// yes file.
						
						// create a SendFile task
						Task sft = new SendFile(this.socket.getInetAddress().getCanonicalHostName(), filename);
						// enqueue sf to taskQ
						Peer_debug.taskQFull.acquire();
						Peer_debug.taskQMutex.acquire();
						Peer_debug.taskQ.add(sft); // add the request file task
						Peer_debug.taskQMutex.release();
						Peer_debug.taskAvailable.release();
						
					}
					// close the socket and streams
					socket.close();
					in.close();
					out.close();
					
				} catch (SocketException e) {
					System.out.println("Socket is closed by main!");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.out.println("Something went wrong with semaphores. Oh no.");
					e.printStackTrace();
				}
				//loop
			}
			
		}
	}
	
	public ArrayList<String> getPeer(String filename) {
		ArrayList<String> peerList = new ArrayList<String>();
		Socket socket = null;
		String res = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;		
		try {
			socket = new Socket(this.centralServerIp, this.CENTRAL_SERVER_SEARCH_PORT);
			// System.out.printf("%s Connected to %s:%s\n", Thread.currentThread().getName(), socket.getInetAddress().getCanonicalHostName(), socket.getPort());
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(filename);
			in = new ObjectInputStream(socket.getInputStream());
			res = (String) in.readObject();
			// parse this string to an ArrayList
			String tmp[] = res.split(" ");
			for(String str: tmp) {
				peerList.add(str);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		// Receive a response String obj via the socket
		
		return peerList;
	}
	
	// interface functions
	public void obtain(String filename) {
		// get the peer host name list that has the file
		ArrayList<String> pl;
		if(this.name == "(1)") { // debug mode.
			pl = new ArrayList<String>();
			pl.add("localhost");
		}else {
			// call getPeer
			pl = getPeer(filename);
		}
		Task rf = new RequestFile(pl, filename);
		
		try {
			Peer_debug.receivingMutex.acquire(); // make sure only one rf is in the queue
			
			Peer_debug.taskQFull.acquire();
			Peer_debug.taskQMutex.acquire();
			Peer_debug.taskQ.add(rf); // add the request file task
			Peer_debug.taskQMutex.release();
			Peer_debug.taskAvailable.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public int registerFiles() {
		System.out.println("in regfile");
		// talk to 4444
		Socket s = null;
		ObjectOutputStream ois = null;
		String fns = "";
		for(String fn: this.fileList) {
			fns += fn + " ";
		}
		// System.out.println(fns);
		try {
			s = new Socket(this.centralServerIp, this.CENTRAL_SERVER_REGISTER_PORT);
			ois = new ObjectOutputStream(s.getOutputStream());
			ois.writeObject(fns);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1; // unreachable
		} finally {
			
			if(s!=null)
				try {
					s.close();
					if(ois!=null) ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return 0; // assumed it worked
	}
	
// benchmarking functions
	public static void testSRTask() { // send recieve task
		Peer_debug p1 = new Peer_debug(1);
		// initialize the fileList
		p1.fileList = new ArrayList<String>();
		p1.fileList.add("f1.txt"); // indicate f1.txt is in the arch directory
		p1.fileList.add("tf1.in");
		// p1.obtain("f1.txt");
		p1.obtain("tf1.in");
		
	}
	
	public static void testRegFile() {
		Peer_debug p = new Peer_debug(0);
		FileListUtils flu = new FileListUtils();
		p.fileList = flu.getList();
		p.registerFiles();
	}

	// driver
	public static void main(String[] args) {
		// args: 0:[0/1] 1:[peerID] 2:[CentralServerIp] 3:[weak/strong_small/strong_median] 4:[1/2/4]
		// parse the input
		Peer_debug p = null;
		if(args.length == 0) {
			System.out.println("no args.");
			p = new Peer_debug(0);
		}else {
			p = new Peer_debug(Integer.parseInt(args[1]));
			p.centralServerIp = args[2];
			FileListUtils flu = new FileListUtils();
			p.fileList = flu.getList();
			if(args[0].compareTo("0")==0) {
				// register the files to the central server
				System.out.println("Registering files to central server.");
				p.registerFiles();
				System.out.println("Files registered. Exiting.");
				System.exit(0);
			}else {
				System.out.println("Skipping file register.");
			}
			
			long start, end, t;
			if(args[3].compareTo("weak")==0) {
				if(args[4].compareTo("2")==0) {
					// stop peerID 2 and 3
					if(p.peerID==2 || p.peerID==3) {
						System.out.println("Idle.");
						System.exit(0);
					}
				}
				if(args[4].compareTo("1")==0) {
					if(p.peerID==1 || p.peerID==2 || p.peerID==3) {
						System.out.println("Idle.");
						System.exit(0);
					}
				}
				System.out.println("Weak Scaling Search time exp:");
				// create 10k filenames to request
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
					p.getPeer(fn);					
				}
				end = System.currentTimeMillis();
				t = end - start;
				// open benchmarking file, save results
				ResultStamper stamper = new ResultStamper(p.peerID);
				stamper.stampRaw(args[3]+args[4], t);
				
			}else if(args[3].compareTo("strong_small") == 0) {
				if(args[4].compareTo("2") == 0 ) {
					if(p.peerID==2 || p.peerID==3) {
						System.out.println("Idle.");
						System.exit(0);
					}
					System.out.println("Strong Scaling Search and transfer for small size file(10k) with 2 nodes time exp:");
					// create 10k filenames to request
					ArrayList<String> reqFL = new ArrayList<String>();
					for(int i = 0; i < 500; i++) {
						reqFL.add(String.format("s.0.%d.txt", i));
						reqFL.add(String.format("s.1.%d.txt", i));
					}
					start = System.currentTimeMillis();
					for(String fn: reqFL) {
						p.obtain(fn);
					}
					end = System.currentTimeMillis();
					t = end - start;
					ResultStamper stamper = new ResultStamper(p.peerID);
					stamper.stampRaw(args[3]+args[4], t);
				}else if(args[4].compareTo("4") == 0 ) {
					System.out.println("strong Scaling Search and transfer for small size file(10k) with 4 nodes time exp:");
					// create 10k filenames to request
					ArrayList<String> reqFL = new ArrayList<String>();
					for(int i = 0; i < 250; i++) {
						reqFL.add(String.format("s.0.%d.txt", i));
						reqFL.add(String.format("s.1.%d.txt", i));
						reqFL.add(String.format("s.2.%d.txt", i));
						reqFL.add(String.format("s.3.%d.txt", i));
					}
					start = System.currentTimeMillis();
					for(String fn: reqFL) {
						p.obtain(fn);
					}
					end = System.currentTimeMillis();
					t = end - start;
					ResultStamper stamper = new ResultStamper(p.peerID);
					stamper.stampRaw(args[3]+args[4], t);
				}
			}else if(args[3].compareTo("strong_medium") == 0 ) {
				if(args[4].compareTo("2") == 0) {
					if(p.peerID==2 || p.peerID==3) {
						System.out.println("Idle.");
						System.exit(0);
					}
					System.out.println("strong Scaling Search and transfer for medium size file(1k) with 2 nodes time exp:");
					// create 10k filenames to request
					ArrayList<String> reqFL = new ArrayList<String>();
					for(int i = 0; i < 50; i++) {
						reqFL.add(String.format("m.0.%d.txt", i));
						reqFL.add(String.format("m.1.%d.txt", i));
					}
					start = System.currentTimeMillis();
					for(String fn: reqFL) {
						p.obtain(fn);
					}
					end = System.currentTimeMillis();
					t = end - start;
					ResultStamper stamper = new ResultStamper(p.peerID);
					stamper.stampRaw(args[3]+args[4], t);
				}
				if(args[4].compareTo("4") == 0) {
					System.out.println("strong Scaling Search and transfer for median size file(1k) with 4 nodes time exp:");
					// create 10k filenames to request
					ArrayList<String> reqFL = new ArrayList<String>();
					for(int i = 0; i < 25; i++) {
						reqFL.add(String.format("m.0.%d.txt", i));
						reqFL.add(String.format("m.1.%d.txt", i));
						reqFL.add(String.format("m.2.%d.txt", i));
						reqFL.add(String.format("m.3.%d.txt", i));
					}
					start = System.currentTimeMillis();
					for(String fn: reqFL) {
						p.obtain(fn);
					}
					end = System.currentTimeMillis();
					t = end - start;
					ResultStamper stamper = new ResultStamper(p.peerID);
					stamper.stampRaw(args[3]+args[4], t);
				}
			}
			
		}
		try {Thread.sleep(1000);} catch (InterruptedException ie){} // wait a sec to let threads initialize vars
		System.out.printf("%s Task Done. Start timeout sequence.\n", Thread.currentThread().getName());
		while(System.currentTimeMillis()-p.listener.lastReqTime < 10000) { 
			// there was a connection less than 5s ago
			try {Thread.sleep(1000);} catch (InterruptedException ie){}
		}
		System.out.printf("%s Signaling threads to terminate.\n", Thread.currentThread().getName());
		end(p); // signal all threads to terminate.
		System.out.println("All threads terminated gracefully.");
		System.exit(0);
		
	}
	
	public static void end(Peer_debug p) {
		// enqueue [worker count] Exit task on the taskQ and close the server socket on listener
		try {
			System.out.println("main waiting on taskQ");
			Peer_debug.taskQFull.acquire();
			Peer_debug.taskQMutex.acquire();
			System.out.println("main accessing taskQ");
			for( int i = 0; i < Peer_debug.NUM_WORKERS; i++) {
				System.out.println("Enqueue exit");
				Peer_debug.taskQ.add(new Exit()); // add the request file task
			}
			Peer_debug.taskQMutex.release();
			Peer_debug.taskAvailable.release(Peer_debug.NUM_WORKERS);
			
			p.listener.listener.close();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// wait for all threads to join
		try {
			for(Thread t: p.workerList) {
				t.join();
				System.out.printf("%s joined.\n", t.getName());
			}
			p.listener.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


