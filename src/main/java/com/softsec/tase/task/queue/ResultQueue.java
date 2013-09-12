/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;

/**
 * 
 * @author yanwei
 * @date 2013-1-22 上午9:54:22
 * @description
 */
public class ResultQueue {
	
	private LinkedBlockingQueue<Result> globalResultQueue = new LinkedBlockingQueue<Result>();
	
	private static final ResultQueue resultQueueSingleton = new ResultQueue();
	
	private static final int DEFAULT_QUEUE_LIMIT = 5000;
	
	private static int MAX_QUEUE_SIZE;
	
	public ResultQueue () {
		MAX_QUEUE_SIZE = Configuration.getInt(Constants.MAX_QUEUE_SIZE, DEFAULT_QUEUE_LIMIT);
	}
	
	public static ResultQueue getInstance() {
		return resultQueueSingleton;
	}
	
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}
	
	public synchronized LinkedBlockingQueue<Result> getResultQueue() {
		return globalResultQueue;
	}
	
	public synchronized boolean addToResultQueue(Result result) {
		return globalResultQueue.add(result);
	}
	
	public synchronized boolean addToResultQueue(Set<Result> resultSet) {
		return globalResultQueue.addAll(resultSet);
	}

}
