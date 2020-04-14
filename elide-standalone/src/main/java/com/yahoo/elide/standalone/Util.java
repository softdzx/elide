/*
 * Copyright 2017, Oath Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.standalone;

import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.datastores.aggregation.queryengines.sql.annotation.FromSubquery;
import com.yahoo.elide.datastores.aggregation.queryengines.sql.annotation.FromTable;
import com.yahoo.elide.standalone.dynamic.config.ElideDynamicEntityCompiler;
import com.yahoo.elide.utils.ClassScanner;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Util.
 */
public class Util {

    private static ElideDynamicEntityCompiler dynamicEntityCompiler;

    public static EntityManagerFactory getEntityManagerFactory(String modelPackageName, boolean includeAsyncModel,
            boolean includeDynamicModel, String dynamicConfigPath, Properties options) {

        // Configure default options for example service
        if (options.isEmpty()) {
            options.put("hibernate.show_sql", "true");
            options.put("hibernate.hbm2ddl.auto", "create");
            options.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
            options.put("hibernate.current_session_context_class", "thread");
            options.put("hibernate.jdbc.use_scrollable_resultset", "true");

            // Collection Proxy & JDBC Batching
            options.put("hibernate.jdbc.batch_size", "50");
            options.put("hibernate.jdbc.fetch_size", "50");
            options.put("hibernate.default_batch_fetch_size", "100");

            // Hikari Connection Pool Settings
            options.putIfAbsent("hibernate.connection.provider_class",
                    "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
            options.putIfAbsent("hibernate.hikari.connectionTimeout", "20000");
            options.putIfAbsent("hibernate.hikari.maximumPoolSize", "30");
            options.putIfAbsent("hibernate.hikari.idleTimeout", "30000");

            options.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
            options.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost/elide?serverTimezone=UTC");
            options.put("javax.persistence.jdbc.user", "elide");
            options.put("javax.persistence.jdbc.password", "elide123");
        }

        dynamicEntityCompiler = new ElideDynamicEntityCompiler(dynamicConfigPath);
        if (includeDynamicModel) {
            dynamicEntityCompiler.compile();
            Collection<ClassLoader> classLoaders = new ArrayList<>();
            classLoaders.add(dynamicEntityCompiler.getClassLoader());
            options.put(AvailableSettings.CLASSLOADERS, classLoaders);
        }
        PersistenceUnitInfo persistenceUnitInfo = null;
        try {

            ElideDynamicEntityCompiler.bindClasses = new HashSet<>();
            // add dynamic generated classes
            ElideDynamicEntityCompiler.bindClasses.addAll(populateBindClasses(dynamicEntityCompiler, Entity.class));
            ElideDynamicEntityCompiler.bindClasses.addAll(populateBindClasses(dynamicEntityCompiler, FromTable.class));
            ElideDynamicEntityCompiler.bindClasses.addAll(populateBindClasses(dynamicEntityCompiler,
                    FromSubquery.class));
            // add classes
            ElideDynamicEntityCompiler.bindClasses.addAll(ClassScanner.getAnnotatedClasses(Entity.class));
            ElideDynamicEntityCompiler.bindClasses.addAll(ClassScanner.getAnnotatedClasses(FromTable.class));
            ElideDynamicEntityCompiler.bindClasses.addAll(ClassScanner.getAnnotatedClasses(FromSubquery.class));

            persistenceUnitInfo = new PersistenceUnitInfoImpl("elide-stand-alone",
                    combineModelEntities(dynamicEntityCompiler, modelPackageName,
                            includeAsyncModel, includeDynamicModel),
                    options,
                    dynamicEntityCompiler.getClassLoader());

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new EntityManagerFactoryBuilderImpl(
                new PersistenceUnitInfoDescriptor(persistenceUnitInfo), new HashMap<>(),
                dynamicEntityCompiler.getClassLoader())
                .build();
    }

    /**
     * Combine the model entities with Async  and Dynamic models.
     *
     * @param modelPackageName Package name
     * @param includeAsyncModel Include Async model package Name
     * @param includeDynamicModel Include Dynamic model package Name
     * @return All entities combined from both package.
     * @throws ClassNotFoundException
     */
    public static List<String> combineModelEntities(ElideDynamicEntityCompiler compiler, String modelPackageName,
            boolean includeAsyncModel, boolean includeDynamicModel) throws ClassNotFoundException {
        List<String> modelEntities = getAllEntities(modelPackageName);
        if (includeAsyncModel) {
            modelEntities.addAll(getAllEntities(AsyncQuery.class.getPackage().getName()));
        }
        if (includeDynamicModel) {
            modelEntities.addAll(findAnnotatedClasses(compiler, Entity.class));
        }
        return modelEntities;
    }

    /**
     * Get all the entities in a package.
     *
     * @param packageName Package name
     * @return All entities found in package.
     */
    public static List<String> getAllEntities(String packageName) {
        return ClassScanner.getAnnotatedClasses(packageName, Entity.class).stream()
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    /**
     * Find classes with a particular annotation from dynamic compiler.
     * @param compiler An instance of ElideDynamicEntityCompiler.
     * @param annotationClass Annotation to search for.
     * @return Set of Classes matching the annotation.
     * @throws ClassNotFoundException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<String> findAnnotatedClasses(ElideDynamicEntityCompiler compiler, Class annotationClass)
            throws ClassNotFoundException {
        List<String> annotatedClass = new ArrayList<String>();
        List<String> dynamicClasses = ElideDynamicEntityCompiler.classNames;
        for (String dynamicClass : dynamicClasses) {
            Class<?> classz = compiler.getClassLoader().loadClass(dynamicClass);
            if (classz.getAnnotation(annotationClass) != null) {
                annotatedClass.add(classz.getName());
            }
        }
        return annotatedClass;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Set<Class> populateBindClasses(ElideDynamicEntityCompiler compiler,
            Class annotationClass) throws ClassNotFoundException {
        Set<Class> bindClasses = new HashSet<>();
        List<String> dynamicClasses = ElideDynamicEntityCompiler.classNames;
        for (String dynamicClass : dynamicClasses) {
            Class<?> bindClass = compiler.getClassLoader().loadClass(dynamicClass).getClass();
            if (bindClass.getAnnotation(annotationClass) != null) {
                bindClasses.add(bindClass);
            }
        }
        return bindClasses;
    }
}
