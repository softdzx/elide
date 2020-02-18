package com.yahoo.elide.async.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.OnCreatePostCommit;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.async.models.security.IsOwner.AsyncQueryOwner;
import com.yahoo.elide.async.service.AsyncExecutorService;
import com.yahoo.elide.async.service.QueryThread;

import lombok.extern.slf4j.Slf4j;

@Entity
@Include(type = "query", rootLevel = true)
@ReadPermission(expression = AsyncQueryOwner.PRINCIPAL_IS_OWNER)
@UpdatePermission(expression = "Prefab.Role.None")
@Slf4j
public class AsyncQuery implements PrincipalOwned {
    @Id
    UUID id; //Can be generated or provided.

    //Extracted from the Principal object
    String principalName;

    String query;  //JSON-API PATH or GraphQL payload.

    QueryType queryType; //GRAPHQL, JSONAPI

//    @UpdatePermission(expression = "Principal is Owner AND value is Cancelled")
    QueryStatus status;

    @OneToOne
    AsyncQueryResult result;

    Date createdOn;
    Date updatedOn;

    @Override
	public String getPrincipalName() {
		return principalName;
	}

    @OnCreatePostCommit
    public void executeQueryFromExecutor() {
        log.info("AsyncExecutorService executor starting to execute query");
        AsyncExecutorService.executeQuery(query, queryType);
    }
}
