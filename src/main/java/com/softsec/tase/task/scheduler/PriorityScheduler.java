package com.softsec.tase.task.scheduler;

import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.task.queue.DistributeTaskQueueProducer;
import com.softsec.tase.task.queue.GlobalTaskQueueProducer;
import com.softsec.tase.task.queue.TaskQueue;

/**
 * 任务调度器 先按任务优先级，再任务按提交时间调度
 * 
 * @author wanghouming & long
 * 
 */

public class PriorityScheduler extends TaskScheduler {

	private static final Logger LOGGER = Logger.getLogger(PriorityScheduler.class);

	/**
	 * start to scheduling
	 */
	@Override
	public void start() {

		LOGGER.info("Priority Scheduler start ... ");
		TaskQueue taskQueueSingleton = TaskQueue.getInstance();
		PriorityBlockingQueue<Task> globalTaskQueue = taskQueueSingleton.getGlobalTaskQueue();

		globalTaskQueueProducerRunner = new GlobalTaskQueueProducer(globalTaskQueue);
		globalTaskQueueProducerThread = new Thread(globalTaskQueueProducerRunner);

		distributeTaskQueueProducerRunner = new DistributeTaskQueueProducer(globalTaskQueue);
		distributeTaskQueueProducerThread = new Thread(distributeTaskQueueProducerRunner);
		
		globalTaskQueueProducerThread.start();
		distributeTaskQueueProducerThread.start();
	}

}
