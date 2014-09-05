package com.qbit.commons.log.dao;

import com.qbit.commons.dao.util.DAOUtil;
import static com.qbit.commons.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.commons.dao.util.TrCallable;
import com.qbit.commons.log.model.Log;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Alex
 */
@Singleton
public class LogDAO {
	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	public Log find(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtil.find(entityManagerFactory.createEntityManager(),
				Log.class, id, null);
		} finally {
			entityManager.close();
		}
	}
	
	public Log create(final Log log) {
		if ((log == null) || !log.isValid()) {
			throw new IllegalArgumentException("Log is null or not valid.");
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<Log>() {

			@Override
			public Log call(EntityManager entityManager) {
				Log mergedLog = entityManager.merge(log);
				System.out.println("LOG: " + mergedLog);
				return mergedLog;
			}
		});
	}
}
