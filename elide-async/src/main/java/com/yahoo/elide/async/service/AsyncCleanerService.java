/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.async.service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.yahoo.elide.Elide;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to execute Async queries. It will schedule task to track long
 * running queries and kills them. It will also schedule task to update
 * orphan query statuses after host/app crash or restart.
 */
@Slf4j
public class AsyncCleanerService {

    private final int DEFAULT_CLEANUP_DELAY_MINUTES = 360;
    private final int MAX_CLEANUP_INTIAL_DELAY_MINUTES = 100;

    @Getter private ScheduledExecutorService cleaner;

    @Inject
    public AsyncCleanerService(Elide elide, Integer maxRunTimeMinutes, Integer queryCleanupDays, AsyncQueryDAO asyncQueryDao) {

        //If query is still running for twice than maxRunTime, then interrupt did not work due to host/app crash.
        int queryRunTimeThresholdMinutes = maxRunTimeMinutes * 2;

        // Setting up query cleaner that marks long running query as TIMEDOUT.
        cleaner = Executors.newSingleThreadScheduledExecutor();
        AsyncQueryCleanerThread cleanUpTask = new AsyncQueryCleanerThread(queryRunTimeThresholdMinutes, elide, queryCleanupDays, asyncQueryDao);

        // Since there will be multiple hosts running the elide service,
        // setting up random delays to avoid all of them trying to cleanup at the same time.
        Random random = new Random();
        int initialDelayMinutes = random.ints(0, MAX_CLEANUP_INTIAL_DELAY_MINUTES).limit(1).findFirst().getAsInt();
        log.debug("Initial Delay for cleaner service is {}", initialDelayMinutes);

        //Having a delay of at least DEFAULT_CLEANUP_DELAY between two cleanup attempts.
        //Or maxRunTimeMinutes * 2 so that this process does not coincides with query interrupt process.
        cleaner.scheduleWithFixedDelay(cleanUpTask, initialDelayMinutes, Math.max(DEFAULT_CLEANUP_DELAY_MINUTES, queryRunTimeThresholdMinutes), TimeUnit.MINUTES);
    }

}