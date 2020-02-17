package com.yahoo.elide.async.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryThread implements Runnable{

    public QueryThread(String s){
        log.info(s + " thread created");
    }

    @Override
    public void run() {
        processQuery();
    }

    private void processQuery() {
        try {
            //Just doing sleep for testing
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
