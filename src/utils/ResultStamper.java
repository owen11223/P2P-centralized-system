package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ResultStamper {
	public static String fp = "./result.txt";
	public int peerID;
	public Date date;
	SimpleDateFormat formatter;
	
	public ResultStamper(int peerID) {
		this.peerID = peerID;
		this.date = new Date();
		this.formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	}
	public void stamp_annotate(String expType, long time) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fp, true));
			bw.append(String.format("[%s] PeerID - %d, Exp.Type - %s, time - %d (ms)\n", 
					formatter.format(date), peerID, expType, time));
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stampRaw(String expType, long time) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fp, true));
			bw.append(String.format("[%s] %d %d %s\n", 
					formatter.format(date), peerID, time, expType));
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
