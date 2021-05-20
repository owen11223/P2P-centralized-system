// A message object that are transfered through sockets.

package ftransfer;

import java.io.*;
import java.util.ArrayList;

public class Message implements Serializable{
	public int num_blocks; // total number of blocks for this transfer
	public int blocks_left; // number of blocks left in the transfer, 0 indicates transfer complete.
	
	// data chunk
	public static int BLOCK_SIZE = 512; // 512 bytes per block
	public byte[] data;
	public int dataLength;
	
	public Message(int nb, int bl, byte[] d, int dl) {
		this.num_blocks = nb;
		this.blocks_left = bl;
		this.data = d;
		this.dataLength = dl;
	}
	
	public String toString() {
		return String.format("%d", this.dataLength);
	}
	private static String FILE_NAME = "./files/arch/f1.txt";
	
	public static void main(String[] args) {
		
	}
	
}
