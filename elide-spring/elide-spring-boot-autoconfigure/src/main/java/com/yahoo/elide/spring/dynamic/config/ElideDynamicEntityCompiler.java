package com.yahoo.elide.spring.dynamic.config;

import com.google.common.collect.Sets;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.ElideTableToPojo;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHydrator;

import lombok.extern.slf4j.Slf4j;

import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideTable;

import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ElideDynamicEntityCompiler {

	public static ArrayList<String> classNames = new ArrayList<String>();

	public static final String PACKAGE_NAME = "com.yahoo.elide.contrib.dynamicconfig.model.";
	private Map<String, Class<?>> compiledObjects;

	InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

	HandlebarsHydrator hydrator = new HandlebarsHydrator();
	
	ElideTableToPojo tablePojo = new ElideTableToPojo();
	
	public ElideDynamicEntityCompiler(String path) {
		try {
			ElideTable table = tablePojo.parseTableConfigFile(path);
			List<String> tableClassNames = hydrator.getTableClassNames(table);
			
			for (String className : tableClassNames) {
				classNames.add(PACKAGE_NAME + className);
				
			}
			compiler.useParentClassLoader(
					new ElideDynamicInMemoryClassLoader(ClassLoader.getSystemClassLoader(), Sets.newHashSet(classNames)));

		} catch (NullPointerException | IOException e) {
			log.error("Unable to read Dynamic Configuration " + e.getMessage());
		}

	}

	public Map<String, String> getTablePojoMap(String path) {

		Map<String, String> classPojoStringMap = new HashMap<String, String>();
		
		try {
			
			ElideTable table = tablePojo.parseTableConfigFile(path);
			List<String> tableClassNames = hydrator.getTableClassNames(table);
			List<String> tablePojoStrings = hydrator.hydrateTableTemplate(table);

			for (int i = 0; i < tableClassNames.size(); i++) {
				log.debug("Table Class " + tableClassNames.get(i));
				classPojoStringMap.put(tableClassNames.get(i), tablePojoStrings.get(i));
			}

			return classPojoStringMap;
		} catch (Exception e) {
			log.error("Unable to get Table Pojo "+e.getMessage());
			return null;
		}
		

	}

	public void compile(String path) {

		try {
			Map<String, String> tablePojoMap = getTablePojoMap(path);
			
			for (Map.Entry<String, String> tablePojo : tablePojoMap.entrySet()) {
				log.info("key: " + tablePojo.getKey() + ", value: " + tablePojo.getValue());
				compiler.addSource(PACKAGE_NAME + tablePojo.getKey(),
							tablePojo.getValue());
				
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
}