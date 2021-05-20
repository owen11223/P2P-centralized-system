package pa1;

import java.io.*;
import java.net.*;
import java.util.*;

public class CentralIndexServer {

	public CentralIndexServer() {
		// Start Register and Search with specific port
		int Port_Register0 = 4444;
		int Port_Search0 = 4445;
		int Port_Search1 = 4446;
		int Port_Search2 = 4447;
		int Port_Search3 = 4448;
		
		//Select the model
        System.out.println("========== Please set the model of sever ==========");
		System.out.println("1. Set the filename index from peer client");
		System.out.println("2. Set the filename index from local");
		Scanner scanner = new Scanner(System.in);
		int n=scanner.nextInt();
		scanner.close();
//		System.out.println(n);
		if (n==1) {
			Port_Register0 =4444;
		}else if (n==2) {
			Port_Register0 =5000;
		}else {
			System.out.println("Model choose woring");
			return;
		}
				
		Thread R0_thread = new Thread (new SeverListener(Port_Register0));   	
		Thread S0_thread = new Thread (new SeverListener(Port_Search0));    	
		Thread S1_thread = new Thread (new SeverListener(Port_Search1));    	
		Thread S2_thread = new Thread (new SeverListener(Port_Search2));   	
		Thread S3_thread = new Thread (new SeverListener(Port_Search3));  
			
		R0_thread.start();
		S0_thread.start();
		S1_thread.start();
		S2_thread.start();
		S3_thread.start();
	}
	
	public static void main(String[] args) {
		System.out.println("========== Central Index Server start ==========");
		System.out.println("Awaiting peer connect to register and search...");
	    @SuppressWarnings("unused")
		CentralIndexServer main = new CentralIndexServer();
	}
}

//Set the save data form of peer
class PeerInfo
{
	String filename;   
	int peerid;
	String ipAddress;
}

class SeverListener implements Runnable {

	ServerSocket server;
	Socket connection;
	BufferedReader br = null;
	public String DataReceive ="";
	int port = 0;
	static int maxsize = 0;
	int PeerID = 1 ;
	char var = 0;
	String test="";
   
	static HashMap<String,String> hm=new HashMap<String,String>();

	public SeverListener(int port) {
		this.port = port;
	}

