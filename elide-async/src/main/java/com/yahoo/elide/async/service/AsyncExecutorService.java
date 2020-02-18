package com.yahoo.elide.async.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.yahoo.elide.Elide;
import com.yahoo.elide.async.models.QueryType;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class AsyncExecutorService {

	private Elide elide;

	@Inject
    public AsyncExecutorService(Elide elide) {
		this.elide = elide;
	}

	// Simple threadpool of size 5 for testing
	private static ExecutorService executor = Executors.newFixedThreadPool(5);

	public void executeQuery(String query, QueryType queryType) {
		Runnable queryWorker = new QueryThread("New Thread");
		log.info("query: {}", query);
		log.info("queryType: {}", queryType);
		log.info("Elide object: {}", elide);
		//elide.get(query, null, user);
		executor.execute(queryWorker);
	}

}
