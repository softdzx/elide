package com.yahoo.elide.async.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

@Singleton 
class AsyncQueryExecutor {
	
	private static AsyncQueryExecutor executor;
    private ExecutorService executorService;

    protected static AsyncQueryExecutor getInstance(int threadPoolSize) {
        if (executor == null) {
          synchronized (AsyncQueryExecutor.class) {
            executor = new AsyncQueryExecutor(threadPoolSize);
          }
        }
        return executor;
      }

    protected AsyncQueryExecutor(int threadPoolSize) {
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

	protected ExecutorService getExecutorService() {
		return executorService;
	}
}
