package com.qbit.commons.dao.util;

import javax.persistence.EntityManager;

/**
 *
 * @author Александр
 */
public interface TrCallable<T> {

	T call(EntityManager entityManager);
}
