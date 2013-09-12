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
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.task.exception.ResultException;

/**
 * Result collector
 * 		start specific result service by result type
 * @author yanwei
 * @date 2013-1-22 下午1:41:56
 * 
 */
public class ResultCollector {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollector.class);
	
	private static final String NAMESPACE = "com.softsec.tase.task.result.";
	
	/**
	 * start specific result service by result type
	 * using java reflection
	 * @param result
	 * @throws ResultException
	 */
	public void collect(Result result) throws ResultException {
		
		LOGGER.info("Start collecting result [ " + result.getTaskId() + " ] ...");
		int retValue = -1;
		
		String resultCollectorService = getResultCollectorService(result.getAppType(), result.getJobLifecycle(), result.getResultType());
		
		Class<?> resultCollectorClass = null;
		try {
			resultCollectorClass = Class.forName(resultCollectorService);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("No such class found : " + resultCollectorService + " : " + cnfe.getMessage(), cnfe);
			throw new ResultException("No such class found : " + resultCollectorService + " : " + cnfe.getMessage(), cnfe);
		}
		
		Object resultCollectorObject = null;
		try {
			resultCollectorObject = resultCollectorClass.newInstance();
		} catch (InstantiationException ie) {
			LOGGER.error("Failed to instantiate class [ " + resultCollectorService + " ] : " + ie.getMessage(), ie);
			throw new ResultException("Failed to instantiate class [ " + resultCollectorService + " ] : " + ie.getMessage(), ie);
		} catch (IllegalAccessException iae) {
			LOGGER.error("Failed to access class [ " + resultCollectorService + " ] : " + iae.getMessage(), iae);
			throw new ResultException("Failed to access class [ " + resultCollectorService + " ] : " + iae.getMessage(), iae);
		}
		
		Method resultCollectorMethod = null;
		try {
			resultCollectorMethod = resultCollectorClass.getDeclaredMethod("parse", new Class<?>[]{Result.class});
		} catch (SecurityException se) {
			LOGGER.error("Failed to invoke method [ parse ] : " + se.getMessage(), se);
			throw new ResultException("Failed to invoke method [ parse ] : " + se.getMessage(), se);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error("No such method found [ parse ] : " + nsme.getMessage(), nsme);
			throw new ResultException("No such method found [ parse ] : " + nsme.getMessage(), nsme);
		}
		
		try {
			retValue = (Integer) resultCollectorMethod.invoke(resultCollectorObject, result);
		} catch (IllegalArgumentException iage) {
			LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
			throw new ResultException("Illegal argument used in method invocation : " + iage.getMessage(), iage);
		} catch (IllegalAccessException iace) {
			LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
			throw new ResultException("Failed to access the method : " + iace.getMessage(), iace);
		} catch (InvocationTargetException ite) {
			LOGGER.error("Failed to invoke method correctly : " + ite.getMessage(), ite);
			throw new ResultException("Failed to invoke method correctly : " + ite.getMessage(), ite);
		}
		
		// execute save process only if parse process execute successfully
		if (retValue >= 0) {
			// save method needs no parameter
			Class<?>[] saveParam = {};
			try {
				resultCollectorMethod = resultCollectorClass.getDeclaredMethod("save", saveParam);
			} catch (SecurityException se) {
				LOGGER.error("Failed to invoke method [ save ] " + se.getMessage(), se);
				throw new ResultException("Failed to invoke method [ save ] " + se.getMessage(), se);
			} catch (NoSuchMethodException nsme) {
				LOGGER.error("No such method found [ save ] " + nsme.getMessage(), nsme);
				throw new ResultException("No such method found [ save ] " + nsme.getMessage(), nsme);
			}
			
			try {
				retValue = (Integer) resultCollectorMethod.invoke(resultCollectorObject, (Object[])null);
			} catch (IllegalArgumentException iage) {
				LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
				throw new ResultException("illegal argument used in method invocation : " + iage.getMessage(), iage);
			} catch (IllegalAccessException iace) {
				LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
				throw new ResultException("Failed to access the method : " + iace.getMessage(), iace);
			} catch (InvocationTargetException ite) {
				LOGGER.error("Failed to invoke method correctly : " + ite.getMessage(), ite);
				throw new ResultException("Failed to invoke method correctly : " + ite.getMessage(), ite);
			}
		}
		
		if (retValue < 0) {
			LOGGER.error("Failed to collect result [ " + result.getTaskId() + " ].");
			throw new ResultException("Failed to collect result [ " + result.getTaskId() + " ].");
		}
		
		LOGGER.info("Finished collecting result [ " + result.getTaskId() + " ].");
	}
	
	/**
	 * generate result collector service class name by job type
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 * @throws ResultException
	 */
	public static String getResultCollectorService(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase) {
		
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
		sbuilder.append("Collector");
		
		return sbuilder.toString();
	}
}
