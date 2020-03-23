/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.spring.config;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Dynamic Configuration For Elide Services. Override any of the beans (by
 * defining your own) and setting flags to disable in properties to change the
 * default behavior.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ElideConfigProperties.class)
@ConditionalOnExpression("${elide.dynamicConfig.enabled:true}")
public class ElideDynamicConfiguration {

    @Autowired
    private ElideConfigProperties configProperties;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
            DataSource source) throws IOException {

    	try {
    		log.info("Elide Dynamic Config Path" + configProperties.getDynamicConfig().getPath());

        	ElideDynamicEntityCompiler compiler = new ElideDynamicEntityCompiler(configProperties.getDynamicConfig().getPath());
            
        	compiler.compile(configProperties.getDynamicConfig().getPath());

	        Collection<ClassLoader> classLoaders = new ArrayList<>();
	        classLoaders.add(compiler.getClassLoader());
	        Map<String, Object> properties = new HashMap<>();
	        properties.put(AvailableSettings.CLASSLOADERS, classLoaders);
	
	        Properties props = new Properties();
	        props.putAll(properties);
	
	        ElideDynamicPersistenceUnit pui = new ElideDynamicPersistenceUnit("DynamicConfiguration", ElideDynamicEntityCompiler.classNames, props,
	                compiler.getClassLoader());
	        pui.setNonJtaDataSource(source);
	        pui.setJtaDataSource(source);
	
	        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
	        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
	        bean.setJpaPropertyMap(properties);
	        bean.setPersistenceUnitManager(new PersistenceUnitManager() {
	            @Override
	            public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() throws IllegalStateException {
	                return pui;
	            }
	
	            @Override
	            public PersistenceUnitInfo obtainPersistenceUnitInfo(String persistenceUnitName)
	                    throws IllegalArgumentException, IllegalStateException {
	                return pui;
	            }
	        });
	
	        return bean;
        } catch (Exception e) {
            log.error("Setting up Dynamic Configuration failed "+e.getMessage());
            return null;
        }
    }

}