package com.qbit.commons.dao.util;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author Александр
 */
public interface DAOExecutor {
	
	<T> Future<T> submit(TrCallable<T> callable);
	
	ScheduledFuture<?> submit(TrCallable<Void> callable, long delay, int maxFailCount);
}
