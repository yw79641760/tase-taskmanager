package com.softsec.tase.task.customer;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.schedule.Task;
import com.softsec.tase.common.rpc.domain.container.BundleType;
import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.JobExecutionMode;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.node.TaskService;
import com.softsec.tase.store.domain.ProgramItem;
import com.softsec.tase.task.Configuration;
import com.softsec.tase.task.Constants;
import com.softsec.tase.task.exception.DistributionException;
import com.softsec.tase.task.pool.ProgramMapper;
import com.softsec.tase.task.util.net.RpcUtils;

/**
 * mission distribution 
 * @author yanwei
 * 
 */
public class TaskDistributor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDistributor.class);

	/**
	 * distribute single mission
	 * @param task
	 * @throws DistributionException
	 */
	public void distributeToNode(Task task) throws DistributionException{
		Context context = getContext(task);
		if (context != null) {
			distribute(task.getExecutorId().trim(), context);
		} else {
//			LOGGER.error("Failed to get context of task [ " + task.getTaskId() + " ].");
//			throw new DistributionException("Failed to get context of task [ " + task.getTaskId() + " ].");
		}
	}

	/**
	 * distribute mission list
	 * @param taskList
	 * @throws DistributionException
	 */
	public void distributeToNode(List<Task> taskList) throws DistributionException {
		for (Task task : taskList) {
			distributeToNode(task);
		}
	}

	/**
	 * task distribution implementation
	 * @param executorId
	 * @param context
	 * @throws DistributionException
	 */
	public void distribute(String executorId, Context context) throws DistributionException {
		
		TaskService.Client receiver = null;
		String[] nodeInfo = executorId.split(":");
		String domain = nodeInfo[0].trim();
		String port = nodeInfo[1].trim();
		
		int timeout = Configuration.getInt(Constants.NETWORK_CONNECTION_TIMEOUT, 5000);
		int retryTimes = Configuration.getInt(Constants.NETWORK_CONNECTION_RETRY_TIMES, 10);
		
		try {
			receiver = RpcUtils.getReceiver(domain, Integer.parseInt(port), timeout, retryTimes);
		} catch (NumberFormatException nfe) {
			LOGGER.error("Invalid RPC parameter number : " + nfe.getMessage(), nfe);
			throw new DistributionException("Invalid RPC parameter number : " + nfe.getMessage(), nfe);
		} catch (TTransportException tte) {
			LOGGER.error("Failed to establish RPC connection : " + tte.getMessage(), tte);
			throw new DistributionException("Failed to establish RPC connection : " + tte.getMessage(), tte);
		}

		int retCode = -1;
		try {
			retCode = receiver.submitContext(context);
		} catch (InvalidRequestException ire) {
			LOGGER.error("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + ire.getMessage(), ire);
			throw new DistributionException("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + ire.getMessage(), ire);
		} catch (UnavailableException ue) {
			LOGGER.error("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + ue.getMessage(), ue);
			throw new DistributionException("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + ue.getMessage(), ue);
		} catch (TimeoutException te) {
			LOGGER.error("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + te.getMessage(), te);
			throw new DistributionException("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + te.getMessage(), te);
		} catch (TException te) {
			LOGGER.error("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + te.getMessage(), te);
			throw new DistributionException("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to [ " + executorId + " ] : " + te.getMessage(), te);
		}
		
		if (retCode == 0) {
			LOGGER.info("Succeed to distribute task [ " + context.getTaskId() 
					+ " ] to Node [ " + executorId + " ].");
		} else {
			LOGGER.error("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to Node [ " + executorId + " ] with error code : " + retCode);
			throw new DistributionException("Failed to distribute task [ " + context.getTaskId() 
					+ " ] to Node [ " + executorId + " ] with error code : " + retCode);
		}
		RpcUtils.close(receiver);
	}
	
	/**
	 * get context by task
	 * @param taskId
	 * @param programId
	 * @return
	 */
	private Context getContext(Task task){
		
		Context context = null;
		ProgramItem programItem = ProgramMapper.getInstance().getProgramItem(task.getProgramId());
		if (programItem == null) {
			return context;
		}
		context = new Context();
		context.setTaskId(task.getTaskId());
		if (task.getTaskOperationRequirement() != null) {
			context.setJobExecutionMode(task.getTaskOperationRequirement().getJobExecutionMode());
			context.setJobReturnMode(task.getTaskOperationRequirement().getJobReturnMode());
			context.setTimeout(task.getTaskOperationRequirement().getTimeout());
		}
		context.setProgramId(task.getProgramId());
		context.setPriority(task.getTaskPriority());
		
		context.setProgramName(programItem.getProgramName());
		// TODO context bundle type ZIP or JAR
		context.setScriptName(programItem.getScriptName());
		context.setScriptPath(programItem.getScriptPath());
		context.setScriptMd5(programItem.getScriptMd5());
		context.setExecutableName(programItem.getExecutableName());
		context.setExecutablePath(programItem.getExecutablePath());
		context.setExecutableMd5(programItem.getExecutableMd5());
		context.setEnvVariables(programItem.getEnvVariables());
		
		context.setParameter(task.getTaskParameter());
		
		// FIXME test data here
		context.setBundleType(BundleType.ZIP);
		context.setEnvVariables("env=jar");
		context.setExecutableMd5("test");
		context.setExecutableName("test");
		context.setExecutablePath("text");
		context.setJobExecutionMode(JobExecutionMode.EXCLUSIVE);
		context.setJobPhase(task.getJobPhase());
		context.setProgramId(1100000301L);
		context.setProgramName("test");
		context.setScriptMd5("test");
		context.setScriptName("test");
		context.setScriptPath("test");
		
		// TODO context result address
		return context;
	}
}
