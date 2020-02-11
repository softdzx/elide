package com.yahoo.elide.async.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;

@Entity
@Include(type = "query", rootLevel = true)
@ReadPermission(expression = "Principal is Owner")
@UpdatePermission(expression = "None")
public class AsyncQuery implements PrincipalOwned {
    @Id
    UUID id; //Can be generated or provided.

    //Extracted from the Principal object
    String principalName;

    String query;  //JSON-API PATH or GraphQL payload.
    QueryType queryType; //GRAPHQL, JSONAPI

    @UpdatePermission(expression = "Principal is Owner AND value is Cancelled")
    QueryStatus status;

    @OneToOne
    AsyncQueryResult result;

    Date createdOn;
    Date updatedOn;

    @Override
	public String getPrincipalName() {
		return principalName;
	}

}
