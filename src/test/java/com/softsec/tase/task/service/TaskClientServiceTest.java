/**
 * 
 */
package com.softsec.tase.task.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.softsec.tase.common.domain.schedule.Job;
import com.softsec.tase.common.rpc.domain.app.AppType;
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
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.task.pool.JobMapper;
import com.softsec.tase.task.queue.JobObservable;
import com.softsec.tase.task.queue.JobObserver;

/**
 * TaskClientServiceTest
 * <p> </p>
 * @author yanwei
 * @since 2013-9-6 上午8:32:13
 * @version
 */
public class TaskClientServiceTest {

	@Test
	public void testSubmitJob() {
		Job job = new Job();
		job.setCommittedTime(System.currentTimeMillis());
		job.setImpatienceTime(600000);
		job.setJobDistributionMode(JobDistributionMode.SERIAL);
		List<JobOperationRequirement> jobOpList = new ArrayList<JobOperationRequirement>();
		JobOperationRequirement taskOp1 = new JobOperationRequirement();
		taskOp1.setJobLifecycle(JobLifecycle.REINFORCE);
		taskOp1.setJobPhase(JobPhase.GENERATE);
		taskOp1.setJobExecutionMode(JobExecutionMode.EXCLUSIVE);
		taskOp1.setJobReturnMode(JobReturnMode.PASSIVE);
		taskOp1.setTimeout(100000);
		jobOpList.add(taskOp1);
		job.setJobOperationRequirementList(jobOpList);
		List<JobParameter> jobParameterList = new ArrayList<JobParameter>();
		
		JobParameter taskParam1 = new JobParameter();
		taskParam1.setJobPhase(JobPhase.GENERATE);
		List<ContextParameter> contextParamList1 = new ArrayList<ContextParameter>();
		ContextParameter parameter1 = new ContextParameter();
		parameter1.setSequenceNum(1);
		parameter1.setContent("/default/apk/16/c4/16c42f69817f2abb0acdc15a54949dd5.apk");
		parameter1.setNeedDownload(true);
		contextParamList1.add(parameter1);
		ContextParameter parameter2 = new ContextParameter();
		parameter2.setSequenceNum(2);
		parameter2.setContent("target.apk");
		parameter2.setNeedDownload(false);
		contextParamList1.add(parameter2);
		taskParam1.setContextParameterList(contextParamList1);
		jobParameterList.add(taskParam1);
		
		JobParameter taskParam2 = new JobParameter();
		taskParam2.setJobPhase(JobPhase.GENERATE);
		List<ContextParameter> contextParamList2 = new ArrayList<ContextParameter>();
		ContextParameter parameter3 = new ContextParameter();
		parameter3.setSequenceNum(1);
		parameter3.setContent("/default/apk/16/c2/16c2c73c9bddf1a571c98b5b237427e3.apk");
		parameter3.setNeedDownload(true);
		contextParamList2.add(parameter3);
		ContextParameter parameter4 = new ContextParameter();
		parameter4.setSequenceNum(2);
		parameter4.setContent("target.apk");
		parameter4.setNeedDownload(false);
		contextParamList2.add(parameter4);
		taskParam2.setContextParameterList(contextParamList2);
		jobParameterList.add(taskParam2);
		
		job.setJobParameterList(jobParameterList);
		job.setJobPriority(JobPriority.MEDIUM);
		
		List<JobPhase> jobPhaseList = new ArrayList<JobPhase>();
		jobPhaseList.add(JobPhase.GENERATE);
		job.setSubmitterId(110);
		
		JobResourceRequirement jobResc = new JobResourceRequirement();
		jobResc.setJobLifecycle(JobLifecycle.REINFORCE);
		jobResc.setJobPhase(JobPhase.GENERATE);
		jobResc.setClusterType(ClusterType.DEDICATED);
		List<JobResourceRequirement> jobRescList = new ArrayList<JobResourceRequirement>();
		jobRescList.add(jobResc);
		job.setJobResourceRequirementList(jobRescList);
		job.setJobStatus(JobUtils.getJobStatusCode(jobPhaseList.get(0), JobStatus.COMMITTED));
		
		try {
			job.setJobId(JobMapper.getInstance().generateJobId(AppType.APK, JobLifecycle.REINFORCE, JobUtils.getJobPhaseCode(jobPhaseList)));
		} catch (DbUtilsException due) {
			due.printStackTrace();
		}
		
		JobObservable jobObservable = new JobObservable();
		JobObserver jobObserver = new JobObserver();
		jobObservable.addObserver(jobObserver);
		jobObservable.received(job);
		
		JobStorageService jobStorageService = new JobStorageService();
		try {
			jobStorageService.save(job);
		} catch (DbUtilsException due) {
			due.printStackTrace();
		} finally {
			jobStorageService = null;
		}
		
	}
}
