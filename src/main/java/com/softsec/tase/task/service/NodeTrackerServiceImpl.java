/**
 * 
 */
package com.softsec.tase.task.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.softsec.tase.common.domain.Result;
import com.softsec.tase.common.domain.app.App;
import com.softsec.tase.common.domain.app.XmlResultData;
import com.softsec.tase.common.dto.app.FileMetadata;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.rpc.domain.app.AppTransfer;
import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.common.rpc.domain.app.OriginType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.rpc.domain.node.ClusterType;
import com.softsec.tase.common.rpc.domain.node.NodeInfo;
import com.softsec.tase.common.rpc.domain.node.NodePayload;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.NodeTrackerService;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.common.util.domain.AppUtils;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.common.util.domain.TaskUtils;
import com.softsec.tase.store.domain.NodeItem;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.mongo.MongodbService;
import com.softsec.tase.store.service.AppStorageService;
import com.softsec.tase.store.service.FileStorageService;
import com.softsec.tase.store.service.NodeStorageService;
import com.softsec.tase.store.service.TaskStorageService;
import com.softsec.tase.store.util.db.MongodbConnFactory;
import com.softsec.tase.store.util.db.MongodbUtils;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.task.pool.AppMapper;
import com.softsec.tase.task.pool.JobMapper;
import com.softsec.tase.task.pool.NodeMapper;
import com.softsec.tase.task.queue.ResultQueue;
import com.softsec.tase.task.util.app.AppPriceParser;
import com.softsec.tase.task.util.app.AppVersionParser;

/**
 * Node Tracker Service实现类
 * @author yanwei
 * @date 2012-12-27 上午11:33:51
 * 
 */
public class NodeTrackerServiceImpl implements NodeTrackerService.Iface{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeTrackerServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.NodeTrackerService.Iface#registerNode(com.softsec.tase.rpc.domain.node.NodeInfo)
	 */
	@Override
	public int registerNode(NodeInfo nodeInfo) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException {
		
		int retValue = -1;
		
		if (nodeInfo == null) {
			retValue = -1;
			throw new InvalidRequestException("Node info must not be null.");
		}
		
		NodeStorageService nodeStorageService = new NodeStorageService();
		try {
			retValue = nodeStorageService.register(nodeInfo);
		} catch (DbUtilsException de) {
			throw new UnavailableException("Failed to register node : " + nodeInfo.getNodeId() + " : " + de.getMessage());
		} finally {
			nodeStorageService = null;
		}
		
		return retValue;
	}

	/**
	 * node heart beat reporting
	 * @param nodePayload
	 * @return succeed or not
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.NodeTrackerService.Iface#reportHeartbeat(com.softsec.tase.rpc.domain.node.NodePayload)
	 */
	@Override
	public int reportHeartbeat(NodePayload nodePayload)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		int retCode = -1;
		if (nodePayload == null || nodePayload.getNodeRuntime() == null) {
			LOGGER.error("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
			throw new InvalidRequestException("Invalid node payload [ " + nodePayload.getNodeId() + " ].");
		}
		LOGGER.info("Node [ " + nodePayload.getNodeId() + " ] payload info received.");
		NodeItem nodeItem = new NodeItem(nodePayload);
		if (nodeItem.getClusterType().equals(ClusterType.DEDICATED)) {
			NodeMapper.getInstance().updateDedicatedNode(nodeItem);
			LOGGER.info("Dedicated Resource Node Pool Info : " + NodeMapper.getInstance().getDedicatedNodeMap());
		} else if (nodeItem.getClusterType().equals(ClusterType.GENERAL)) {
			NodeMapper.getInstance().updateGeneralNode(nodeItem);
			LOGGER.info("General Resource Node Pool Info : " + NodeMapper.getInstance().getGeneralNodeMap());
		}
		retCode = 0;
		return retCode;
	}

