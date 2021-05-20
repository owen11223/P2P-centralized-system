package utils;

import java.io.*;
import java.util.ArrayList;

public class FileListUtils {
	
	public String FLIST_PATH = "./flist.txt";
	
	public ArrayList<String> fList;
	
	public FileListUtils() {
		// read the default paths for the .txts
		this.fList = fetch(this.FLIST_PATH);
	}
	public FileListUtils(String path) {
		this.fList = fetch(path);
	}
	public ArrayList<String> fetch(String fp){
		BufferedReader reader = null;
		ArrayList<String> l = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(fp));
			String line = reader.readLine();
			while(line != null) {
				l.add(line);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}
	
	public ArrayList<String> getList(){
		return this.fList;
	}
	
	public static void main(String[] args) {
		FileListUtils fl = new FileListUtils();
		System.out.println(fl.getList().get(0));
	}

}
