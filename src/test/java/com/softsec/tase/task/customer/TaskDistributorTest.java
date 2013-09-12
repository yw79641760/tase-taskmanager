/**
 * 
 */
package com.softsec.tase.task.customer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.container.BundleType;
import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobExecutionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobPriority;
import com.softsec.tase.common.rpc.domain.job.JobReturnMode;
import com.softsec.tase.task.exception.DistributionException;

/**
 * TaskDistributorTest
 * <p> </p>
 * @author yanwei
 * @since 2013-9-5 下午7:30:13
 * @version
 */
public class TaskDistributorTest extends TestCase {

	@Test
	public void testDistributeTask() {
		Context context = new Context();
		Task task = new Task();
		task.setTaskId(110000680112345601L);
		task.setTaskPriority(JobPriority.MEDIUM);
		task.setExecutorId("192.168.2.90:7000");
		task.setJobPhase(JobPhase.GENERATE);
		task.setLoadedTime(System.currentTimeMillis());
		JobOperationRequirement taskOperationRequirement = new JobOperationRequirement();
		taskOperationRequirement.setJobLifecycle(JobLifecycle.REINFORCE);
		taskOperationRequirement.setJobPhase(JobPhase.GENERATE);
		taskOperationRequirement.setJobExecutionMode(JobExecutionMode.EXCLUSIVE);
		taskOperationRequirement.setJobReturnMode(JobReturnMode.PASSIVE);
		taskOperationRequirement.setTimeout(600000);
		task.setTaskOperationRequirement(taskOperationRequirement);
		JobParameter taskParameter = new JobParameter();
		taskParameter.setJobPhase(JobPhase.GENERATE);
		List<ContextParameter> contextParameterList = new ArrayList<ContextParameter>();
		ContextParameter parameter1 = new ContextParameter();
		parameter1.setSequenceNum(1);
		parameter1.setContent("/default/apk/16/c4/16c42f69817f2abb0acdc15a54949dd5.apk");
		parameter1.setNeedDownload(true);
		contextParameterList.add(parameter1);
		ContextParameter parameter2 = new ContextParameter();
		parameter2.setSequenceNum(2);
		parameter2.setContent("./target.apk");
		parameter2.setNeedDownload(false);
		contextParameterList.add(parameter2);
		taskParameter.setContextParameterList(contextParameterList);
		context.setParameter(taskParameter);
		context.setBundleType(BundleType.ZIP);
		context.setTaskId(task.getTaskId());
		context.setPriority(task.getTaskPriority());
		context.setJobExecutionMode(task.getTaskOperationRequirement().getJobExecutionMode());
		context.setJobReturnMode(task.getTaskOperationRequirement().getJobReturnMode());
		context.setProgramId(10000L);
		context.setProgramName("test");
		context.setScriptName("test");
		context.setScriptPath("test");
		context.setScriptMd5("test");
		context.setExecutableName("test");
		context.setExecutablePath("test");
		context.setExecutableMd5("test");
		
		TaskDistributor distributor = new TaskDistributor();
		try {
			distributor.distribute(task.getExecutorId(), context);
		} catch (DistributionException de) {
			de.printStackTrace();
		}
	}
}
