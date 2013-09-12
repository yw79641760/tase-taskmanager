package com.softsec.tase.task.customer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;
import com.softsec.tase.task.queue.TaskQueue;


/**
 * 任务下发队列消费者，负载创建下发队列线程。
 * 
 * @author yanwei
 * 
 */
public class TaskDistributorCustomer {

	private static final Logger LOGGER = Logger.getLogger(TaskDistributorCustomer.class);

	private int customerCount;

	private ExecutorService threadPool;

	public TaskDistributorCustomer() {
		customerCount = Configuration.getInt(Constants.TASK_DISTRIBUTOR__COUNT, 3);
	}

	public TaskDistributorCustomer(int customerCount) {
		this.customerCount = customerCount;
	}

	/**
	 * task distribution thread pool
	 */
	public void start() {
		LOGGER.info("Create task distribution queue customers, customer count : [ " + customerCount + " ].");
		threadPool = Executors.newFixedThreadPool(customerCount);
		for (int i = 0; i < customerCount; i++) {
			Runnable taskDistributorRunner = new TaskDistributorThread(TaskQueue.getInstance().getDistributeTaskQueue(), new TaskDistributor());
			threadPool.submit(new Thread(taskDistributorRunner, "TaskDistributor-" + i));
		}
		threadPool.shutdown();
	}
}
