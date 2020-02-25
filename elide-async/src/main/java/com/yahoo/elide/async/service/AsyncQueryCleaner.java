package com.yahoo.elide.async.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.async.models.QueryStatus;
import com.yahoo.elide.core.DataStoreTransaction;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.request.EntityProjection;

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
    Elide elide;
    ElideSettings settings;

    CleanUpTask(int maxRunTime, Elide elide) {
        this.maxRunTime = maxRunTime;
        this.elide = elide;
    }

    @Override
    public void run() {
        updateAsyncQueryStatus();
    }
    
    /**
     * This method updates the model for AsyncQuery
     * @param status new status based on the enum QueryStatus
     * @throws IOException 
     * */
    private void updateAsyncQueryStatus() {
		DataStoreTransaction tx = elide.getDataStore().beginTransaction();
        
        EntityDictionary dictionary = elide.getElideSettings().getDictionary();
        Class idType = dictionary.getIdType(AsyncQuery.class);
        String idField = dictionary.getIdFieldName(AsyncQuery.class);
        //dictionary.get
        
        RequestScope scope = new RequestScope(null, null, tx, null, null, elide.getElideSettings());
            
        //FilterExpression idFilter = new InPredicate(
        //        new Path.PathElement(entityClass, idType, idField),
        //        id
        //);
            
        EntityProjection asyncQueryCollection = EntityProjection.builder()
                .type(AsyncQuery.class)
        //      .filterExpression(filterExpression)
                .build();

        AsyncQuery query = (AsyncQuery) tx.loadObject(asyncQueryCollection, UUID.fromString("ba31ca4e-ed8f-4be0-a0f3-12088fa9263e"), scope);
        query.setQueryStatus(QueryStatus.TIMEDOUT);
        tx.save(query, scope);
        tx.commit(scope);
        tx.flush(scope);
        try {
			tx.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
