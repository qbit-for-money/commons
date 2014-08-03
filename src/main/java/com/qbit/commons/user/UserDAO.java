package com.qbit.commons.user;

import static com.qbit.commons.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.commons.dao.util.TrCallable;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

/**
 *
 * @author Александр
 */
@Singleton
public class UserDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public static UserInfo findAndLock(EntityManager entityManager, String publicKey) {
		if ((entityManager == null) || (publicKey == null) || publicKey.isEmpty()) {
			return null;
		}
		return entityManager.find(UserInfo.class, publicKey, LockModeType.PESSIMISTIC_WRITE);
	}

	public UserInfo find(String publicKey) {
		if ((publicKey == null) || publicKey.isEmpty()) {
			return null;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return entityManager.find(UserInfo.class, publicKey);
		} finally {
			entityManager.close();
		}
	}

	public UserInfo create(final String publicKey) {
		if (publicKey == null) {
			return null;
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<UserInfo>() {

			@Override
			public UserInfo call(EntityManager entityManager) {
				UserInfo user = entityManager.find(UserInfo.class, publicKey);
				if (user != null) {
					return user;
				}
				user = new UserInfo();
				user.setPublicKey(publicKey);
				user.setRegistrationDate(new Date());
				entityManager.merge(user);
				return user;
			}
		});
	}
}