	/**
	 * report task execution status
	 * @param nodeId
	 * @param taskId
	 * @param jobPhase
	 * @param taskStatus
	 * @return succeed or not
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.common.rpc.service.task.NodeTrackerService.Iface#reportTaskExecutionStatus(java.lang.String, long, com.softsec.tase.common.rpc.domain.job.JobPhase, com.softsec.tase.common.rpc.domain.job.JobStatus)
	 */
	@Override
	public int reportTaskExecutionStatus(String nodeId, long taskId, JobPhase jobPhase, JobStatus taskStatus)
			throws InvalidRequestException, UnavailableException, TimeoutException, TException {
		
		int retValue = 0;
		
		if (StringUtils.isEmpty(nodeId)
				|| taskId == 0L
				|| !JobUtils.isJobPhaseMember(jobPhase)
				|| !JobUtils.isJobStatusMember(taskStatus)) {
			
			LOGGER.error("Invalid parameter [ " + nodeId + " : " + taskId + " : " + jobPhase + " : " + taskStatus);
			throw new InvalidRequestException("Invalid parameter [ " + nodeId + " : " + taskId + " : " + jobPhase + " : " + taskStatus);
		}
		
		// update task status in job monitor map
//		JobMapper.getInstance().updateTaskStatus(taskId, taskStatus);
		// update task status in database
		
		TaskStorageService taskStorageService = new TaskStorageService();
		try {
			retValue = taskStorageService.updateTaskStatus(taskId, jobPhase, taskStatus);
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to update task status in database : " + taskId);
			throw new UnavailableException("Failed to update task status in database : " + taskId);
		} finally {
			taskStorageService = null;
		}
		
		return retValue;
	}

