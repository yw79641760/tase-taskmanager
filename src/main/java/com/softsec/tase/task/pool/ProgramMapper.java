/**
 * 
 */
package com.softsec.tase.task.pool;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.util.domain.ProgramUtils;
import com.softsec.tase.store.domain.ProgramItem;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.ProgramStorageService;

/**
 * ProgramMapper.java
 * @author yanwei
 * @date 2013-3-13 上午9:34:10
 * @description only to fetch program mapper and is not responsible for program registration
 */
public class ProgramMapper {

	/**
	 * Map<ProgramType, ProgramCount>
	 */
	private Map<Integer, AtomicInteger> programCountMap = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	/**
	 * Map<ProgramId, ProgramItem>
	 */
	private Map<Long, ProgramItem> programItemMap = new ConcurrentHashMap<Long, ProgramItem>();
	
	private static final ProgramMapper programMapper = new ProgramMapper();
	
	public ProgramMapper() {
	}
	
	public static ProgramMapper getInstance() {
		return programMapper;
	}
	
	public synchronized Map<Integer, AtomicInteger> getProgramCountMap() {
		return programCountMap;
	}
	
	public synchronized Map<Long, ProgramItem> getProgramItemMap() {
		return programItemMap;
	}
	
	/**
	 * init program id prefix
	 * @param programType
	 * @param programCount
	 */
	public synchronized void initProgramCountMap(int programType, int programCount) {
		if (programCountMap.get(programType) == null) {
			programCountMap.put(programType, new AtomicInteger(programCount));
		}
	}
	
	/**
	 * init program id and item for building context
	 * @param programId
	 * @param programItem
	 */
	public synchronized void initProgramIdMap(Long programId, ProgramItem programItem) {
		if (programItemMap.get(programId) == null) {
			programItemMap.put(programId, programItem);
		}
	}
	
	/**
	 * add new program type by program id prefix
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 */
	public synchronized void addProgramType(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase)
		throws DbUtilsException {
		int programType = ProgramUtils.getProgramType(appType, jobLifecycle, jobPhase);
		
		if (programCountMap.get(programType) == null) {
			initProgramCountMap(programType, 0);
			
			// insert new program id prefix into database
			new ProgramStorageService().addProgramType(programType);
		}
	}

	/**
	 * get program count by program type
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 */
	public synchronized int getProgramCount(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase) {
		int programType = ProgramUtils.getProgramType(appType, jobLifecycle, jobPhase);
		return programCountMap.get(programType).get();
	}
	
	/**
	 * get program count by program type
	 * @param programType
	 * @return
	 */
	public synchronized int getProgramCount(int programType) {
		return programCountMap.get(programType).get();
	}
	
	/**
	 * get program item by program id
	 * @param programId
	 * @return
	 */
	public synchronized ProgramItem getProgramItem(long programId) {
		ProgramItem programItem = programItemMap.get(programId);
		if (programItem != null) {
			return programItem;
		} else {
			programItem = new ProgramStorageService().getProgramItem(programId);
			if (programItem != null) {
				initProgramIdMap(programId, programItem);
			}
		}
		return programItem;
	}
	
	/**
	 * increase and get program count by program type
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 */
	public synchronized int increaseAndGetProgramCount(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase)
		throws DbUtilsException {
		addProgramType(appType, jobLifecycle, jobPhase);
		int programType = ProgramUtils.getProgramType(appType, jobLifecycle, jobPhase);
		int programCount = programCountMap.get(programType).incrementAndGet();
		
		new ProgramStorageService().updateProgramCount(programType, programCount);
		return programCount;
	}
	
	/**
	 * generate new program id
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 */
	public synchronized long generateProgramId(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(ProgramUtils.getProgramType(appType, jobLifecycle, jobPhase));
		int programCount = increaseAndGetProgramCount(appType, jobLifecycle, jobPhase);
		sbuilder.append(new DecimalFormat("00").format(programCount));
		return Long.parseLong(sbuilder.toString());
	}
}

