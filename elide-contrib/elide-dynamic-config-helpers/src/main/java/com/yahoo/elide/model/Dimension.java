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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "description",
    "category",
    "hidden",
    "readAccess",
    "definition",
    "type",
    "grains",
    "tags"
})
@Data
@EqualsAndHashCode()
@AllArgsConstructor
@NoArgsConstructor
public class Dimension {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;


    @JsonProperty("hidden")
    private Boolean hidden = false;

    @JsonProperty("readAccess")
    private String readAccess = "Allow All";

    @JsonProperty("definition")
    private String definition = "";

    @JsonProperty("type")
    private Type type = Type.TEXT;

    @JsonProperty("grains")
    private List<Grains> grains = new ArrayList<Grains>();

    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = new LinkedHashSet<String>();


    //    default behaviour, in case description is null
    public String getDescription() {
        return (this.description == null ? getName() : this.description);
    }
}
