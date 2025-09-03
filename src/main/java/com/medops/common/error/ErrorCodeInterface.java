package com.medops.common.error;

public interface ErrorCodeInterface {

    Integer getHttpStatusCode();
    Integer getErrorCode();
    String getDescription();
}