package com.yahoo.elide.async.service;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.yahoo.elide.Elide;
import com.yahoo.elide.async.models.QueryStatus;
import com.yahoo.elide.async.models.QueryType;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.graphql.QueryRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AsyncExecutorService {

	private final int DEFAULT_THREADPOOL_SIZE = 6; 
	private final int DEFAULT_CLEANUP_DELAY = 60; 
	
	private Elide elide;
	private QueryRunner runner;
	private ExecutorService executor;
	private ScheduledExecutorService cleaner;
	
	@Inject
    public AsyncExecutorService(Elide elide, Integer threadPoolSize, Integer maxRunTime, Integer numberOfNodes) {
		this.elide = elide;
		this.runner = new QueryRunner(elide);
		executor = AsyncQueryExecutor.getInstance(threadPoolSize == null ? DEFAULT_THREADPOOL_SIZE : threadPoolSize).getExecutorService(); 
		log.info("AsyncExecThreadCorePoolSize=" + ((ThreadPoolExecutor) executor).getCorePoolSize());
		log.info("AsyncExecThreadMaxPoolSize=" + ((ThreadPoolExecutor) executor).getMaximumPoolSize());
		
		// Setting up query cleaner that marks loing running query as TIMEDOUT.
		cleaner = AsyncQueryCleaner.getInstance().getExecutorService(); 
		CleanUpTask cleanUpTask = new CleanUpTask(maxRunTime, elide);
		log.info("AsycnCleanUpTaskMaxRunTime=" + cleanUpTask.maxRunTime);
		
		// Since there will be multiple hosts running the elide service,
		// setting up random delays to avoid all of them trying to cleanup
		// at the same time.
		Random random = new Random();
		int initialDelay = random.ints(0, numberOfNodes*2).limit(1).findFirst().getAsInt();
		
		cleaner.scheduleWithFixedDelay(cleanUpTask, initialDelay, DEFAULT_CLEANUP_DELAY, TimeUnit.MINUTES);
		//cleaner.scheduleWithFixedDelay(cleanUpTask, 1, DEFAULT_CLEANUP_DELAY, TimeUnit.MINUTES);
	}
	
	public void executeQuery(String query, QueryType queryType, RequestScope scope, UUID id) {
		AsyncQueryThread queryWorker = new AsyncQueryThread(query, queryType, scope, elide, runner, id);
		// Change async query in Datastore to queued
		try {
			queryWorker.updateAsyncQueryStatus(QueryStatus.QUEUED, id);
		} catch (IOException e) {
			log.error("IOException: {}", e.getMessage());
		}
		executor.execute(queryWorker);
	}
}
