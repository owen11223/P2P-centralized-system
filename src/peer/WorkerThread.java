package peer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import ftransfer.Message;
import ftransfer.MessageUtils;
import task.Exit;
import task.RequestFile;
import task.SendFile;
import task.Task;
import task.TaskQueue;

public class WorkerThread extends Thread{
	public boolean debug;
	private TaskQueue taskQ;
	private Semaphore fileObtained;
	
	public WorkerThread(TaskQueue taskQ, Semaphore fileObtained, boolean debugFlag) {
		this.taskQ = taskQ;
		this.fileObtained = fileObtained;
		debug = debugFlag;
	}
		
	@Override
	public void run() {
		if(debug) {
			System.out.printf("Thread created, %s\n", Thread.currentThread().getName());
		}
		Task curTask = null;
		while(true) {
			curTask = taskQ.poll(); // poll a task
			if(debug) {
				System.out.printf("%s Polled - [%s]\n", 
						Thread.currentThread().getName(), curTask.getClass().getName());
			}
			// find out what task it is.
			try {
				if(curTask.getClass() == RequestFile.class) {
					requestFileTask((RequestFile) curTask);
					fileObtained.release();
				}else if(curTask.getClass() == SendFile.class) {
					// run send file
					sendFileTask((SendFile) curTask);
				}else if(curTask.getClass() == Exit.class) {
					return;
				}
			}catch(Exception e) {
				System.out.println("[fileRequestMutex Error]");
			}
			

		}
	}
	private void requestFileTask(RequestFile rft) {
		// We now know which peer has the file. Now we request the file from the peer.
		Socket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		try {
			if(debug) {
				//check rtf here
				System.out.printf("%s In requestFileTask fn - %s\n", Thread.currentThread().getName(), rft.filename);
				System.out.printf("%s rtf peerList.get(0) - %s\n", Thread.currentThread().getName(), rft.peerList.get(0));
			}
			while(socket == null) {
				try {
					socket = new Socket(rft.peerList.get(0), Peer.PEER_FILEREQUEST_PORT);
				}catch(Exception e) {
					System.out.printf("%s ServerSocket connection refused. Retrying in 500ms.\n", Thread.currentThread().getName());
					Thread.sleep(500);
				}
			}
			
			if(debug) {
				System.out.printf("%s [%s]: Connected to - %s\n",
						Thread.currentThread().getName(), rft.getClass().getName(), socket);
				System.out.printf("%s Writing filename - %s to socket\n", 
						Thread.currentThread().getName(), rft.filename);
			}
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(rft.filename); // tell the peer which file we need
			if(debug) {
				System.out.printf("%s Write to socket done\n", 
						Thread.currentThread().getName());
			}
			in = new DataInputStream(socket.getInputStream());
			char response;
			response = in.readChar();
			socket.close(); // got the response. close the connection.
			if(debug) {
				System.out.printf("%s recieved response %s\n", 
					Thread.currentThread().getName(), response);
			}
			
			if(response == 'y') {
				this.receive(rft);
			}else {
				// perhaps try another host from the list. maybe later.
				throw new Exception("Oh no. The requested peer responded 'n'.");
			}
		} catch (Exception e) {
			System.out.println("-[Error in requestFileTask] (maybe check rft?)");
			System.out.println(e);
			System.out.println(rft.peerList.get(0));
		}
		return;
	}
	private void receive(RequestFile rft) throws IOException, ClassNotFoundException {
		int bytesWritten = 0;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    ObjectInputStream ois = null;
	    ServerSocket reciever = null;
	    Socket socket = null;
	    String fileFullPath = Peer.DL_DIR + rft.filename;
	    try {
			reciever = new ServerSocket(Peer.PEER_FILEACCEPT_PORT);
			socket = reciever.accept(); // wait for the peer to connect
			if(debug) {
				System.out.printf("%s [%s]: receive() - Connection - %s\n", 
						Thread.currentThread().getName(), rft.getClass().getName(), socket);
			}
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
					break;
				}
				msg = (Message) ois.readObject();
				msgChunks.add(msg);
				blkCount++;
				bytesWritten += msg.dataLength;
				
			}
			if(debug) {
				System.out.printf("%s Block recieved, blkCount - %d, nb - %d, bytesWritten - %s\n", 
						Thread.currentThread().getName(), blkCount, nb, bytesWritten);
				// build the file from msgChunks
				MessageUtils.stitchChunks(msgChunks, fileFullPath);
				System.out.printf("%s File constructed from chunks, path - %s\n", 
						Thread.currentThread().getName(), fileFullPath);
			}
			
	    } finally {
	    	if (fos != null) fos.close();
	    	if (bos != null) bos.close();
	    	if (reciever != null) reciever.close();
	    	if (socket != null) socket.close();
	    }
		return;
	}

	private void sendFileTask(SendFile sft) {
		try {
			this.send(sft);
		} catch (Exception e) {
			System.out.println("-[Error in sendFileTask]");
		}
		return;
	}
	
	private void send(SendFile sft) throws IOException, InterruptedException {
	    BufferedInputStream bis = null;
	    ObjectOutputStream oos = null;
	    Socket socket = null;
	    String fileFullPath = Peer.ARCHIVE_DIR + sft.filename; // full path to the file
	    try {
	    	// Make sure the receiver thread is ready.
	    	do{
	    	    try{
	                socket = new Socket(sft.hostname, Peer.PEER_FILEACCEPT_PORT); // connect to the file accept port
		        }catch(ConnectException e){
		        	if(debug) {
		        		System.out.println("Ouch. We're too fast. Reciever thread is not ready.");
		        	}
		    	    try {Thread.sleep(100);} catch (InterruptedException ie){}
		        }
	    	}while(socket == null);
	    	
	    	try {
	    		if(debug) {
	    			System.out.printf("%s [%s]: Connected to - %s\n", 
    					Thread.currentThread().getName(), sft.getClass().getName(), socket);
	    		}
    			// send file, split the file into msgChunks.
    			ArrayList<Message> msgChunks = MessageUtils.getMsgChunks(fileFullPath);
    			oos = new ObjectOutputStream(socket.getOutputStream());
    			if(debug) {
    				System.out.printf("%s Sending %d(%d + %d bytes) Message blocks, totalBytes(%d)...\n", 
    					Thread.currentThread().getName(), 
    					msgChunks.size(),  Message.BLOCK_SIZE, msgChunks.get(msgChunks.size()-1).dataLength,
    					(msgChunks.size()-1)*Message.BLOCK_SIZE+msgChunks.get(msgChunks.size()-1).dataLength);
    			}
    			
    			for(Message msg: msgChunks) {
    				oos.writeObject(msg);
    			}
    			
    			if(debug) {
    				System.out.printf("%s Done.\n", Thread.currentThread().getName());
    			}
    			
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
