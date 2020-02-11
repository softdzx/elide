package com.yahoo.elide.async.model;

public enum QueryStatus {
    COMPLETE,
    QUEUED,
    PROCESSING,
    CANCELLED,
    TIMEDOUT,
    FAILURE
}
