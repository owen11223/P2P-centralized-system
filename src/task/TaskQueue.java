package task;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;


public class TaskQueue {
	
	private Queue<Task> taskQ;
	private Semaphore taskQMutex;
	private Semaphore taskAvailable;
	
	public TaskQueue() {
		taskQ = new LinkedList<Task>();
		taskQMutex = new Semaphore(1);
		taskAvailable = new Semaphore(0);
	}
	
	// thread-safe access functions
	public boolean add(Task t) {
		try {
			taskQMutex.acquire();
			taskQ.add(t);
			taskQMutex.release();
			taskAvailable.release(1);
			
		} catch (Exception e) {
			System.out.println("Add task failed.");
			return false;
		}
		return true;
	}
	
	public Task poll() {
		Task t = null;
		try {
			taskAvailable.acquire();
			taskQMutex.acquire();
			t = taskQ.poll();
			taskQMutex.release();
		} catch (Exception e) {
			System.out.println("Poll task failed.");
			return null;
		}
		return t;
	}
	
	public Task peek() {
		Task t = null;
		try {
			taskQMutex.acquire();
			t = taskQ.peek();
			taskQMutex.release();
		} catch (Exception e) {
			System.out.println("Peek task failed.");
			return null;
		}
		return t;
	}
}
