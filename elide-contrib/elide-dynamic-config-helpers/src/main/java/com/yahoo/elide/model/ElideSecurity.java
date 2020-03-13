/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "roles",
    "rules"
})
@Data
@EqualsAndHashCode()
@AllArgsConstructor
@NoArgsConstructor
public class ElideSecurity {

    @JsonProperty("roles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> roles = new LinkedHashSet<String>();

    @JsonProperty("rules")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Rule> rules = new LinkedHashSet<Rule>();
}
