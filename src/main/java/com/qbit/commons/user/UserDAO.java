package com.qbit.commons.user;

import static com.qbit.commons.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.commons.dao.util.TrCallable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.WebApplicationException;
import org.eclipse.persistence.jpa.JpaQuery;

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

	public UserInfo findFromAllIds(String publicKey) {
		UserInfo userInfo = find(publicKey);
		if (userInfo != null) {
			return userInfo;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<UserInfo> criteria = builder.createQuery(UserInfo.class);

			Root<UserInfo> user = criteria.from(UserInfo.class);
			criteria.select(user);
			Expression<Collection> idsExpression = user.get("additionalIds");
			Predicate containsIdsPredicate = builder.isMember(publicKey, idsExpression);
			criteria.where(containsIdsPredicate);

			TypedQuery<UserInfo> q = entityManager.createQuery(criteria);
			List<UserInfo> users = q.getResultList();
			if (users.isEmpty()) {
				return null;
			}
			if (users.size() > 1) {
				throw new WebApplicationException();
			}
			return users.get(0);
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

	public long getAdditionalIdCount(final String publicKey) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery criteria = builder.createQuery(Long.class);
			Root<UserInfo> user = criteria.from(UserInfo.class);
			criteria.select(builder.countDistinct(user));
			Expression<Collection> idsExpression = user.get("additionalIds");
			Predicate containsIdsPredicate = builder.isMember(publicKey, idsExpression);
			criteria.where(containsIdsPredicate);
			long idsCount = (Long) entityManager.createQuery(criteria).getSingleResult();
			return idsCount;
		} finally {
			entityManager.close();
		}
	}

	public boolean containsAuthServiceId(String userId, String additionalUserId) {
		if ((userId == null) || userId.isEmpty() || (additionalUserId == null) || additionalUserId.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if ((additionalUserId.contains("@") && userId.contains("@"))
				|| (additionalUserId.contains("vk-") && userId.contains("vk-"))) {
			return true;
		}
		UserInfo user = find(userId);
		if ((user == null) || (user.getAdditionalIds() == null) || user.getAdditionalIds().isEmpty()) {
			return false;
		}
		for (String additionalId : user.getAdditionalIds()) {
			if ((additionalId.contains("@") && additionalUserId.contains("@"))
					|| (additionalId.contains("vk-") && additionalUserId.contains("vk-"))) {
				return true;
			}
		}
		return false;
	}

	public List<UserInfo> getCommonsIds(final String publicKey) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery criteria = builder.createQuery(UserInfo.class);

			Root<UserInfo> user = criteria.from(UserInfo.class);
			criteria.select(user);

			Expression<String> userIdExpression = user.get("publicKey");
			Predicate publicKeyPredicate = builder.equal(userIdExpression, publicKey);
			Expression<Collection> idsExpression = user.get("additionalIds");
			Predicate containsIdsPredicate = builder.isMember(publicKey, idsExpression);
			Predicate predicate = builder.or(publicKeyPredicate, containsIdsPredicate);
			criteria.where(predicate);
			return entityManager.createQuery(criteria).getResultList();
		} finally {
			entityManager.close();
		}
	}

	public UserInfo setAdditionalId(final String userId, final String additionalUserId) {
		if ((userId == null) || userId.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<UserInfo>() {

			@Override
			public UserInfo
					call(EntityManager entityManager) {
				UserInfo user = entityManager.find(UserInfo.class, userId, LockModeType.PESSIMISTIC_WRITE);
				System.out.println("%% " + getAdditionalIdCount(additionalUserId) + " : " + additionalUserId);
				if ((user == null) || userId.equals(additionalUserId) 
						|| (getAdditionalIdCount(additionalUserId) > 0) 
						|| (find(additionalUserId) != null)) {
					return null;
				}
				List<String> additionalIds = user.getAdditionalIds();
				if (additionalIds == null) {
					additionalIds = new ArrayList<>();
				}
				if (!additionalIds.contains(additionalUserId)) {
					additionalIds.add(additionalUserId);
				}
				user.setAdditionalIds(additionalIds);
				return user;
			}
		});
	}
}
