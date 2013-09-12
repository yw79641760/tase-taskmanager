package com.softsec.tase.task.util.net;

import org.apache.log4j.Logger;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.softsec.tase.common.rpc.service.node.TaskService;

/**
 * PRC 工具类
 * 
 * @author zhangyong & yanwei
 * 
 */
public class RpcUtils {

	private static final Logger LOGGER = Logger.getLogger(RpcUtils.class);

	public static TaskService.Client getReceiver(String ip, int port, int timeout, int retryTimes) throws TTransportException {
		TProtocol protocol = prepare(ip, port, timeout, retryTimes);
		return new TaskService.Client(protocol);
	}

	private static TProtocol prepare(String ip, int port, int timeout, int retryTimes) throws TTransportException {
		TTransport transport = new TSocket(ip, port, timeout);

		boolean opened = false;
		Exception cause = null;
		for (int i = 1; i <= retryTimes; i++) {
			try {
				transport.open();
				opened = true;
				break;
			} catch (TTransportException e) {
				cause = e;
				LOGGER.error("Open connect [" + ip + ":" + port + "] fail retry times : " + i);
			}
		}

		if (!opened) {
			throw new TTransportException("Remote host unreachable.", cause);
		}
		TProtocol protocol = new TBinaryProtocol(transport);
		return protocol;
	}

	/**
	 * 关闭连接
	 * 
	 * @param client
	 */
	public static void close(TServiceClient client) {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
		}
	}

}
