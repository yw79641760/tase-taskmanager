/**
 * 
 */
package com.softsec.tase.task.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UserMapper.java
 * @author yanwei
 * @date 2013-3-20 下午9:39:15
 * @description just fetch user name and is not responsible for user registration 
 */
public class UserMapper {

	/**
	 * Map<UserName, UserId>
	 */
	private Map<String, Integer> userIdMap = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * Map<UserId, UserAppCount>
	 */
	private Map<Integer, AtomicInteger> userCountMap = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	private static final UserMapper userMapper = new UserMapper();
	
	public UserMapper() {
	}
	
	public static UserMapper getInstance() {
		return userMapper;
	}
	
	public synchronized Map<String, Integer> getUserIdMap() {
		return userIdMap;
	}
	
	public synchronized Map<Integer, AtomicInteger> getUserCountMap() {
		return userCountMap;
	}
	
	/**
	 * init user id map
	 * @param userName
	 * @param userId
	 */
	public synchronized void initUserIdMap(String userName, int userId) {
		if (userIdMap.get(userName) == null) {
			userIdMap.put(userName, userId);
		}
	}
	
	/**
	 * init user count map
	 * @param userId
	 * @param userCount
	 */
	public synchronized void initUserCountMap(int userId, int userCount) {
		if (userCountMap.get(userId) == null) {
			userCountMap.put(userId, new AtomicInteger(userCount));
		}
	}
	
	/**
	 * get user id by user name
	 * @param userName
	 * @return
	 */
	public synchronized int getUserId(String userName) {
		return userIdMap.get(userName);
	}
	
	/**
	 * get user count by user id
	 * @param userId
	 * @return
	 */
	public synchronized int getUserCount(int userId) {
		return userCountMap.get(userId).get();
	}
	
	/**
	 * increase and get user count
	 * @param userId
	 * @return
	 */
	public synchronized int increaseAndGetUserCount(int userId) {
		return userCountMap.get(userId).incrementAndGet();
	}
}
