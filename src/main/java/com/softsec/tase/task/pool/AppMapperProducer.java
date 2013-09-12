/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.app.OriginType;
import com.softsec.tase.store.domain.AppCategoryItem;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.AppStorageService;

/**
 * AppMapperProducer.java
 * @author yanwei
 * @date 2013-3-12 下午2:03:59
 * @description
 */
public class AppMapperProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppMapperProducer.class);
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		LOGGER.info("Start initializing app mapper ...");
		
		List<AppCategoryItem> appCategoryItemList = null;
		AppStorageService appStorageService = new AppStorageService();
		try {
			for (AppType appType : AppType.values()) {
				
				if (!appType.equals(AppType.COMMON)) {
					for (OriginType originType : OriginType.values()) {
						
						if (originType.equals(OriginType.OFFICIAL_STORE)
								|| originType.equals(OriginType.UNOFFICIAL_STORE)) {
							appCategoryItemList = appStorageService.getCategoryList(appType, originType, NodeMapper.getInstance().getMasterId());
							
							// build app category and count mapper instance
							if (appCategoryItemList != null && appCategoryItemList.size() != 0) {
								for (AppCategoryItem appCategoryItem : appCategoryItemList) {
									
									AppMapper.getInstance().initAppCategoryMapWithCategory(
											appCategoryItem.getStoreName(), 
											appCategoryItem.getCategory(), 
											appCategoryItem.getCategoryId());
									
									AppMapper.getInstance().initAppCountMapWithCategoryCount(
											appType, 
											originType, 
											appCategoryItem.getCategoryId(), 
											appCategoryItem.getCollectedCount());
								}
							}
						}
					}
				}
			}
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get app category list : " + due.getMessage(), due);
			System.exit(-1);
		}
		
		// finish initialize app category mapper
		appStorageService = null;
		LOGGER.info("Finished initializing app category mapper : " + AppMapper.getInstance().getAppCategoryMap().size());
		LOGGER.info("Finished initializing app count mapper : " + AppMapper.getInstance().getAppCountMap().size());
	}

}
