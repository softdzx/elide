package com.yahoo.elide.async.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

@Singleton 
class AsyncQueryCleaner {
	
	private final int DEFAULT_CLEANUP_TASKS = 2; //Interrupt & Timeout
	
	private static AsyncQueryCleaner cleaner;
    private ScheduledExecutorService cleanerService;

    protected static AsyncQueryCleaner getInstance() {
        if (cleaner == null) {
          synchronized (AsyncQueryCleaner.class) {
        	  cleaner = new AsyncQueryCleaner();
          }
        }
        return cleaner;
      }

    protected AsyncQueryCleaner() {
    	cleanerService = Executors.newScheduledThreadPool(DEFAULT_CLEANUP_TASKS);
    }

	protected ScheduledExecutorService getExecutorService() {
        return cleanerService;
    }
}
