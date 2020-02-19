package com.yahoo.elide.async.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;

@Entity
@Include(type="queryResult")
@ReadPermission(expression = "Principal is Owner")
@UpdatePermission(expression = "Prefab.Role.None")
@CreatePermission(expression = "Prefab.Role.None")
public class AsyncQueryResult implements PrincipalOwned {
    @Id
    UUID id; //Matches UUID in query.

    Integer contentLength;

    String responseBody; //success or errors

    Integer status; // HTTP Status

    Date createdOn;
    Date updatedOn;

    @OneToOne
    AsyncQuery query;

    @Exclude
    public String getPrincipalName() {
       return query.getPrincipalName();
    }
}