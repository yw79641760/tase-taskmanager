/**
 * 
 */
package com.softsec.tase.task.scheduler;

import com.softsec.tase.task.queue.DistributeTaskQueueProducer;
import com.softsec.tase.task.queue.GlobalTaskQueueProducer;

/**
 * 任务调度器虚拟类
 * @author yanwei
 * @date 2013-1-6 上午11:20:18
 * 
 */
public abstract class TaskScheduler {

	protected Thread globalTaskQueueProducerThread = null;

	protected Thread distributeTaskQueueProducerThread = null;

	protected GlobalTaskQueueProducer globalTaskQueueProducerRunner = null;

	protected DistributeTaskQueueProducer distributeTaskQueueProducerRunner = null;

	/**
	 * 开始调度
	 */
	public void start() {
	}

	/**
	 * 结束调度
	 */
	public void terminate() {

		if (globalTaskQueueProducerThread != null
				&& globalTaskQueueProducerThread.isAlive()) {
			globalTaskQueueProducerRunner.shutdown();
		}

		if (distributeTaskQueueProducerThread != null
				&& distributeTaskQueueProducerThread.isAlive()) {
			distributeTaskQueueProducerRunner.shutdown();
		}

	}

}
