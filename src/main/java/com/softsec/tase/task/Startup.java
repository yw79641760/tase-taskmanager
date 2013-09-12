/**
 * 
 */
package com.softsec.tase.task;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.service.task.AdminService;
import com.softsec.tase.common.rpc.service.task.NodeTrackerService;
import com.softsec.tase.common.rpc.service.task.TaskClientService;
import com.softsec.tase.task.customer.ResultCollectorCustomer;
import com.softsec.tase.task.customer.TaskDistributorCustomer;
import com.softsec.tase.task.pool.AppMapperProducer;
import com.softsec.tase.task.pool.JobMapperProducer;
import com.softsec.tase.task.pool.NodeMapperProducer;
import com.softsec.tase.task.pool.ProgramMapperProducer;
import com.softsec.tase.task.pool.UserMapperProducer;
import com.softsec.tase.task.scheduler.PriorityScheduler;
import com.softsec.tase.task.scheduler.TaskScheduler;
import com.softsec.tase.task.service.AdminServiceImpl;
import com.softsec.tase.task.service.NodeTrackerServiceImpl;
import com.softsec.tase.task.service.TaskClientServiceImpl;

/**
 * 
 * @author yanwei
 * @date 2013-1-11 上午10:09:19
 * 
 */
public class Startup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Startup.class);
	
	private int adminPort;
	
	private int taskClientPort;
	
	private int nodeTrackerPort;
	
	public Startup () {
		taskClientPort = Configuration.getInt(Constants.TASK_CLIENT_SERVICE_PORT, 6000);
		adminPort = Configuration.getInt(Constants.ADMIN_SERVICE_PORT, 6010);
		nodeTrackerPort = Configuration.getInt(Constants.NODE_TRACKER_SERVICE_PORT, 6020);
	}
	
	/**
	 * initiate resource pool
	 */
	public void startupResourcePool() {
		new Thread(new AppMapperProducer()).start();
		new Thread(new JobMapperProducer()).start();
		new Thread(new ProgramMapperProducer()).start();
		new Thread(new UserMapperProducer()).start();
	}
	
	/**
	 *  startup task scheduler and quartz jobs
	 */
	public void startupScheduler() {
		// task scheduler startup
		TaskScheduler taskScheduler = new PriorityScheduler();
		taskScheduler.start();
		
		// quartz jobs startup
		StdSchedulerFactory factory = new StdSchedulerFactory();
		Scheduler quartz = null;
		try {
			quartz = factory.getScheduler();
			quartz.start();
		} catch (SchedulerException se) {
			LOGGER.error("Failed to startup quartz scheduler : " + se.getMessage(), se);
			throw new RuntimeException("Failed to startup quartz scheduler : " + se.getMessage(), se);
		}
	}
	
	/**
	 *  startup admin service listener
	 */
	public void startupAdminService() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startupAdminServiceListener();
			}

			private void startupAdminServiceListener() {
				try {
					final TServerTransport serverTransport 
						= new TServerSocket(adminPort);
					final AdminService.Processor<AdminService.Iface> processor 
						= new AdminService.Processor<AdminService.Iface>(new AdminServiceImpl());
					final TServer server 
						= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Admin Server start listening on port : [ " + adminPort + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup admin service listener at port : " + adminPort + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup admin service listener at port : " + adminPort + " : " + tte.getMessage(), tte);
				}
			}
			
		}).start();
	}
	
	/**
	 *  startup client service listener
	 */
	public void startupTaskClientService() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startupTaskClientServiceListener();
			}

			private void startupTaskClientServiceListener() {
				try {
					final TServerTransport serverTransport 
						= new TServerSocket(taskClientPort);
					final TaskClientService.Processor<TaskClientService.Iface> processor 
						= new TaskClientService.Processor<TaskClientService.Iface>(new TaskClientServiceImpl());
					final TServer server 
						= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Task Client Server start listening on port : [ " + taskClientPort + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup client service at port : " + taskClientPort + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup client service at port : " + taskClientPort + " : " + tte.getMessage(), tte);
				}
			}
			
		}).start();
	}
	
	/**
	 *  startup node tracker service listener
	 */
	public void startupNodeTrackerService() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startupNodeTrackerServiceListener();
			}

			private void startupNodeTrackerServiceListener() {
				try {
					final TServerTransport serverTransport 
						= new TServerSocket(nodeTrackerPort);
					final NodeTrackerService.Processor<NodeTrackerService.Iface> processor 
						= new NodeTrackerService.Processor<NodeTrackerService.Iface>(new NodeTrackerServiceImpl());
					final TServer server 
						= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Node Tracker Server start listening on port : [ " + nodeTrackerPort + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup node tracker service at port : " + nodeTrackerPort + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup node tracker service at port : " + nodeTrackerPort + " : " + tte.getMessage(), tte);
				}
			}
			
		}).start();
	}
	
	/**
	 * task distribution thread pool initialization
	 */
	public void startupCustomers() {
		TaskDistributorCustomer taskDistributorCustomer = new TaskDistributorCustomer();
		taskDistributorCustomer.start();
		ResultCollectorCustomer resultCollectorCustomer = new ResultCollectorCustomer();
		resultCollectorCustomer.start();
	}
	
	public void startupTaskManager() {
		new NodeMapperProducer().initMasterId();
//		new Thread(new AppBundleChecksumRevisionService()).start();
		startupResourcePool();
		startupCustomers();
		startupAdminService();
		startupTaskClientService();
		startupNodeTrackerService();
		startupScheduler();
	}
	

	/**
	 * @param
	 */
	public static void main(String[] args) {
		Startup startup = new Startup();
		startup.startupTaskManager();
	}

}
