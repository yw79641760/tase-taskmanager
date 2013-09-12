/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;

/**
 * TaskObserver.java
 * @author yanwei
 * @date 2013-3-27 上午10:13:51
 * @description
 */
public class TaskObserver implements Observer {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(TaskObserver.class);

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object object) {
		
		Task task = (Task)object;
		LOGGER.info("Reloading task [ " + task.getTaskId() + " ] into global task queue ...");
		
		PriorityBlockingQueue<Task> globalTaskQueue = TaskQueue.getInstance().getGlobalTaskQueue();
		// do not load unfinished task in database when reload task
		globalTaskQueue.add(task);
		
		LOGGER.info("Reloaded task [ " + task.getTaskId() + " ] into global task queue ...");
	}

}
