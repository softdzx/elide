/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.spring.config;

import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.async.models.AsyncQueryResult;
import com.yahoo.elide.core.EntityDictionary;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Auto Configuration For Elide Services.  Override any of the beans (by defining your own) to change
 * the default behavior.
 */
@Configuration
@EntityScan(basePackageClasses = AsyncQuery.class)
@EnableConfigurationProperties(ElideConfigProperties.class)
@ConditionalOnExpression("${elide.async.enabled:false}")
public class ElideAsyncConfiguration {

    /**
     * Creates a singular swagger document for JSON-API.
     * @param dictionary Contains the static metadata about Elide models.
     * @param settings Elide configuration settings.
     * @return An instance of a JPA DataStore.
     */
    public EntityDictionary bindAsyncDictionary(EntityDictionary dictionary, ElideConfigProperties settings) {

        dictionary.bindEntity(AsyncQuery.class);
        dictionary.bindEntity(AsyncQueryResult.class);

        return dictionary;
    }
}
