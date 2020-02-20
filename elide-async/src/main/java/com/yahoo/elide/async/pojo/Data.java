package com.yahoo.elide.async.pojo;

import java.util.UUID;

public class Data {
	private UUID id;
	private String type;
	private Attributes attributes;

	public Data(UUID id, String type, Attributes attributes) {
		this.id = id;
		this.type = type;
		this.attributes = attributes;
	}
}
