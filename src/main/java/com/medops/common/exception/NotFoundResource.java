package com.medops.common.exception;

public class NotFoundResource extends RuntimeException{
    public NotFoundResource(String message){
        super(message);
    }
}
