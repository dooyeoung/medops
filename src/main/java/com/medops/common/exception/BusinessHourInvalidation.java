package com.medops.common.exception;

public class BusinessHourInvalidation extends RuntimeException{
    public BusinessHourInvalidation(String message){
        super(message);
    }
}
