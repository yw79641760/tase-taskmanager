/**
 * 
 */
package com.softsec.tase.task.pool;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.softsec.tase.common.domain.app.App;
import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.app.OriginType;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.AppStorageService;

/**
 * AppCategoryMapper.java
 * @author yanwei
 * @date 2013-3-12 上午10:27:59
 * @description
 */
public class AppMapper {
	
	/**
	 * Map<Store, Map<Category, CategoryId>>
	 */
	private Map<String, Map<String, Integer>> appCategoryMap 
		= new ConcurrentHashMap<String, Map<String, Integer>>();
	
	/**
	 * Map<AppType, Map<OriginType, Map<CategoryId, CollectedCount>>>
	 */
	private Map<AppType, Map<OriginType, Map<Integer, AtomicInteger>>> appCountMap 
		= new ConcurrentHashMap<AppType, Map<OriginType, Map<Integer, AtomicInteger>>>();

	private static final AppMapper appMapper = new AppMapper();
	
	public AppMapper() {
	}
	
	public static AppMapper getInstance() {
		return appMapper;
	}
	
	public synchronized Map<String, Map<String, Integer>> getAppCategoryMap() {
		return appCategoryMap;
	}
	
	public synchronized Map<AppType, Map<OriginType, Map<Integer, AtomicInteger>>> getAppCountMap() {
		return appCountMap;
	}
	
	/**
	 * init new store
	 * @param storeName
	 */
	public synchronized void initAppCategoryMapWithStore(String storeName) {
		if (appCategoryMap.get(storeName) == null) {
			appCategoryMap.put(storeName, new ConcurrentHashMap<String, Integer>());
		}
	}
	
	/**
	 * init new category
	 * @param storeName
	 * @param category
	 */
	public synchronized void initAppCategoryMapWithCategory(String storeName, String category, int categoryId) {
		initAppCategoryMapWithStore(storeName);
		if (appCategoryMap.get(storeName).get(category) == null) {
			Map<String, Integer> newCategoryMap = appCategoryMap.get(storeName);
			newCategoryMap.put(category, categoryId);
			appCategoryMap.put(storeName, newCategoryMap);
		}
	}
	
	/**
	 * add new store name or new category into mapper and database
	 * @param storeName
	 * @param category
	 * @throws DbUtilsException
	 */
	public synchronized void addAppCategoryMapWithStoreOrCategory(AppType appType, OriginType originType, String storeName, 
			String storeDisplayName, String storeUrl, int storeType, String category, int masterId) 
		throws DbUtilsException {
		
		Map<String, Integer> categoryMap = appCategoryMap.get(storeName);
		AppStorageService appStorageService = new AppStorageService();
		
		// if no such store exists
		// then insert store and category
		if (categoryMap == null) {
			
			// add new store and category into database
			appStorageService.addStore(appType, originType, storeName, storeDisplayName, storeUrl, storeType);
			appStorageService.addCategory(appType, originType, storeName, category, masterId);
			// fetch new category id
			// add new store and category into map
			int categoryId = appStorageService.getCategoryId(appType, originType, storeName, category, masterId);
			if (categoryId != 0) {
				initAppCategoryMapWithCategory(storeName, category, categoryId);
				initAppCountMapWithCategoryCount(appType, originType, categoryId, 0);
			}
			
		} else if (categoryMap.get(category) == null){
			// if store exists but no such category exists
			
			// then insert new category into database
			appStorageService.addCategory(appType, originType, storeName, category, masterId);
			// fetch new category id
			// then insert new category into map
			int categoryId = appStorageService.getCategoryId(appType, originType, storeName, category, masterId);
			if (categoryId != 0) {
				initAppCategoryMapWithCategory(storeName, category, categoryId);
				initAppCountMapWithCategoryCount(appType, originType, categoryId, 0);
			}
		}
		appStorageService = null;
	}
	
	/**
	 * get category id by store name and category
	 * @param storeName
	 * @param category
	 * @return
	 * @throws DbUtilsException 
	 */
	public synchronized int getCategoryId(AppType appType, OriginType originType, 
			String storeName, String storeDisplayName, String storeUrl, 
			int storeType, String category, int masterId) throws DbUtilsException {
		addAppCategoryMapWithStoreOrCategory(appType, originType, storeName, storeDisplayName, 
				storeUrl, storeType, category, masterId);
		return appCategoryMap.get(storeName).get(category);
	}
	/**
	 * init app type
	 * @param appType
	 */
	public synchronized void initAppCountMapWithAppType(AppType appType) {
		if (appCountMap.get(appType) == null) {
			appCountMap.put(appType, new ConcurrentHashMap<OriginType, Map<Integer, AtomicInteger>>());
		}
	}
	/**
	 * init origin type
	 * @param appType
	 * @param originType
	 */
	public synchronized void initAppCountMapWithOriginType(AppType appType, OriginType originType) {
		initAppCountMapWithAppType(appType);
		if (appCountMap.get(appType).get(originType) == null) {
			Map<OriginType, Map<Integer, AtomicInteger>> newOriginTypeMap = appCountMap.get(appType);
			newOriginTypeMap.put(originType, new ConcurrentHashMap<Integer, AtomicInteger>());
			appCountMap.put(appType, newOriginTypeMap);
		}
	}
	/**
	 * init category count
	 * @param appType
	 * @param originType
	 * @param categoryId
	 * @param categoryCount
	 */
	public synchronized void initAppCountMapWithCategoryCount(AppType appType, OriginType originType, int categoryId, int categoryCount) {
		initAppCountMapWithOriginType(appType, originType);
		if (appCountMap.get(appType).get(originType).get(categoryId) == null) {
			Map<OriginType, Map<Integer, AtomicInteger>> newOriginTypeMap = appCountMap.get(appType);
			Map<Integer, AtomicInteger> newCategoryIdMap = appCountMap.get(appType).get(originType);
			newCategoryIdMap.put(categoryId, new AtomicInteger(categoryCount));
			newOriginTypeMap.put(originType, newCategoryIdMap);
			appCountMap.put(appType, newOriginTypeMap);
		}
	}
	
