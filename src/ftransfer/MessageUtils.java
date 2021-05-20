package ftransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MessageUtils {
	// returns an array of messagechunks from a given filepath
	public static ArrayList<Message> getMsgChunks(String fp){

		ArrayList<Message> msgChunks;
		File inputFile = new File(fp);
		FileInputStream fis;
		int fileSize = (int) inputFile.length();
		byte[] dataChunk;
		msgChunks = new ArrayList<Message>();
		int tnb = fileSize/Message.BLOCK_SIZE + 1; // the last block is shorter.
		int nbl = tnb;
		Message msg;
		int readLength = Message.BLOCK_SIZE;
		int read = 0;
		try {
			fis = new FileInputStream(fp);
			while(fileSize > 0) {
				if (fileSize <= Message.BLOCK_SIZE) {
					readLength = fileSize;
				}
				dataChunk = new byte[readLength];
				read = fis.read(dataChunk, 0, readLength);
				fileSize -= read;
				// save the dataChunk in a Message obj
				msgChunks.add(new Message(tnb, --nbl, dataChunk, read));
			}
		
		}catch (IOException e) {
			System.out.println(e);
		}
		
		return msgChunks;
	}
	
	// create a proper file from an array of message chunks. created on fpOut
	public static int stitchChunks (ArrayList<Message> msgChunks, String fpOut) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fpOut); // write from offset 0.
			for(Message msg: msgChunks) {
				fos.write(msg.data);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static void main(String[] args) {

		String fpIn = "./files/arch/tf1.in";
		String fpOut = "./files/dl/tf1.out";
		ArrayList<Message> msgChunks = testChunkify(fpIn);
		System.out.println(msgChunks.size());
		System.out.println(msgChunks.get(msgChunks.size()-2));
		System.out.println(msgChunks.get(msgChunks.size()-1));
		//testStitch(msgChunks, fpOut);
		
	}
	// test functions
	public static ArrayList<Message> testChunkify(String fp) {
		ArrayList<Message> msgChunks = MessageUtils.getMsgChunks(fp);
		// System.out.printf("chunkSizes - %s\n", msgChunks);
		// System.out.printf("numChunks - %d\n", msgChunks.get(0).num_blocks);
		Message first = msgChunks.get(0);
		Message last = msgChunks.get(msgChunks.size()-1);

		System.out.printf("first message obj:\n nb - %d\n dl - %d\n bl - %d\n", first.num_blocks, first.dataLength, first.blocks_left);
		
		System.out.printf("last message obj:\n nb - %d\n dl - %d\n bl - %d\n", last.num_blocks, last.dataLength, last.blocks_left);
		return msgChunks;
	}
	public static void testStitch(ArrayList<Message> msgChunks, String fpOut) {
		MessageUtils.stitchChunks(msgChunks, fpOut);
		System.out.printf("File constructed from chunks, path - %s\n", fpOut);
	}

}
