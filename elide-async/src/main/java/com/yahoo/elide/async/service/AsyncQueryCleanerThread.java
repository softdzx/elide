package com.yahoo.elide.async.service;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.yahoo.elide.Elide;
import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.async.models.QueryStatus;
import com.yahoo.elide.core.DataStoreTransaction;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.core.filter.expression.FilterExpression;
import com.yahoo.elide.request.EntityProjection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncQueryCleanerThread implements Runnable {

    int maxRunTime;
    Elide elide;
    boolean interrupt;
    
    AsyncQueryCleanerThread(int maxRunTime, Elide elide, boolean interrupt) {
        this.maxRunTime = maxRunTime;
        this.elide = elide;
        this.interrupt = interrupt;
    }

    @Override
    public void run() {
        if(interrupt) {
            interruptAsyncQuery();
        } else {
            timeoutAsyncQuery();
        }
    }
    
    /**
     * This method updates the status of long running async query which 
     * were not interrupted due to host crash/app shutdown to TIMEDOUT.
     * */
    private void timeoutAsyncQuery() {
    	DataStoreTransaction tx = elide.getDataStore().beginTransaction();
    	
    	try {

            EntityDictionary dictionary = elide.getElideSettings().getDictionary();
            RSQLFilterDialect filterParser = new RSQLFilterDialect(dictionary);
            RequestScope scope = new RequestScope(null, null, tx, null, null, elide.getElideSettings());

            FilterExpression filter = filterParser.parseFilterExpression("status=in=(" + QueryStatus.PROCESSING.toString() + "," 
            		+ QueryStatus.QUEUED.toString() + ")", AsyncQuery.class, false);

            EntityProjection asyncQueryCollection = EntityProjection.builder()
                    .type(AsyncQuery.class)
                    .filterExpression(filter)
                    .build();

            //AsyncQuery query = (AsyncQuery) tx.loadObjects(asyncQueryCollection, UUID.fromString("ba31ca4e-ed8f-4be0-a0f3-12088fa9263e"), scope);
            Iterable<Object> loaded = tx.loadObjects(asyncQueryCollection, scope);
            Iterator<Object> itr = loaded.iterator();
            while(itr.hasNext()) {
            	log.info("Updating Async Query Status to TIMEDOUT");
            	AsyncQuery query = (AsyncQuery) itr.next(); 
            	long differenceInMillies = Math.abs((new Date()).getTime() - query.getCreatedOn().getTime());
            	long difference = TimeUnit.MINUTES.convert(differenceInMillies, TimeUnit.MILLISECONDS);
            	log.info("difference=" + difference);
            	if(difference > 0) {
            		log.info("Updating Async Query Status to TIMEDOUT");
                    query.setQueryStatus(QueryStatus.TIMEDOUT);
                    tx.save(query, scope);
                    tx.commit(scope);
                    tx.flush(scope);
            	}
            }
		} catch (Exception e) {
           e.printStackTrace();
		} finally {
			try {
				tx.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * This method interrupts the long running AsyncQueryThread.
     * */
    private void interruptAsyncQuery() {
    	List<FutureTask> futures= AsyncExecutorService.futures;
        Iterator<FutureTask> itr = futures.iterator();
        while (itr.hasNext()) {
        	FutureTask futureDetail  = (FutureTask) itr.next();
        	long differenceInMillies = Math.abs((new Date()).getTime() - futureDetail.submittedOn.getTime());
        	long difference = TimeUnit.MINUTES.convert(differenceInMillies, TimeUnit.MILLISECONDS);
        	if(difference > 1 && futureDetail.future.isDone() == false) {
        		futureDetail.future.cancel(true);
        		futures.remove(futureDetail);
        		DataStoreTransaction tx = elide.getDataStore().beginTransaction();
        		try{ 
        		log.debug("Thread interrupted, updating AsyncQuery status to {}", QueryStatus.FAILURE);
                RequestScope scope = new RequestScope(null, null, tx, null, null, elide.getElideSettings());
                EntityProjection asyncQueryCollection = EntityProjection.builder()
                    .type(AsyncQuery.class)
                    .build();
                AsyncQuery query = (AsyncQuery) tx.loadObject(asyncQueryCollection, futureDetail.id, scope);
                query.setQueryStatus(QueryStatus.FAILURE);
                tx.save(query, scope);
                tx.commit(scope);
                tx.flush(scope);
        		} catch (Exception e) {
        	        e.printStackTrace();
        		} finally {
        			try {
        				tx.close();
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        		}
        	}
        }
    }
}