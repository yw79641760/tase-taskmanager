/**
 * 
 */
package com.softsec.tase.task.customer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobDistributionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.JobStorageService;
import com.softsec.tase.task.exception.ReloadException;

/**
 * TaskReloader.java
 * @author yanwei
 * @date 2013-3-27 下午4:10:08
 * @description
 */
public class TaskReloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReloader.class);
	
	private static final String NAMESPACE = "com.softsec.tase.task.result.";
	
	/**
	 * reload task into global task queue from result
	 * using java reflection
	 * @param result
	 * @throws ReloadException
	 */
	public void reload(Result result) throws ReloadException {
		
		LOGGER.info("Start reloading task [ " + result.getTaskId() + " ] from result ...");
		
		// decide whether task should be reloaded
		if (JobUtils.getNextJobPhase(result.getTaskId(), result.getResultType()) == null) {
			LOGGER.info("Task [ " + result.getTaskId() + " ] has finished the last phase.");
			return;
		} else {
			JobStorageService jobStorageService = new JobStorageService();
			JobDistributionMode jobDistributionMode = null;
			try {
				jobDistributionMode = jobStorageService.getJobDistributionModeByJobId(result.getTaskId() / 100);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to get job distribution mode from task [ " + result.getTaskId() + " ] : " + due.getMessage(), due);
				
			}
			// if failed to get job distribution mode or the mode is PARALLEL, then ignore reload process
			if (jobDistributionMode == null || jobDistributionMode.equals(JobDistributionMode.PARALLEL)) {
				LOGGER.info("Task [ " + result.getTaskId() + " ] is not serial distributed task.");
				return;
			}
		}
		
		String taskReloaderService = getTaskReloaderService(result.getAppType(), result.getJobLifecycle(), result.getResultType());
		
		Class<?> taskReloaderClass = null;
		try {
			taskReloaderClass = Class.forName(taskReloaderService);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("No such class found : " + taskReloaderService + " : " + cnfe.getMessage(), cnfe);
			throw new ReloadException("No such class found : " + taskReloaderService + " : " + cnfe.getMessage(), cnfe);
		}
		
		Object taskReloaderObject = null;
		try {
			taskReloaderObject = taskReloaderClass.newInstance();
		} catch (InstantiationException ie) {
			LOGGER.error("Failed to instantiate class : " + taskReloaderService + " : " + ie.getMessage(), ie);
			throw new ReloadException("Failed to instantiate class : " + taskReloaderService + " : " + ie.getMessage(), ie);
		} catch (IllegalAccessException iae) {
			LOGGER.error("Failed to access class : " + taskReloaderService + " : " + iae.getMessage(), iae);
			throw new ReloadException("Failed to access class : " + taskReloaderService + " : " + iae.getMessage(), iae);
		}
		
		Method taskReloaderMethod = null;
		try {
			taskReloaderMethod = taskReloaderClass.getDeclaredMethod("reloadRequirement", new Class<?>[]{Long.class, JobPhase.class});
		} catch (SecurityException se) {
			LOGGER.error("Failed to invoke method [ reloadRequirement ] : " + se.getMessage() ,se);
			throw new ReloadException("Failed to invoke method [ reloadRequirement ] : " + se.getMessage() ,se);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error("No such method found [ reloadRequirement ] : " + nsme.getMessage(), nsme);
			throw new ReloadException("No such method found [ reloadRequirement ] : " + nsme.getMessage(), nsme);
		}
		
		try {
			taskReloaderMethod.invoke(taskReloaderObject, result.getTaskId(), result.getResultType());
		} catch (IllegalArgumentException iage) {
			LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
			throw new ReloadException("Illegal argument used in method invocation : " + iage.getMessage(), iage);
		} catch (IllegalAccessException iace) {
			LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
			throw new ReloadException("Failed to access the method : " + iace.getMessage(), iace);
		} catch (InvocationTargetException ite) {
			LOGGER.error("Failed to invoke the method correctly : " + ite.getMessage(), ite);
			throw new ReloadException("Failed to invoke the method correctly : " + ite.getMessage(), ite);
		}
		
		try {
			taskReloaderMethod = taskReloaderClass.getDeclaredMethod("reloadParameter", new Class<?>[]{Result.class});
		} catch (SecurityException se) {
			LOGGER.error("Failed to invoke method [ reloadParameter ] : " + se.getMessage(), se);
			throw new ReloadException("Failed to invoke method [ reloadParameter ] : " + se.getMessage(), se);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error("No such method found [ reloadParameter ] : " + nsme.getMessage(), nsme);
			throw new ReloadException("No such method found [ reloadParameter ] : " + nsme.getMessage(), nsme);
		}
		
		try {
			taskReloaderMethod.invoke(taskReloaderObject, result);
		} catch (IllegalArgumentException iage) {
			LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
			throw new ReloadException("Illegal argument used in method invocation : " + iage.getMessage(), iage);
		} catch (IllegalAccessException iace) {
			LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
			throw new ReloadException("Failed to access the method : " + iace.getMessage(), iace);
		} catch (InvocationTargetException ite) {
			LOGGER.error("Failed to invoke the method correctly : " + ite.getMessage(), ite);
			throw new ReloadException("Failed to invoke the method correctly : " + ite.getMessage(), ite);
		}
		
		try {
			taskReloaderMethod = taskReloaderClass.getDeclaredMethod("reloadTask", new Class<?>[]{});
		} catch (SecurityException se) {
			LOGGER.error("Failed to invoke method [ reloadTask ] : " + se.getMessage(), se);
			throw new ReloadException("Failed to invoke method [ reloadTask ] : " + se.getMessage(), se);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error("No such method found [ reloadTask ] : " + nsme.getMessage(), nsme);
			throw new ReloadException("No such method found [ reloadTask ] : " + nsme.getMessage(), nsme);
		}
		
		try {
			taskReloaderMethod.invoke(taskReloaderObject, (Object[]) null);
		} catch (IllegalArgumentException iage) {
			LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
			throw new ReloadException("Illegal argument used in method invocation : " + iage.getMessage(), iage);
		} catch (IllegalAccessException iace) {
			LOGGER.error("Failed to access the method : " + iace.getMessage() ,iace);
			throw new ReloadException("Failed to access the method : " + iace.getMessage() ,iace);
		} catch (InvocationTargetException ite) {
			LOGGER.error("Failed to invoke the method correctly : " + ite.getMessage(), ite);
			throw new ReloadException("Failed to invoke the method correctly : " + ite.getMessage(), ite);
		}
	}
	
	/**
	 * generate task reload service class name by job type
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 */
	public static String getTaskReloaderService(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase) {
		
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(NAMESPACE);
		
		if (appType != null) {
			String appTypeStr = appType.name().toLowerCase();
			sbuilder.append(appTypeStr.substring(0, 1).toUpperCase());
			sbuilder.append(appTypeStr.substring(1));
		}
		
		if (jobLifecycle != null) {
			String jobLifecycleStr = jobLifecycle.name().toLowerCase();
			sbuilder.append(jobLifecycleStr.substring(0, 1).toUpperCase());
			sbuilder.append(jobLifecycleStr.substring(1));
		}
		
		if (jobPhase != null) {
			String jobPhaseStr = jobPhase.name().toLowerCase();
			sbuilder.append(jobPhaseStr.substring(0, 1).toUpperCase());
			sbuilder.append(jobPhaseStr.substring(1));
		}
		sbuilder.append("Reloader");
		
		return sbuilder.toString();
	}
}
