package com.yahoo.elide.async.models;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.OnCreatePostCommit;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.async.service.AsyncExecutorService;
import com.yahoo.elide.security.RequestScope;

import lombok.extern.slf4j.Slf4j;

@Entity
@Include(type = "query", rootLevel = true)
@ReadPermission(expression = "Principal is Owner")
@UpdatePermission(expression = "Prefab.Role.None")
@Slf4j
public class AsyncQuery implements PrincipalOwned {
    @Id
    UUID id; //Can be generated or provided.

    //Extracted from the Principal object
    String principalName;

    String query;  //JSON-API PATH or GraphQL payload.

    QueryType queryType; //GRAPHQL, JSONAPI

    //@UpdatePermission(expression = "Principal is Owner AND value is Cancelled")
    @UpdatePermission(expression = "Principal is Owner")
    QueryStatus status;

    @OneToOne
    AsyncQueryResult result;

    Date createdOn;
    Date updatedOn;

    @Override
	public String getPrincipalName() {
		return principalName;
	}
    
	public QueryStatus getQueryStatus() {
		return status;
	}

    @Inject
    @Transient
    AsyncExecutorService asyncExecutorService;

    @OnCreatePostCommit
    public void executeQueryFromExecutor(RequestScope scope) {
        log.info("AsyncExecutorService executor object: {}", asyncExecutorService);
        asyncExecutorService.executeQuery(query, queryType, id, scope);
    }
}
