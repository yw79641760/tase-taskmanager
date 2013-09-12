/**
 * 
 */
package com.softsec.tase.task.customer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;
import com.softsec.tase.task.queue.ResultQueue;

/**
 * Result queue customer
 * 		thread to handle result queue
 * @author yanwei
 * @date 2013-1-22 下午1:46:13
 * 
 */
public class ResultCollectorCustomer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollectorCustomer.class);
	
	private int customerCount;
	
	private ExecutorService threadPool;
	
	public ResultCollectorCustomer() {
		customerCount = Configuration.getInt(Constants.RESULT_COLLECTOR_COUNT, 3);
	}
	
	public ResultCollectorCustomer(int customerCount) {
		this.customerCount = customerCount;
	}
	
	
	/**
	 * result collection thread pool
	 */
	public void start() {
		LOGGER.info("Create result collection queue customers, customer count : [ " + customerCount + " ].");
		threadPool = Executors.newFixedThreadPool(customerCount);
		for (int i = 0; i < customerCount; i++) {
			Runnable resultCollectorRunner = new ResultCollectorThread(ResultQueue.getInstance().getResultQueue(), 
					new ResultCollector(), new TaskReloader());
			threadPool.submit(new Thread(resultCollectorRunner, "ResultCollector-" + i));
		}
		threadPool.shutdown();
	}
}
