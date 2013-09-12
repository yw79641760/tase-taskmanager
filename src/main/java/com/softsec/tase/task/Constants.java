/**
 * 
 */
package com.softsec.tase.task;

/**
 * 常量类
 * @author yanwei
 * @date 2012-12-28 上午9:16:42
 * 
 */
public final class Constants {
	
	/** rpc settings */
	public static final String LISTENER_DOMAIN= "taskmanager.listener.domain";

	public static final String ADMIN_SERVICE_PORT = "admin.service.port";
	
	public static final String TASK_CLIENT_SERVICE_PORT = "task.client.service.port";
	
	public static final String NODE_TRACKER_SERVICE_PORT = "node.tracker.service.port";
	
	public static final String TASK_CLIENT_SERVICE_PUBLISH_URL = "task.client.service.publish.url";

	public static final String MAX_HTTP_THREADS = "max.http.threads";

	public static final String NETWORK_CONNECTION_TIMEOUT = "network.connection.timeout";

	public static final String NETWORK_CONNECTION_RETRY_TIMES = "network.connection.retry.times";
	
	/** task scheduling settings */
	public static final String MAX_QUEUE_SIZE = "max.queue.size";
	
	public static final String DEFAULT_RESOURCE_MATCHER = "default.resource.matcher";
	
	public static final String RESOURCE_REFRESH_INTERVAL = "resource.refresh.interval";
	
	public static final String TASK_DISTRIBUTOR__COUNT = "task.distributor.count";
	
	public static final String RESULT_COLLECTOR_COUNT = "result.collector.count";
	
	public static final String SCHEDULING_FAILURE_RECYCLER_ENABLE = "scheduling.failure.recycler.enable";
	
	public static final String DISTRIBUTION_FAILURE_RECYCLER_ENABLE = "distribution.failure.recycler.enable";

	public static final String EXECUTION_INTERRUPTION_RECYCLER_ENABLE = "execution.interruption.recycler.enable";
	
	public static final String EXECUTION_TIMEOUT_RECYCLER_ENABLE = "execution.timeout.recycler.enable";

	public static final String EXECUTION_FAILURE_RECYCLER_ENABLE = "execution.failure.recycler.enable";
	
	/** FTP settings */
	public static final String FTP_SERVER_URLS = "ftp.server.urls";

	public static final String FTP_APP_REPO = "ftp.app.repo";
	
	public static final String FTP_PROGRAM_REPO = "ftp.program.repo";

	/** Mongodb settings*/
	public static final String MONGODB_URI = "mongodb.uri";

}
