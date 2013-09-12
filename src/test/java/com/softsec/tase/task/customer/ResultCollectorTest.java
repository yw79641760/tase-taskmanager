/**
 * 
 */
package com.softsec.tase.task.customer;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;

/**
 * ResultCollectorTest.java
 * @author yanwei
 * @date 2013-3-26 下午10:09:59
 * @description
 */
public class ResultCollectorTest extends TestCase {

	@Test
	public void testGetResultService() {
		
		System.out.println(ResultCollector.getResultCollectorService(AppType.COMMON, JobLifecycle.ANALYSIS, JobPhase.INITIALIZE));
	}
}
