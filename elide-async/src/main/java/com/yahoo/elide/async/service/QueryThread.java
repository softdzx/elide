package com.yahoo.elide.async.service;

import java.net.URISyntaxException;
import java.security.Principal;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.async.models.QueryStatus;
import com.yahoo.elide.async.models.QueryType;
import com.yahoo.elide.async.pojo.AsyncQueryRequest;
import com.yahoo.elide.async.pojo.Attributes;
import com.yahoo.elide.async.pojo.Data;
import com.yahoo.elide.graphql.QueryRunner;
import com.yahoo.elide.security.RequestScope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryThread implements Runnable {

	private String query;
	private QueryType queryType;
	private RequestScope scope;
	private Elide elide;
	private QueryRunner runner;
	private UUID id;

    public QueryThread(String query, QueryType queryType, RequestScope scope, Elide elide, QueryRunner runner, UUID id){
        log.info("New thread created");
        this.query = query;
        this.queryType = queryType;
        this.scope = scope;
        this.elide = elide;
        this.runner = runner;
        this.id = id;
    }

    @Override
    public void run() {
        processQuery();
    }

    private void processQuery() {
        try {
            // Change async query to processing
            updateAsyncQueryStatus(QueryStatus.PROCESSING, id);
            //Just doing sleep for testing
            Thread.sleep(60000);
            ElideResponse response = null;
            log.info("query: {}", query);
            log.info("queryType: {}", queryType);
            Principal principal = ((Principal) scope.getUser().getOpaqueUser());
            log.info("Principal name: {}", principal.getName());
            if (queryType.equals(QueryType.JSONAPI_V1_0)) {
                MultivaluedMap<String, String> queryParams = getQueryParams(query);
                response = elide.get(getPath(query), queryParams, scope.getUser().getOpaqueUser());
                log.info("JSONAPI_V1_0 getResponseCode: {}", response.getResponseCode());
                log.info("JSONAPI_V1_0 getBody: {}", response.getBody());
            }
            else if (queryType.equals(QueryType.GRAPHQL_V1_0)) {
                response = runner.run(query, principal);
                log.info("GRAPHQL_V1_0 getResponseCode: {}", response.getResponseCode());
                log.info("GRAPHQL_V1_0 getBody: {}", response.getBody());
            }
            // if 200 - response code then Change async query to complete else change to Failure
            // add async query result no matter what the response
            if (response.getResponseCode() == 200) {
                updateAsyncQueryStatus(QueryStatus.COMPLETE, id);
            } else {
                updateAsyncQueryStatus(QueryStatus.FAILURE, id);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method parses the url and gets the query params and adds them into a MultivaluedMap
     * to be used by underlying Elide.get method
     * @param query query from the Async request
     * */
	private MultivaluedMap<String, String> getQueryParams(String query) {
		URIBuilder uri;
		try {
			uri = new URIBuilder(query);
			MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
			for (NameValuePair queryParam : uri.getQueryParams()) {
				queryParams.add(queryParam.getName(), queryParam.getValue());
				return queryParams;
			}
			log.info("QueryParams: {}", queryParams);
		} catch (URISyntaxException e) {
			log.error("URISyntaxException: {}", e.getMessage());
		}
		return null;
	}

    /**
     * This method parses the url and gets the query params and retrieves path
     * to be used by underlying Elide.get method
     * @param query query from the Async request
     * */
	private String getPath(String query) {
		URIBuilder uri;
		try {
			uri = new URIBuilder(query);
			return uri.getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * This method updates the model for AsyncQuery
     * @param status new status based on the enum QueryStatus
     * */
	private void updateAsyncQueryStatus(QueryStatus status, UUID asyncQueryId) {
		String contentType = "application/vnd.api+json";
		String accept = "application/vnd.api+json";
		StringBuilder path = new StringBuilder().append("query/").append(asyncQueryId.toString());
		String jsonApiDocument = jsonRequestBuilder(status, asyncQueryId);
		elide.patch(contentType, accept, path.toString(), jsonApiDocument, scope.getUser().getOpaqueUser());
	}

	/**
	 * This method updates the model for AsyncQuery
	 * @param status new status based on the enum QueryStatus
	 * */
	private String jsonRequestBuilder(QueryStatus status, UUID asyncQueryId) {
		Attributes attributes = new Attributes(status);
		Data data = new Data(asyncQueryId, "query", attributes);
		AsyncQueryRequest request = new AsyncQueryRequest(data);
		Gson gsonBuilder = new GsonBuilder().create();
		String jsonFromPojo = gsonBuilder.toJson(request);
		return jsonFromPojo;
	}
}
