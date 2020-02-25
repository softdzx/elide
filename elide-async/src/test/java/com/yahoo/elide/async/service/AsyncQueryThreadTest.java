package com.yahoo.elide.async.service;

import java.net.URISyntaxException;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.yahoo.elide.Elide;
import com.yahoo.elide.async.models.QueryType;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.graphql.QueryRunner;

public class AsyncQueryThreadTest {

	AsyncQueryThread asyncQueryThread;

	@BeforeEach
	public void setup() {
		RequestScope scope = Mockito.mock(RequestScope.class);
		Elide elide = Mockito.mock(Elide.class);
		QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
		asyncQueryThread = new AsyncQueryThread("/group?sort=commonName&fields%5Bgroup%5D=commonName,description",
				QueryType.JSONAPI_V1_0, scope, elide, queryRunner, UUID.randomUUID());
	}

	@Test
	public void testGetQueryParams() throws URISyntaxException {
		MultivaluedMap<String, String> result = asyncQueryThread.getQueryParams("/group?sort=commonName&fields%5Bgroup%5D=commonName,description");
		Assertions.assertEquals(result.get("sort").toString(), "[commonName]");
		Assertions.assertEquals(result.get("fields[group]").toString(), "[commonName,description]");
	}
}
