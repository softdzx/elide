package com.yahoo.elide.async.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import com.yahoo.elide.async.models.QueryType;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class AsyncExecutorService {
    // Simple threadpool of size 5 for testing
	private static ExecutorService executor = Executors.newFixedThreadPool(5);

	public static void executeQuery(String query, QueryType queryType) {
		Runnable worker = new QueryThread("New Thread");
		log.info("query: {}", query);
		log.info("queryType: {}", queryType);
		executor.execute(worker);
	}
}
