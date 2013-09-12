/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.Observable;

import com.softsec.tase.common.domain.schedule.Job;

/**
 * JobObservable.java
 * @author yanwei
 * @date 2013-3-22 下午1:09:17
 * @description
 */
public class JobObservable extends Observable{

	public void received(Job job) {
		setChanged();
		notifyObservers(job);
	}
}
