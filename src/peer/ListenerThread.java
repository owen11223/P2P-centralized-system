package peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import task.*;

public class ListenerThread extends Thread{
	public boolean debug;
	private TaskQueue taskQ;
	
	public ListenerThread(TaskQueue taskQ, boolean debugFlag) {
		this.taskQ = taskQ;
		debug = debugFlag;
	}
	public ServerSocket server;
	public Socket socket;
	public String filename;
	private DataInputStream in;
	private DataOutputStream out;
	public long lastReqTime;
	
	@Override
	public void run() {
		if(debug) {
			System.out.printf("Thread created, %s\n", Thread.currentThread().getName());
		}		
		try {
			this.lastReqTime = System.currentTimeMillis();
			server = new ServerSocket(Peer.PEER_FILEREQUEST_PORT);
		} catch (Exception e) {
			System.out.println("-[Error in listener] (Socket in use?");
			e.printStackTrace();
			return;
		}
		while(true) {
			try {
				if(debug) {
					System.out.printf("%s Waiting file requests...\n", Thread.currentThread().getName());
					System.out.println("Listener sanity check...");
					System.out.println(server);
				}
				this.socket = server.accept(); // blocks until other peers connect to this.
				if(debug) {
					System.out.printf("%s socket binded - %s\n", Thread.currentThread().getName(), this.socket);
				}
				this.lastReqTime = System.currentTimeMillis();
				filename = "";
				in = new DataInputStream(socket.getInputStream());
				filename = in.readUTF();
				if(debug) {
					System.out.printf("%s filename received - %s\n", 
							Thread.currentThread().getName(), filename);
				}
				// lookup the requested file
				int i = Peer.fileList.indexOf(filename);
				out = new DataOutputStream(socket.getOutputStream());
				if(i == -1) {
					out.writeChar('n'); // don't have file
				}else {
					out.writeChar('y');	// yes file.
					if(debug) {
						System.out.printf("%s written \'y\'\n", Thread.currentThread().getName());
					}
					
					// create a SendFile task
					Task sft = new SendFile(this.socket.getInetAddress().getCanonicalHostName(), filename);
					taskQ.add(sft); // add the request file task
					if(debug) {
						System.out.printf("%s SendFileTask added. %s", Thread.currentThread().getName(), filename);
					}
				}
				// close the socket and streams
				socket.close();
				in.close();
				out.close();
				
			} catch (SocketException e) {
				System.out.println("Listener: server SocketException caught!");
				e.printStackTrace();
				return;
			} catch (Exception e) {
				System.out.println("-[Error in listener] (probably something about datastreams)");
				e.printStackTrace();
			}
			//loop
		}
		
	}
	
}