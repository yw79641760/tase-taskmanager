/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.Observable;

import com.softsec.tase.common.domain.schedule.Task;

/**
 * TaskObservable.java
 * @author yanwei
 * @date 2013-3-27 上午10:12:46
 * @description
 */
public class TaskObservable extends Observable {

	public void received(Task task) {
		setChanged();
		notifyObservers(task);
	}
}
