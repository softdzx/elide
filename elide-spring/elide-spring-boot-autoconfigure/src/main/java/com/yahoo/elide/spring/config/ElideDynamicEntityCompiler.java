package com.yahoo.elide.spring.config;

import com.google.common.collect.Sets;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.ElideTableToPojo;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHelper;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHydrator;

import lombok.extern.slf4j.Slf4j;

import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideTable;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.Table;

import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ElideDynamicEntityCompiler {

	public static ArrayList<String> classNames = new ArrayList<String>();

	public static final String PACKAGE_NAME = "com.yahoo.elide.spring.model.";
	private Map<String, Class<?>> compiledObjects;

	InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

	public ElideDynamicEntityCompiler(String path) {
		try {
			System.out.println("Entity compiler constructor");
			ElideTableToPojo tablePojo = new ElideTableToPojo();
			ElideTable table = tablePojo.parseTableConfigFile(path);
			HandlebarsHydrator obj = new HandlebarsHydrator();
			List<String> tableClassNames = obj.getTableClassNames(table);
			
			for (String className : tableClassNames) {
				System.out.println("classname " + className);
				classNames.add(PACKAGE_NAME + className);
			}
			compiler.useParentClassLoader(
					new InMemoryClassLoader(ClassLoader.getSystemClassLoader(), Sets.newHashSet(classNames)));

		} catch (NullPointerException | IOException e) {
			System.out.println("Unable to read Dynamic Configuration" + e.getMessage());
			e.printStackTrace();
			log.error("Unable to read Dynamic Configuration" + e.getMessage());
		}

	}

	public Map<String, String> getElideTable(String path) {

		Map<String, String> classPojoStringMap = new HashMap<String, String>();
		try {
			HandlebarsHydrator obj = new HandlebarsHydrator();
			ElideTableToPojo tablePojo = new ElideTableToPojo();
			ElideTable table = tablePojo.parseTableConfigFile(path);
			List<String> tableClassNames = obj.getTableClassNames(table);
			List<String> tablePojoStrings = obj.hydrateTableTemplate(table);

			for (int i = 0; i < tableClassNames.size(); i++) {
				System.out.println("tableClassNames.get(i)" + tableClassNames.get(i));
				classPojoStringMap.put(tableClassNames.get(i), tablePojoStrings.get(i)); // is there a clearer way?
			}

			return classPojoStringMap;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		}
		

	}

	public void compile(String path) {

		try {
			System.out.println("EntityCompiler compile method" + path);
			Map<String, String> tablePojoMap = getElideTable(path);
			
			for (Map.Entry<String, String> tablePojo : tablePojoMap.entrySet()) {
				System.out.println("key: " + tablePojo.getKey() + ", value: " + tablePojo.getValue());
				compiler.addSource(PACKAGE_NAME + tablePojo.getKey(),
							tablePojo.getValue());
				
			}
			compiledObjects = compiler.compileAll();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public ClassLoader getClassLoader() {
		return compiler.getClassloader();
	}

	public Class<?> getCompiled(String name) {
		return compiledObjects.get(name);
	}
}