	/**
	 * check app duplication
	 * @param appChecksum
	 * @return appId
	 * 			appId if exists, 0 if not exists
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.NodeTrackerService.Iface#checkAppDuplication(java.lang.String)
	 */
	@Override
	public long checkAppDuplication(AppType appType, OriginType originType, String appChecksum)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		if (!StringUtils.isEmpty(appChecksum) && appChecksum.length() == 32) {
			try {
				return new AppStorageService().checkAppDuplication(appType, originType, appChecksum); 
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to get app id of app checksum : " + appChecksum);
				throw new UnavailableException("Failed to get app id of app checksum : " + appChecksum);
			}
		} else {
			LOGGER.error("Invalid app checksum : " + appChecksum);
			throw new InvalidRequestException("Invalid app checksum : " + appChecksum);
		}
	}

	/**
	 * check file duplication
	 * @param fileChecksum
	 * @return fileChecksum
	 * 			 fileChecksum if exists, null if not exists
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.NodeTrackerService.Iface#checkFileDuplication(java.lang.String)
	 */
	@Override
	public String checkFileDuplication(AppType appType, String fileChecksum)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		if (!StringUtils.isEmpty(fileChecksum) && fileChecksum.length() == 32) {
			try {
				return new FileStorageService().checkFileDuplication(appType, fileChecksum); 
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to check file duplication via checksum : " + fileChecksum);
				throw new UnavailableException("Failed to check file duplication via checksum : " + fileChecksum);
			}
		} else {
			LOGGER.error("Invalid file checksum : " + fileChecksum);
			throw new InvalidRequestException("Invalid file checksum : " + fileChecksum);
		}
	}

	/**
	 * transfer data from old database to new database
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.common.rpc.service.task.NodeTrackerService.Iface#transferData(com.softsec.tase.common.rpc.domain.app.AppType, com.softsec.tase.common.rpc.domain.app.AppTransfer, java.nio.ByteBuffer, java.util.List, java.nio.ByteBuffer)
	 */
	@Override
	public int transferData(AppType appType, AppTransfer appInfo,
			ByteBuffer apkInfo, List<ByteBuffer> imageList, ByteBuffer result)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		int retValue = 0;
		
		if (!AppUtils.isAppTypeMember(appType) || appInfo == null) {
			LOGGER.error("Invalid app type or appInfo : " + appType + " : " + appInfo);
			throw new InvalidRequestException("Invalid app type or appInfo : " + appType + " : " + appInfo);
		} else {
			AppWeb appWeb = new AppWeb();
			appWeb.setAppType(appType);
			if (!StringUtils.isEmpty(appInfo.getUrl())) {
				while(appInfo.getUrl().endsWith("\\")) {
					appInfo.setUrl(appInfo.getUrl().substring(0, appInfo.getUrl().length() - 1));
				}
				appWeb.setUrl(appInfo.getUrl().replaceAll("'", " "));
			}
			if (!StringUtils.isEmpty(appInfo.getVersion())) {
				while(appInfo.getVersion().endsWith("\\")) {
					appInfo.setVersion(appInfo.getVersion().substring(0, appInfo.getVersion().length() - 1));
				}
				appWeb.setAppVersion(appInfo.getVersion().replaceAll("'", " "));
			}
			if (appInfo.getWrapperId().equals("Sj91")) {
				appWeb.setStoreName("sj91");
				try {
					appWeb.setAppChecksum(StringUtils.md5Encode(appWeb.getStoreName() + appWeb.getUrl() + appWeb.getAppVersion()));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			} else {
				appWeb.setStoreName(appInfo.getWrapperId());
				appWeb.setAppChecksum(appInfo.getAppChecksum());
			}
			// decide the app 's OriginType	
			if (appWeb.getStoreName().equals("Google Play")
					|| appWeb.getStoreName().equals("AppStore")
					|| appWeb.getStoreName().equals("AppStoreUS")) {
				appWeb.setOriginType(OriginType.OFFICIAL_STORE);
			} else {
				appWeb.setOriginType(OriginType.UNOFFICIAL_STORE);
			}
			appWeb.setStoreDisplayName(appInfo.getWrapperName());
			appWeb.setStoreType(appWeb.getOriginType().getValue());
			appWeb.setCategory(appInfo.getSubject());
			if (!StringUtils.isEmpty(appInfo.getAppName())) {
				while(appInfo.getAppName().endsWith("\\")) {
					appInfo.setAppName(appInfo.getAppName().substring(0, appInfo.getAppName().length() - 1));
				}
				appWeb.setAppName(appInfo.getAppName().replaceAll("'", " "));
			}
			appWeb.setInnerId(appInfo.getDownloadId());
			appWeb.setUpdatedTime(appInfo.getUpdateTime());
			appWeb.setCollectedTime(appInfo.getSavedTime());
			appWeb.setOsType(appInfo.getOs());
			appWeb.setOsVersion(appInfo.getOsVersion());
			appWeb.setCountry(appInfo.getCountry());
			if (!StringUtils.isEmpty(appInfo.getDescription())) {
				while(appInfo.getDescription().endsWith("\\")) {
					appInfo.setDescription(appInfo.getDescription().substring(0, appInfo.getDescription().length() - 1));
				}
				appWeb.setDescription(appInfo.getDescription().replaceAll("'", " "));
			}
			if (!StringUtils.isEmpty(appInfo.getSize())) {
				while(appInfo.getSize().endsWith("\\")) {
					appInfo.setSize(appInfo.getSize().substring(0, appInfo.getSize().length() - 1));
				}
				appInfo.setSize(appInfo.getSize().replaceAll("'", " "));
			}
			appWeb.setDownloadUrl(appInfo.getDownUrl());
			appWeb.setSnapshotUrlList(appInfo.getSnapshotUrlList());
			if (!StringUtils.isEmpty(appInfo.getDeveloper())) {
				while(appInfo.getDeveloper().endsWith("\\")) {
					appInfo.setDeveloper((appInfo.getDeveloper().substring(0, appInfo.getDeveloper().length() - 1)));
				}
				appWeb.setDeveloperName(appInfo.getDeveloper().replaceAll("'", " "));
			}
			appWeb.setDownloadFloor(appInfo.getDownloadNum());
			appWeb.setDevice(appInfo.getDevice());
			appWeb.setPrice(appInfo.getPrice());
			
			App app = new App(appWeb);
			
			if (!StringUtils.isEmpty(appWeb.getAppVersion())) {
				AppVersionParser appVersionParser = new AppVersionParser(app.getAppVersion());
				app.setMajorVersion(appVersionParser.getMajorVersion());
				app.setMinorVersioin(appVersionParser.getMinorVersion());
				app.setRevisionVersion(appVersionParser.getRevisionVersion());
				app.setBuildVersion(appVersionParser.getBuildVersion());
				app.setExtraVersion(appVersionParser.getExtraVersion());
			}
			
			if (!StringUtils.isEmpty(appWeb.getPrice())) {
				AppPriceParser appPriceParser = new AppPriceParser(appWeb.getPrice());
				app.setCurrencyUnit(appPriceParser.getCurrencyUnit());
				app.setPrice(appPriceParser.getPrice());
			}
			
			AppStorageService appStorageService = new AppStorageService();
			FileStorageService fileStorageService = new FileStorageService();
			
			long appId = 0;
			// save app
			try {
				appId = AppMapper.getInstance().saveAppInfo(appType, appWeb.getOriginType(), app.getStoreName(), app.getStoreDisplayName(), 
						app.getStoreUrl(), app.getStoreType(), app.getCategory(), app);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to save app info : [ " + appInfo.getAppChecksum() + " ] : " + due.getMessage());
				throw new UnavailableException("Failed to save app info : [ " + appInfo.getAppChecksum() + " ] : " + due.getMessage());
			} 
			appWeb = null;
			
			Apk apk = null;
			if (appId != 0) {
				// save apk info
				retValue += 1;
				if (apkInfo != null) {
					try {
						apk = (Apk) IOUtils.getObject(apkInfo.array());
					} catch (IOUtilsException ioue) {
						LOGGER.error("Failed to save apk : [ " + appInfo.getAppChecksum() + " ] : " + ioue.getMessage(), ioue);
						throw new UnavailableException("Failed to parse apk : [ " + appInfo.getAppChecksum() + " ] : " + ioue.getMessage());
					}
					if (apkInfo != null) {
						try {
							retValue += fileStorageService.saveApk(appId, apk);
							retValue += appStorageService.updateAppStatus(appId, 0, 1, 0, 0);
							OriginType originType = AppUtils.getOriginTypeByAppId(appId);
							int categoryId = Integer.parseInt(String.valueOf(appId).substring(4, 8));
							retValue += appStorageService.updateAppDownloadedCount(appType, originType, categoryId);
						} catch (DbUtilsException due) {
							LOGGER.error("Failed to save apk : [ " + appInfo.getAppChecksum() + " ] : " + due.getMessage());
							throw new UnavailableException("Failed to save apk : [ " + appInfo.getAppChecksum() + " ] : " + due.getMessage());
						}
					}
				}
				// save image info
				if (imageList != null && imageList.size() != 0) {
					List<FileMetadata> snapshotMetadataList = new ArrayList<FileMetadata>();
					for (int i = 0 ; i < imageList.size() ; i++ ) {
						try {
							snapshotMetadataList.add((FileMetadata) IOUtils.getObject(imageList.get(i).array()));
						} catch (IOUtilsException ioue) {
							LOGGER.error("Failed to parse image metadata list : " + appId + " : " + ioue.getMessage(), ioue);
							throw new UnavailableException("Failed to parse image metadata list : " + appId + " : " + ioue.getMessage());
						}
					}
					if (snapshotMetadataList != null && snapshotMetadataList.size() != 0) {
						try {
							retValue += fileStorageService.saveImage(appId, null, snapshotMetadataList);
							retValue += appStorageService.updateAppStatus(appId, 0, 0, 1, 0);
						} catch (DbUtilsException due) {
							LOGGER.error("Failed to save image metadata list : " + appId + " : " + due.getMessage(), due);
							throw new UnavailableException("Failed to save image metadata list : " + appId + " : " + due.getMessage());
						} finally {
							snapshotMetadataList = null;
						}
					}
				}
				// save android static detection report
				if (result != null) {
					XmlResultData androidStaticResult = null;
					try {
						androidStaticResult = (XmlResultData) IOUtils.getObject(result.array());
					} catch (IOUtilsException ioue) {
						LOGGER.error("Failed to parse android static result : " + appId + " : " + ioue.getMessage(), ioue);
						throw new UnavailableException("Failed to parse android static result : " + appId + " : " + ioue.getMessage());
					}
					DBCollection collection = MongodbConnFactory.getCollection("android.static.result");
					MongodbService mongodbService = new MongodbService();
					LOGGER.info("App Checksum : " + appInfo.getAppChecksum());
					mongodbService.insertItem(collection, MongodbUtils.beanToDBObj(androidStaticResult));
					if (appId != 0 && apk != null) {
						fileStorageService.updateFileStatus(appId, apk.getBundleChecksum(), apk.getFileMetadata().getFileChecksum(), 1, 0);
					}
					retValue += 2;
				}
			}
			apk = null;
			appStorageService = null;
			fileStorageService = null;
		}
		return retValue;
	}

	/**
	 * submit result
	 * @param appType
	 * @param jobLifecycle
	 * @param resultType
	 * @param content
	 * @param md5
	 * @param taskId
	 * @param identifier
	 * @return succeed or not
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.common.rpc.service.task.NodeTrackerService.Iface#submitResult(com.softsec.tase.common.rpc.domain.app.AppType, com.softsec.tase.common.rpc.domain.job.JobLifecycle, com.softsec.tase.common.rpc.domain.job.JobPhase, java.nio.ByteBuffer, java.lang.String, long, java.lang.String)
	 */
	@Override
	public int submitResult(AppType appType, JobLifecycle jobLifecycle,
			JobPhase resultType, ByteBuffer content, String resultChecksum,
			long taskId, String identifier) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException {
		
		int retValue = -1;
		
		if (!AppUtils.isAppTypeMember(appType)
				|| !JobUtils.isJobLifecycleMember(jobLifecycle)
				|| !JobUtils.isJobPhaseMember(resultType)) {
			
			LOGGER.error("Invalid result type : " + resultType.name());
			throw new InvalidRequestException("Invalid result type : " + resultType.name());
		}
		
		TaskStorageService taskStorageService = new TaskStorageService();
		String actualMd5 = null;
		Result result = null;
		if (content.hasArray()) {
			try {
				actualMd5 = IOUtils.getByteArrayMd5(content.array());
			} catch (IOUtilsException ioue) {
				LOGGER.error("Failed to get result content md5 : " + actualMd5 + " : " + ioue.getMessage(), ioue);
				throw new UnavailableException("Failed to get result content md5 : " + actualMd5 + " : " + ioue.getMessage());
			}
			if (StringUtils.isEmpty(resultChecksum)
					|| StringUtils.isEmpty(actualMd5) 
					|| !actualMd5.equals(resultChecksum)) {
				
				// update database
				try {
					taskStorageService.updateTaskStatus(taskId, resultType, JobStatus.FAILURE);
				} catch (DbUtilsException due) {
					LOGGER.error("Failed to update task [ " + taskId
							+ " ] status to [ " + JobStatus.FAILURE + " ].", due);
					throw new UnavailableException("Failed to update task [ " + taskId
							+ " ] status to [ " + JobStatus.FAILURE + " ].");
				}
				
			} else {
				result = new Result();
				result.setTaskId(taskId);
				result.setAppType(TaskUtils.getAppType(taskId));
				result.setJobLifecycle(TaskUtils.getJobLifecycle(taskId));
				result.setResultType(resultType);
				result.setContent(content);
				result.setMd5(resultChecksum);
				result.setIdentifier(identifier);
				if (ResultQueue.getInstance().addToResultQueue(result)) {
					retValue = 0;
				}
			}
		}
		return retValue;
	}

}
