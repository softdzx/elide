package com.yahoo.elide.standalone.dynamic.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.mdkt.compiler.DynamicClassLoader;

import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElideDynamicInMemoryClassLoader extends DynamicClassLoader {

	private Set<String> classNames = Sets.newHashSet();

	public ElideDynamicInMemoryClassLoader(ClassLoader parent, Set<String> classNames) {
		super(parent);
		setClassNames(classNames);
	}

	public void setClassNames(Set<String> classNames) {
		this.classNames = classNames;
	}
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return super.loadClass(name);
	}

	@Override
	protected URL findResource(String name) {
		log.debug("Finding Resource "+name +" in "+classNames);
		if (classNames.contains(name.replace("/", ".").replace(".class", ""))) {
			try {
				log.debug("Returning Resource "+"file://" + name);
				return new URL("file://" + name);
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}
		return super.findResource(name);
	}
}
