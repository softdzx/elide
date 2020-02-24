package com.yahoo.elide.async.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

@Singleton 
class AsyncQueryCleaner {
	
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
    	cleanerService = Executors.newSingleThreadScheduledExecutor();
    }

	protected ScheduledExecutorService getExecutorService() {
        return cleanerService;
    }
}

class CleanUpTask implements Runnable {

    int maxRunTime;

    CleanUpTask(int maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    @Override
    public void run() {
        System.out.println("Clean Long Running Query");
        // Add logic to pull queries running longer than maxRunTime and change their status to TIMEDOUT.
    }
}