	/**
	 * increase and get app count by origin type and category id
	 * @param appType
	 * @param originType
	 * @param categoryId
	 * @return
	 */
	public synchronized int increaseAndGetAppCount(AppType appType, OriginType originType, int categoryId) {
		return appCountMap.get(appType).get(originType).get(categoryId).incrementAndGet();
	}
	/**
	 * get app count 
	 * @param appType
	 * @param originType
	 * @param categoryId
	 * @return
	 */
	public synchronized int getAppCount(AppType appType, OriginType originType, int categoryId) {
		return appCountMap.get(appType).get(originType).get(categoryId).get();
	}
	/**
	 * backtrack app count for failed in saving new app
	 * @param appType
	 * @param originType
	 * @param categoryId
	 * @return
	 */
	public synchronized int backtrackAppCount(AppType appType, OriginType originType, int categoryId) {
		return appCountMap.get(appType).get(originType).get(categoryId).decrementAndGet();
	}
	
	/**
	 * generate new app id
	 * <p>APP ID FORMAT</p>
	 * |AppType(1 digit) | OriginType(1 digit) | MasterId(2 digits) | CategoryId/UserId(4 digits) | AppCount(8 digits) |
	 * @param appType
	 * @param originType
	 * @param storeName
	 * @param category
	 * @return
	 */
	public synchronized Long generateAppId(AppType appType, OriginType originType, String storeName, 
			String storeDisplayName, String storeUrl, int storeType, String category) 
			throws DbUtilsException {
		
		StringBuilder sbuilder = new StringBuilder();
		
		sbuilder.append(appType.getValue());
		sbuilder.append(originType.getValue());
		int masterId = NodeMapper.getInstance().getMasterId();
		sbuilder.append(new DecimalFormat("00").format(masterId));
		
		int categoryId = 0;
		if (originType.equals(OriginType.OFFICIAL_STORE)
				|| originType.equals(OriginType.UNOFFICIAL_STORE)) {
			categoryId = AppMapper.getInstance().getCategoryId(appType, originType, storeName, 
					storeDisplayName, storeUrl, storeType, category, masterId);
		} else if (originType.equals(OriginType.USER)) {
			// TODO query user id by user name in the database 
			categoryId = UserMapper.getInstance().getUserId(category);
		}
		sbuilder.append(new DecimalFormat("0000").format(categoryId));
		
		int appCount = AppMapper.getInstance().increaseAndGetAppCount(appType, originType, categoryId);
		sbuilder.append(new DecimalFormat("00000000").format(appCount));
		
		return Long.parseLong(sbuilder.toString());
	}
	/**
	 * save app info
	 * @param appType
	 * @param originType
	 * @param storeName
	 * @param storeDisplayName
	 * @param storeUrl
	 * @param storeType
	 * @param category
	 * @param app
	 * @return
	 * @throws DbUtilsException
	 */
	public synchronized long saveAppInfo(AppType appType, OriginType originType, String storeName,
			String storeDisplayName, String storeUrl, int storeType, String category, App app) 
			throws DbUtilsException {
		
		Long appId = generateAppId(appType, originType, storeName, storeDisplayName, storeUrl, storeType, category);
		app.setAppId(appId);
		int categoryId = Integer.parseInt(String.valueOf(appId).substring(4, 8));
		int collectedCount = Integer.parseInt(String.valueOf(appId).substring(8));
		int retValue = 0;
		AppStorageService appStorageService = null;
		try {
			appStorageService = new AppStorageService();
			retValue += appStorageService.save(app);
			retValue += appStorageService.updateAppCollectedCount(appType, originType, categoryId, collectedCount);
		} catch (DbUtilsException due) {
			backtrackAppCount(appType, originType, categoryId);
			throw new DbUtilsException("Failed to save app : " + app.getAppChecksum() + " : " + due.getMessage(), due);
		} finally {
			appStorageService = null;
		}
		if (retValue > 0) {
			return appId;
		} else {
			return retValue;
		}
	}
}
