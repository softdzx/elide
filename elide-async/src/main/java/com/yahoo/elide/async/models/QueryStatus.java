package com.yahoo.elide.async.models;

public enum QueryStatus {
    COMPLETE,
    QUEUED,
    PROCESSING,
    CANCELLED,
    TIMEDOUT,
    FAILURE
}
