package com.yahoo.elide.standalone.dynamic.config;

import com.google.common.collect.Sets;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideSecurityConfig;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideTableConfig;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.ElideConfigParser;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHydrator;

import org.mdkt.compiler.CompilationException;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElideDynamicEntityCompiler {
	
	public static List<String> classNames = new ArrayList<String>();
	public static Set<Class> bindClasses;

    public static final String PACKAGE_NAME = "com.yahoo.elide.contrib.dynamicconfig.model.";
    private Map<String, Class<?>> compiledObjects;

    InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

    HandlebarsHydrator hydrator = new HandlebarsHydrator();
    ElideConfigParser elideConfigParser = new ElideConfigParser();
    ElideTableConfig tableConfig = new ElideTableConfig();
    ElideSecurityConfig securityConfig = new ElideSecurityConfig();

    Map<String, String> tableClasses = new HashMap<String, String>();
    Map<String, String> securityClasses = new HashMap<String, String>();
	
	/**
	 * generate java classes from dynamic config
	 * @param path to hjson config
	 */
	public ElideDynamicEntityCompiler(String path) {
		try {
			elideConfigParser.parseConfigPath(path);

            tableConfig = elideConfigParser.getElideTableConfig();
            securityConfig = elideConfigParser.getElideSecurityConfig();
            tableClasses = hydrator.hydrateTableTemplate(tableConfig);
            securityClasses = hydrator.hydrateSecurityTemplate(securityConfig);

            for (Entry<String, String> entry : tableClasses.entrySet()) {
                classNames.add(PACKAGE_NAME + entry.getKey());
            }

            for (Entry<String, String> entry : securityClasses.entrySet()) {
                classNames.add(PACKAGE_NAME + entry.getKey());
            }

            compiler.useParentClassLoader(
                    new ElideDynamicInMemoryClassLoader(ClassLoader.getSystemClassLoader(),
                            Sets.newHashSet(classNames)));

		} catch (Exception e) {
			log.error("Unable to read Dynamic Configuration " + e.getMessage());
		}

	}
	/**
	 * compile table classes in-memory
	 */
	public void compile() {

		try {
			for (Map.Entry<String, String> tablePojo : tableClasses.entrySet()) {
                log.info("key: " + PACKAGE_NAME + tablePojo.getKey() + ", value: " + tablePojo.getValue());
                compiler.addSource(PACKAGE_NAME + tablePojo.getKey(), tablePojo.getValue());
            }

            for (Map.Entry<String, String> secPojo : securityClasses.entrySet()) {
                log.info("key: " + PACKAGE_NAME +  secPojo.getKey() + ", value: " + secPojo.getValue());
                compiler.addSource(PACKAGE_NAME + secPojo.getKey(), secPojo.getValue());
            }
			compiledObjects = compiler.compileAll();
		} catch (Exception e) {
			log.error("Unable to compile dynamic classes");
		}
	}

	public ClassLoader getClassLoader() {
		return compiler.getClassloader();
	}

	public Class<?> getCompiled(String name) {
		return compiledObjects.get(name);
	}
	
	public Set<Class> getBindClasses() {
		return bindClasses;
	}
}
