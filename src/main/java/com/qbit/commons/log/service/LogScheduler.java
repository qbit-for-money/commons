package com.qbit.commons.log.service;

import com.qbit.commons.log.dao.LogDAO;
import com.qbit.commons.log.model.Log;
import com.qbit.commons.log.model.OperationType;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Alex
 */
@Singleton
public class LogScheduler {
	private ExecutorService executorService;
	
	@Inject
	private LogDAO logDAO;
	
	@PostConstruct
	public void init() {
		
		executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "LogScheduler");
				thread.setDaemon(true);
				return thread;
			}
		});
	}
	public void createLog(final Log log) {
		if((log == null)) {
			return;
		}
		log.setLogTimestamp(new Date());
		executorService.submit(new Runnable() {

			@Override
			public void run() {
				logDAO.create(log);
			}
		});
	}
	
	public void createLog(OperationType type, String userId, String entityId, String fieldName, String fieldValue) {
		Log log = new Log();
		log.setType(type);
		log.setUserId(userId);
		log.setEntityId(entityId);
		log.setFieldName(fieldName);
		log.setFieldValue(fieldValue);
		createLog(log);
	}
	
	public void createLog(OperationType type, String userId, String entityId) {
		Log log = new Log();
		log.setType(type);
		log.setUserId(userId);
		log.setEntityId(entityId);
		log.setFieldName(null);
		log.setFieldValue(null);
		createLog(log);
	}
}
