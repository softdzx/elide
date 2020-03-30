/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.spring.dynamic.config;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.Manager;
import org.hibernate.MappingException;
import org.hibernate.annotations.Parent;
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
import java.lang.annotation.Annotation;
import com.yahoo.elide.spring.config.ElideConfigProperties;
import com.yahoo.elide.utils.ClassScanner;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.Entity;

/**
 * Dynamic Configuration For Elide Services. Override any of the beans (by
 * defining your own) and setting flags to disable in properties to change the
 * default behavior.
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(ElideConfigProperties.class)
@ConditionalOnExpression("${elide.dynamic-config.enabled:false}")
public class ElideDynamicConfiguration {

    @Autowired
    private ElideConfigProperties configProperties;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
            DataSource source){

    	try {
    		
    		Map<String, Object> propertyMap = new HashMap<>();
    		
    		ArrayList<Class> bindClasses = new ArrayList<>();
            bindClasses.addAll(ClassScanner.getAnnotatedClasses(Entity.class));
            
            for(int i=0; i < bindClasses.size(); i++){
                System.out.println( bindClasses.get(i) );
            }
            
            
        	ElideDynamicEntityCompiler compiler = new ElideDynamicEntityCompiler(configProperties.getDynamicConfig().getPath());  
        	compiler.compile(configProperties.getDynamicConfig().getPath());

	        Collection<ClassLoader> classLoaders = new ArrayList<>();
	        classLoaders.add(compiler.getClassLoader());
	        
	        propertyMap.put(AvailableSettings.CLASSLOADERS, classLoaders);
	        
	        propertyMap.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
	        propertyMap.put(AvailableSettings.LOADED_CLASSES, bindClasses);
	        
	        Properties props = new Properties();
	        props.putAll(propertyMap);
	
	        ElideDynamicPersistenceUnit elideDynamicPersistenceUnit = new ElideDynamicPersistenceUnit("default", ElideDynamicEntityCompiler.classNames, props,
	                compiler.getClassLoader());
	        elideDynamicPersistenceUnit.setNonJtaDataSource(source);
	        elideDynamicPersistenceUnit.setJtaDataSource(source);
	
	        new HibernateJpaVendorAdapter();
	        
	        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
	        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	         vendorAdapter.setGenerateDdl(true);
	        bean.setJpaVendorAdapter(vendorAdapter);
	        
	        bean.setJpaPropertyMap(propertyMap);
	        bean.setPersistenceUnitManager(new PersistenceUnitManager() {
	            @Override
	            public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() throws IllegalStateException {
	                return elideDynamicPersistenceUnit;
	            }
	
	            @Override
	            public PersistenceUnitInfo obtainPersistenceUnitInfo(String persistenceUnitName)
	                    throws IllegalArgumentException, IllegalStateException {
	                return elideDynamicPersistenceUnit;
	            }
	        });
	
	        return bean;
        } 
        catch (Exception e) {
            log.error("Setting up Dynamic Configuration failed "+e.getMessage());
            return null;
        }
    }
}