	/* Beginning of Run Method */	
	public void run() {

		//Listening for Register	       
		switch (port)
		{
		case 4444:
			try {
				System.out.println("Model 1: Start waiting register from peer....");
				server = new ServerSocket(port);
				while (true) {
					connection = server.accept();			
					System.out.println("The sever is connecting from  " + connection.getInetAddress().getHostName() + "  for Registration.");
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					DataReceive = (String)in.readObject();
					System.out.println("Sever start to register...");	
					System.out.print("The filenames are:  ");
					System.out.println(DataReceive);
					String[] var;
					var = DataReceive.split(" ");     // Separate the data by the flag(space) 
		        	//int PeerID = Integer.parseInt(var[0]);
					String IP_address = connection.getInetAddress().getHostName();
					//Saving the filename and peerID
					for(int x = 0; x < var.length ; x++){
						PeerInfo Info = new PeerInfo();
						Info.filename = var[x];                             
						Info.peerid = PeerID;
						Info.ipAddress = IP_address;
						// hash map
					    hm.put(var[x], IP_address);
						maxsize++;
					}
					PeerID++;  //Ready for next peer
					System.out.println("Peer register on sever successfully !!!!!");	
					System.out.println("------------------------------------------------");
					in.close();
					connection.close();   				
				}
			} 
			
            //To handle the Exceptions
			catch(ClassNotFoundException noclass){                                    
				System.err.println("The sever can not recognize the data which sent bt client.");
			}
			catch(IOException ioException){                                           
				System.out.println(ioException);
			} 
			finally {
			}
			
		case 5000:	
			try { 	
				System.out.println("Model 2: Starting register from localhost....");
//				String pathname = "/home/yuhan/Desktop/group-5/pa1/src/pa1/data_peer.txt";
				URL url = this.getClass().getResource("/");
			    String path = url.getPath();
				String pathname = path +"/pa1/register_data.txt";
				System.out.println("Filepath : "+pathname);
				File filename = new File(pathname); 

				InputStreamReader reader = new InputStreamReader(
				new FileInputStream(filename)); 
				BufferedReader br = new BufferedReader(reader); 
				String line = "";
	            int a =0;
				while (line != null) {
					line = br.readLine(); 
					if (a!=404040) {				
					var = line.charAt(2);
					char p1='0';
					char p2='1';
					char p3='2';
					char p4='3';
					hm.put("1", "x");
					if(var==p1)  
						hm.put(line, "192.168.5.14");
					else if(var==p2)
						hm.put(line, "192.168.5.16");
					else if(var==p3)
						hm.put(line, "192.168.5.15");
					else if(var==p4)
				     	hm.put(line, "192.168.5.17");

					a++;
		           }  
				}
				
				if (hm.containsValue("192.168.4.17") )
				{
					 System.out.println("Bingo!");
				}
		             System.out.println("Peer 1 hostname: "+hm.get("s.0.2499.txt"));
		             System.out.println("Peer 2 hostname: "+hm.get("l.1.5.txt"));  
		             System.out.println("Peer 3 hostname: "+hm.get("m.2.500.txt"));
		             System.out.println("Peer 4 hostname: "+hm.get("l.3.5.txt"));
						br.close();			
            System.out.println("========= Data register from localhost successfully! ===========");
		}
			catch (Exception e) {
				e.printStackTrace();
			}

        //Listening for Search
	    //Peer1
		case 4445:
			try {
//				System.out.println("Waiting peer 1 search...");
				server = new ServerSocket(port);
				while (true) {
					connection = server.accept();			
//					System.out.println("The sever is connecting from  " +connection.getInetAddress().getHostName()+ " for Search");
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					DataReceive = (String)in.readObject();
//					System.out.println("Sever start to search...");	
					String Find_Address = "";					
					if (hm.containsKey(DataReceive) )
					{

						Find_Address = hm.get(DataReceive);

						System.out.println("Central Index Server finds the file ( "+ DataReceive + " ) in peer: " + Find_Address);
//				  	    System.out.println(Find_Address);
					}
					else {
						System.out.println("Central Index Server can not finds the file ( "+ DataReceive + " ) : ");
						 System.out.println("X");
					}
					

					
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					//clear buffer
					out.flush();			
	     			//Seed the result to client.
					out.writeObject(Find_Address);                       
					out.flush();			
					in.close();
					out.close();
					connection.close();   				
//					System.out.println("------------Connection close---------------");		
				}
			} 

            //To handle the Exceptions
			catch(ClassNotFoundException noclass){                                    
				System.err.println("The sever can not recognize the data which sent bt client.");
			}
			catch(IOException ioException){                                           
				System.out.println(ioException);
			}
			finally {
			}
        
		//Peer2
		case 4446:
			try {
//				System.out.println("Waiting peer 2 search...");
				server = new ServerSocket(port);
				while (true) {
					connection = server.accept();			
//					System.out.println("The sever is connecting from  " +connection.getInetAddress().getHostName()+ " for Search");
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					DataReceive = (String)in.readObject();
//					System.out.println("Sever start to search...");	
					String Find_Address = "";
					
					if (hm.containsKey(DataReceive) )
					{
						Find_Address = hm.get(DataReceive);
						System.out.println("Central Index Server finds the file ( "+ DataReceive + " ) in peer: "+ Find_Address);
//					    System.out.println(Find_Address);
					}
					else {
						System.out.println("Central Index Server can not finds the file ( "+ DataReceive + " ) : ");
						 System.out.println("X");
					}
					
//					System.out.println("------------------------------------------------");	
					
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					//clear buffer
					out.flush();			
	     			//Seed the result to client.
					out.writeObject(Find_Address);                       
					out.flush();			
					in.close();
					out.close();
					connection.close();   	
//					System.out.println("------------Connection close---------------");	
				}
			} 

            //To handle the Exceptions
			catch(ClassNotFoundException noclass){                                    
				System.err.println("The sever can not recognize the data which sent bt client.");
			}
			catch(IOException ioException){                                           
				System.out.println(ioException);
			}
			finally {
			}
		
		//Peer3
		case 4447:
			try {
//				System.out.println("Waiting peer 3 search...");
				server = new ServerSocket(port);
				while (true) {
					connection = server.accept();			
//					System.out.println("The sever is connecting from  " +connection.getInetAddress().getHostName()+ " for Search");
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					DataReceive = (String)in.readObject();
//					System.out.println("Sever start to search...");	
					String Find_Address = "";
					
					if (hm.containsKey(DataReceive) )
					{

						Find_Address = hm.get(DataReceive);
//						Find_Address =curInfo.ipAddress ;
						System.out.println("Central Index Server finds the file ( "+ DataReceive + " ) in peer: "+ Find_Address);
//						 System.out.println(Find_Address);
					}
					else {
						System.out.println("Central Index Server can not finds the file ( "+ DataReceive + " ) : ");
						 System.out.println("X");
					}
					
				
//					System.out.println("------------------------------------------------");	
					
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					//clear buffer
					out.flush();			
	     			//Seed the result to client.
					out.writeObject(Find_Address);                       
					out.flush();			
					in.close();
					out.close();
					connection.close();   				
				}
			} 

            //To handle the Exceptions
			catch(ClassNotFoundException noclass){                                    
				System.err.println("The sever can not recognize the data which sent bt client.");
			}
			catch(IOException ioException){                                           
				System.out.println(ioException);
			}
			finally {
			}
			
		//peer4
		case 4448:
			try {
//				System.out.println("Waiting peer 4 search...");
				server = new ServerSocket(port);
				while (true) {
					connection = server.accept();			
//					System.out.println("The sever is connecting from  " +connection.getInetAddress().getHostName()+ " for Search");
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					DataReceive = (String)in.readObject();
//					System.out.println("Sever start to search...");	
					String Find_Address = "";
					
					if (hm.containsKey(DataReceive) )
					{

						Find_Address = hm.get(DataReceive);
						System.out.println("Central Index Server finds the file ( "+ DataReceive + " ) in peer: "+ Find_Address);
//						 System.out.println(Find_Address);
					}
					else {
						System.out.println("Central Index Server can not finds the file ( "+ DataReceive + " ) : ");
						 System.out.println("X");
					}
				
//					System.out.println("------------------------------------------------");	
					
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					//clear buffer
					out.flush();			
	     			//Seed the result to client.
					out.writeObject(Find_Address);                       
					out.flush();			
					in.close();
					out.close();
					connection.close();   				
				}
			} 

            //To handle the Exceptions
			catch(ClassNotFoundException noclass){                                    
				System.err.println("The sever can not recognize the data which sent bt client.");
			}
			catch(IOException ioException){                                           
				System.out.println(ioException);
			}
			finally {
			}
		}		
	}
}
