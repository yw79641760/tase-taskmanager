/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * Task Queue Singleton
 * 		keeping global task queue
 * 		and distribute task queue
 * @author yanwei
 * @date 2013-1-5 下午2:14:14
 * 
 */
public class TaskQueue {

	private PriorityBlockingQueue<Task> globalTaskQueue = new PriorityBlockingQueue<Task>();
	
	private PriorityBlockingQueue<Task> distributeTaskQueue = new PriorityBlockingQueue<Task>();
	
	private static final TaskQueue taskQueueSingleton = new TaskQueue();
	
	private static final int DEFAULT_QUEUE_LIMIT = 1000;
	
	private static int MAX_QUEUE_SIZE;
	
	/**
	 * 
	 */
	public TaskQueue() {
		MAX_QUEUE_SIZE = Configuration.getInt(Constants.MAX_QUEUE_SIZE, DEFAULT_QUEUE_LIMIT);
	}
	
	public static TaskQueue getInstance() {
		return taskQueueSingleton;
	}
	
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}
	
	public synchronized PriorityBlockingQueue<Task> getGlobalTaskQueue() {
		return globalTaskQueue;
	}
	
	public synchronized PriorityBlockingQueue<Task> getDistributeTaskQueue() {
		return distributeTaskQueue;
	}
	
	public synchronized boolean addToGlobalTaskQueue(Task task) {
		return globalTaskQueue.add(task);
	}
	
	public synchronized boolean addToGlobalTaskQueue(Set<Task> taskSet) {
		return globalTaskQueue.addAll(taskSet);
	}
	
	public synchronized boolean addToDistributeTaskQueue(Task task) {
		return distributeTaskQueue.add(task);
	}
	
	public synchronized boolean addToDistributeTaskQueue(Set<Task> taskSet) {
		return distributeTaskQueue.addAll(taskSet);
	}
	
	public synchronized List<Long> getGlobalTaskIdList() {
		List<Long> taskIdList = new ArrayList<Long>();
		for (Task task : getGlobalTaskQueue()) {
			taskIdList.add(task.getTaskId());
		}
		return taskIdList;
	}
	
	public synchronized List<Long> getDistributeTaskIdList() {
		List<Long> taskIdList = new ArrayList<Long>();
		for(Task mission : getDistributeTaskQueue()) {
			taskIdList.add(mission.getTaskId());
		}
		return taskIdList;
	}
}
