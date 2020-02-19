package com.yahoo.elide.async.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.yahoo.elide.Elide;
import com.yahoo.elide.async.models.QueryType;
import com.yahoo.elide.security.RequestScope;

@Singleton
public class AsyncExecutorService {

	private Elide elide;

	@Inject
    public AsyncExecutorService(Elide elide) {
		this.elide = elide;
	}

	// Simple threadpool of size 5 for testing
	private static ExecutorService executor = Executors.newFixedThreadPool(5);

	public void executeQuery(String query, QueryType queryType, RequestScope scope) {
		Runnable queryWorker = new QueryThread(query, queryType, scope, elide);
		// Change async query in db to queued
		executor.execute(queryWorker);
	}

}
