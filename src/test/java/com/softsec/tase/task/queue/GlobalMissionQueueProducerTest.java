/**
 * 
 */
package com.softsec.tase.task.queue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.domain.schedule.Job;

/**
 * GlobalMissionQueueProducerTest.java
 * @author yanwei
 * @date 2013-1-31 下午6:08:59
 * @description
 */
public class GlobalMissionQueueProducerTest extends TestCase {

	@Test
	public void testProduce() {
		PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<Job>();
		Set<Job> missionSet = new HashSet<Job>();
		assertFalse(queue.addAll(missionSet));
		missionSet.add(new Job());
		assertTrue(queue.addAll(missionSet));
	}
}
