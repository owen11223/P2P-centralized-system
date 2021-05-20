package task;

import java.util.ArrayList;

public class RequestFile extends Task{
	public RequestFile(ArrayList<String> pl, String fn) {
		this.peerList = pl;
		this.filename = fn;
	}
}
