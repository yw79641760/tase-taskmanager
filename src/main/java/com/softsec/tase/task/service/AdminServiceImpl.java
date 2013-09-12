/**
 * 
 */
package com.softsec.tase.task.service;

import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.AdminService;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.store.exception.DbUtilsException;
import com.softsec.tase.store.service.ProgramStorageService;

/**
 * Admin Service 实现类
 * @author yanwei
 * @date 2012-12-27 上午11:32:27
 * 
 */
public class AdminServiceImpl implements AdminService.Iface{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
	
	/**
	 * check network's connectivity manually
	 * @return date string
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.AdminService.Iface#ping()
	 */
	@Override
	public String ping() throws UnavailableException, TimeoutException,	TException {
		return new Date().toString();
	}

	/**
	 * check for program's duplication
	 * @param scriptMd5
	 * @param executableMd5
	 * @return programId
	 * 			programId if duplicate or 0L if not duplicate
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.AdminService.Iface#checkProgramDuplication(java.lang.String, java.lang.String)
	 */
	@Override
	public long checkProgramDuplication(String scriptMd5, String executableMd5)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		long programDuplicatedId = 0L;
		
		if (StringUtils.isEmpty(scriptMd5) && StringUtils.isEmpty(executableMd5)) {
			throw new InvalidRequestException("Program 's script md5 and executable md5 must not be null.");
		} else {
			ProgramStorageService programStorageService = new ProgramStorageService();
			try {
				programDuplicatedId = programStorageService.checkForDuplication(scriptMd5, executableMd5);
			} catch (DbUtilsException due) {
				LOGGER.error("Failed to check program 's duplication : " + due.getMessage(), due);
				throw new UnavailableException("Failed to check program 's duplication : " + due.getMessage());
			} finally {
				programStorageService = null;
			}
		}
		return programDuplicatedId;
	}
	
//	/**
//	 * register new program
//	 * @param programme
//	 * @return programId
//	 */
//	/* (non-Javadoc)
//	 * @see com.softsec.tase.rpc.service.task.AdminService.Iface#registerProgram(com.softsec.tase.rpc.domain.container.Programme)
//	 */
//	@Override
//	public long registerProgram(Programme programme)
//			throws InvalidRequestException, UnavailableException,
//			TimeoutException, TException {
//		
//		long programId = 0L;
//		
//		if (!isParamsValid(programme)) {
//			return programId;
//		}
//		
//		ProgramStorageService service = new ProgramStorageService();
//		try {
//			programId = service.register(programme);
//		} catch (FileUtilsException filee) {
//			LOGGER.error("Failed to save program in file I/O : " + filee.getMessage(), filee);
//			throw new UnavailableException("Failed to save program in file I/O : " + filee.getMessage());
//		} catch (FtpUtilsException ftpe) {
//			LOGGER.error("Failed to save program in ftp : " + ftpe.getMessage(), ftpe);
//			throw new UnavailableException("Failed to save program in ftp : " + ftpe.getMessage());
//		} catch (DbUtilsException de) {
//			LOGGER.error("Failed to save program in database : " + de.getMessage(), de);
//			throw new UnavailableException("Failed to save program in database : " + de.getMessage());
//		}
//
//		return programId;
//	}
	
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.AdminService.Iface#refreshQueueConfig()
	 */
	@Override
	public String refreshQueueConfig() throws UnavailableException,
			TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}

//	/**
//	 * check programme parameter's validation
//	 * @param programme
//	 * @return
//	 * @throws InvalidRequestException
//	 * @throws UnavailableException
//	 */
//	private static boolean isParamsValid(Programme programme) throws InvalidRequestException, UnavailableException {
//		
//		boolean isValid = false;
//		
//		if (StringUtils.isEmpty(programme.getProgrammeName())
//				|| StringUtils.isEmpty(programme.getScriptName())
//				|| StringUtils.isEmpty(programme.getScriptMd5())
//				|| StringUtils.isEmpty(programme.getExecutableName())
//				|| StringUtils.isEmpty(programme.getExecutableMd5())) {
//			
//			LOGGER.error("Program's fields must not be null.");
//			throw new InvalidRequestException("Program's fields must not be null.");
//			
//		} else if (!App.isAppTypeMember(programme.getAppType())
//				|| !Job.isJobLifecycleMember(programme.getJobLifecycle())
//				|| !Job.isJobPhaseMember(programme.getJobPhase())){
//			
//			throw new InvalidRequestException("Program type is not valid : " + programme.getAppType().name()
//					+ " : " + programme.getJobLifecycle().name() + " : " + programme.getJobPhase().name());
//			
//		} else {
//			
//			try {
//				
//				// check program 's validation
//				if (!programme.getScriptMd5().equals(IOUtils.getByteArrayMd5(programme.getScript()))) {
//					LOGGER.error("Failed to transfer program's script : md5 mismatch.");
//					throw new InvalidRequestException("Failed in script transfer, md5 mismatch.");
//				}
//				if (!programme.getExecutableMd5().equals(IOUtils.getByteArrayMd5(programme.getExecutable()))) {
//					LOGGER.error("Failed to transfer program's executable : md5 mismatch.");
//					throw new InvalidRequestException("Failed in executable transfer, md5 mismatch.");
//				}
//				
//			} catch (IOUtilsException ioue) {
//				LOGGER.error("Failed to calculate md5 from bytes : " + ioue.getMessage(), ioue);
//				throw new UnavailableException("Failed to calculate program's md5 from bytes : " + ioue.getMessage());
//			}
//			
//			isValid = true;
//		}
//		
//		return isValid;
//	}
	
	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.task.AdminService.Iface#refreshNodeList(java.util.List)
	 */
	@Override
	public String refreshNodeList(List<String> nodeIdList)
			throws UnavailableException, TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}
}
