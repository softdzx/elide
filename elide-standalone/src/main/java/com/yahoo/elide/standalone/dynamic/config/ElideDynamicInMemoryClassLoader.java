/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.standalone.dynamic.config;

import com.google.common.collect.Sets;
import org.mdkt.compiler.DynamicClassLoader;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@Slf4j
/**
 * Dynamic in-memory class loader.
 */
public class ElideDynamicInMemoryClassLoader extends DynamicClassLoader {

    private Set<String> classNames = Sets.newHashSet();

    /**
     * initilize super classloader and set dynamic classes.
     * @param parent ClassLoader.
     * @param classNames - set of Dynamic Classes.
     */
    public ElideDynamicInMemoryClassLoader(ClassLoader parent, Set<String> classNames) {
        super(parent);
        setClassNames(classNames);
    }

    /**
     * setter for set of dynamic class names.
     * @param classNames - set of Dynamic Classes.
     */
    public void setClassNames(Set<String> classNames) {
        this.classNames = classNames;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {
        log.debug("Finding Resource " + name + " in " + classNames);
        if (classNames.contains(name.replace("/", ".").replace(".class", ""))) {
            try {
                log.debug("Returning Resource " + "file://" + name);
                return new URL("file://" + name);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
        return super.findResource(name);
    }
}
