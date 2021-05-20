package task;

import java.net.*;
import java.util.*;

public class Task {
	public static int PEER_FILEREQUEST_PORT = 5555;
	public static int PEER_FILEACCEPT_PORT = 6666;

	public static String centralServerIp = "localhost";
	
	public Socket socket;
	public ArrayList<String> peerList;
	public String hostname;
	public String filename;
}

