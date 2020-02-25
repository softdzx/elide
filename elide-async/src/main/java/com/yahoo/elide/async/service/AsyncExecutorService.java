package com.yahoo.elide.async.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
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
	private final int DEFAULT_CLEANUP_DELAY = 360; 
	private final int DEFAULT_INTERRUPT_DELAY = 35;
	
	private Elide elide;
	private QueryRunner runner;
	private ExecutorService executor;
	private ScheduledExecutorService cleaner;
	protected static List<FutureTask> futures = new ArrayList<FutureTask>();
	
	@Inject
    public AsyncExecutorService(Elide elide, Integer threadPoolSize, Integer maxRunTime, Integer numberOfNodes) {
		this.elide = elide;
		this.runner = new QueryRunner(elide);
		executor = AsyncQueryExecutor.getInstance(threadPoolSize == null ? DEFAULT_THREADPOOL_SIZE : threadPoolSize).getExecutorService(); 
		
		// Setting up query cleaner that marks loing running query as TIMEDOUT.
		cleaner = AsyncQueryCleaner.getInstance().getExecutorService(); 
		AsyncQueryCleanerThread cleanUpTask = new AsyncQueryCleanerThread(maxRunTime, elide, false);
		AsyncQueryCleanerThread interruptTask = new AsyncQueryCleanerThread(maxRunTime, elide, true);
		
		// Since there will be multiple hosts running the elide service,
		// setting up random delays to avoid all of them trying to cleanup
		// at the same time.
		Random random = new Random();
		int initialDelay = random.ints(0, numberOfNodes*2).limit(1).findFirst().getAsInt();
		
		//cleaner.scheduleWithFixedDelay(cleanUpTask, initialDelay, DEFAULT_CLEANUP_DELAY, TimeUnit.MINUTES);
		//cleaner.scheduleWithFixedDelay(interruptTask, initialDelay, DEFAULT_INTERRUPT_DELAY, TimeUnit.MINUTES);
		cleaner.scheduleWithFixedDelay(cleanUpTask, 1, 1, TimeUnit.MINUTES);
		cleaner.scheduleWithFixedDelay(interruptTask, 2, 1, TimeUnit.MINUTES);
	}
	
	public void executeQuery(String query, QueryType queryType, RequestScope scope, UUID id) {
		AsyncQueryThread queryWorker = new AsyncQueryThread(query, queryType, scope, elide, runner, id);
		// Change async query in Datastore to queued
		try {
			queryWorker.updateAsyncQueryStatus(QueryStatus.QUEUED, id);
		} catch (IOException e) {
			log.error("IOException: {}", e.getMessage());
		}
		
		//executor.execute(queryWorker);
		futures.add(new FutureTask(executor.submit(queryWorker), id, new Date()));
	}
}

class FutureTask {
	Future<?> future;
	UUID id;
	Date submittedOn;
	
	public FutureTask(Future<?> future, UUID id, Date submittedOn) {
		this.future = future;
		this.id = id;
		this.submittedOn = submittedOn;
	}
}

