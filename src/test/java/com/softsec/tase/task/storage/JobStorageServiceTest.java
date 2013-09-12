/**
 * 
 */
package com.softsec.tase.task.storage;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobDistributionMode;
import com.softsec.tase.common.rpc.domain.job.JobExecutionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobPriority;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobReturnMode;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.rpc.domain.node.ClusterType;
import com.softsec.tase.common.rpc.domain.node.NodeType;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.task.util.JobSplitter;

/**
 * JobStorageServiceTest.java
 * <p></p>
 * @author yanwei
 * @since 2013-4-18 下午9:04:56
 * @version
 */
public class JobStorageServiceTest extends TestCase {

	@Test
	public void testSaveNewJob() {
		Job job = new Job();
		job.setSubmitterId(10);
		job.setJobId(1000006801123467L);
		job.setJobPriority(JobPriority.MEDIUM);
		job.setJobDistributionMode(JobDistributionMode.SERIAL);
		
		JobOperationRequirement op1 = new JobOperationRequirement();
		op1.setJobPhase(JobPhase.GENERATE);
		op1.setJobExecutionMode(JobExecutionMode.CONCURRENT);
		op1.setJobReturnMode(JobReturnMode.PASSIVE);
		op1.setTimeout(60000);
		JobOperationRequirement op2 = new JobOperationRequirement();
		op2.setJobPhase(JobPhase.ON_STATIC);
		op2.setJobExecutionMode(JobExecutionMode.EXCLUSIVE);
		op2.setJobReturnMode(JobReturnMode.ACTIVE);
		op2.setTimeout(3600000);
		List<JobOperationRequirement> opList = new ArrayList<JobOperationRequirement>();
		opList.add(op1);
		opList.add(op2);
		job.setJobOperationRequirementList(opList);
		
		JobResourceRequirement rsc1 = new JobResourceRequirement();
		rsc1.setJobPhase(JobPhase.GENERATE);
		rsc1.setProgramId(10000);
		rsc1.setNodeType(NodeType.BASIC);
		JobResourceRequirement rsc2 = new JobResourceRequirement();
		rsc2.setJobPhase(JobPhase.ON_STATIC);
		rsc2.setClusterType(ClusterType.DEDICATED);
		List<JobResourceRequirement> rscList = new ArrayList<JobResourceRequirement>();
		rscList.add(rsc1);
		rscList.add(rsc2);
		job.setJobResourceRequirementList(rscList);
		
		List<JobParameter> jobParameterList = new ArrayList<JobParameter>();
		JobParameter param1 = new JobParameter();
		param1.setJobPhase(JobPhase.GENERATE);
		List<ContextParameter> parameterList1 = new ArrayList<ContextParameter>();
		ContextParameter parameter1 = new ContextParameter();
		parameter1.setSequenceNum(1);
		parameter1.setOpt("-f");
		parameter1.setContent("/path/to/source.apk");
		parameter1.setNeedDownload(true);
		parameterList1.add(parameter1);
		ContextParameter parameter2 = new ContextParameter();
		parameter2.setSequenceNum(2);
		parameter2.setOpt("-o");
		parameter2.setContent("target.apk");
		parameter2.setNeedDownload(false);
		parameterList1.add(parameter2);
		param1.setContextParameterList(parameterList1);
		JobParameter param2 = new JobParameter();
		param2.setJobPhase(JobPhase.ON_STATIC);
		List<ContextParameter> parameterList2 = new ArrayList<ContextParameter>();
		ContextParameter parameter11 = new ContextParameter();
		parameter11.setSequenceNum(1);
		parameter11.setOpt("-f");
		parameter11.setContent("/path/to/source.apk");
		parameter11.setNeedDownload(true);
		parameterList2.add(parameter11);
		param2.setContextParameterList(parameterList2);
		jobParameterList.add(param1);
		jobParameterList.add(param2);
		job.setJobParameterList(jobParameterList);
	
		job.setJobStatus(JobUtils.getJobStatusCode(JobPhase.GENERATE, JobStatus.COMMITTED));
		job.setImpatienceTime(3600000);
		job.setCommittedTime(System.currentTimeMillis());
		
		JobStorageService service = new JobStorageService();
		System.out.println(service.save(job));
		System.out.println(job);
		List<Task> taskList = JobSplitter.split(job);
		System.out.println(taskList);
		TaskStorageService taskService = new TaskStorageService();
		System.out.println(taskService.saveAll(taskList));
	}
}
