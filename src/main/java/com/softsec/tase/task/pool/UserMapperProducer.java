/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.domain.auth.User;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.UserStorageService;

/**
 * UserMapperProducer.java
 * @author yanwei
 * @date 2013-3-20 下午9:44:41
 * @description just fetch user name and is not responsible for user registration
 */
public class UserMapperProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserMapperProducer.class);

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Start initializing user mapper ...");
		
		List<User> userList = null;
		UserStorageService userStorageService = new UserStorageService();
		try {
			userList = userStorageService.getUserList();
		} catch (DbUtilsException due) {
			LOGGER.error("Failed to get user list : " + due.getMessage(), due);
			System.exit(-1);
		}
		
		// build user mapper
		for (User user : userList) {
			UserMapper.getInstance().initUserIdMap(user.getUsername(), user.getUserId());
			UserMapper.getInstance().initUserCountMap(user.getUserId(), user.getUserCount());
		}
		
		LOGGER.info("Finished initializing user mapper : " + UserMapper.getInstance().getUserCountMap().toString());
		userStorageService = null;
	}

}
