package ftransfer;

import java.io.*;
import java.net.*;

public class Client {


	public final static int SOCKET_PORT = 11111;      // you may change this
	public final static String SERVER = "127.0.0.1";  // localhost
	public final static String FILE_TO_RECEIVED = "/home/justin/dl.txt";
	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
	
	public void recieve() throws UnknownHostException, IOException {
	    int bytesRead;
	    int current = 0;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    Socket sock = null;
	    try {
	      sock = new Socket(SERVER, SOCKET_PORT);
	      System.out.println("Connecting...");

	      // receive file
	      byte [] mybytearray  = new byte [FILE_SIZE];
	      InputStream is = sock.getInputStream();
	      fos = new FileOutputStream(FILE_TO_RECEIVED);
	      bos = new BufferedOutputStream(fos);
	      bytesRead = is.read(mybytearray,0,mybytearray.length);
	      current = bytesRead;

	      do {
	         bytesRead =
	            is.read(mybytearray, current, (mybytearray.length-current));
	         if(bytesRead >= 0) current += bytesRead;
	      } while(bytesRead > -1);

	      bos.write(mybytearray, 0 , current);
	      bos.flush();
	      System.out.println("File " + FILE_TO_RECEIVED
	          + " downloaded (" + current + " bytes read)");
	    }
	    finally {
	      if (fos != null) fos.close();
	      if (bos != null) bos.close();
	      if (sock != null) sock.close();
	    }
	}
	
	public static void main(String[] args) {
		Client c = new Client();
		try {
			c.recieve();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
