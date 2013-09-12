/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.softsec.tase.common.rpc.domain.app.AppWeb;

/**
 * AppWebQueue.java
 * @author yanwei
 * @date 2013-3-26 上午10:06:11
 * @description
 */
public class AppWebQueue {

	private ConcurrentLinkedQueue<AppWeb> appWebQueue = new ConcurrentLinkedQueue<AppWeb>();
	
	private static final AppWebQueue appWebQueueSingleton = new AppWebQueue();
	
	public AppWebQueue() {
	}
	
	public static AppWebQueue getInstance() {
		return appWebQueueSingleton;
	}
	
	public synchronized ConcurrentLinkedQueue<AppWeb> getAppWebQueue() {
		return appWebQueue;
	}
	
	public synchronized boolean addToAppWebQueue(AppWeb appWeb) {
		return appWebQueue.add(appWeb);
	}
	
	public synchronized boolean addToAppWebQueue(Set<AppWeb> appWebSet) {
		return appWebQueue.addAll(appWebSet);
	}
}
