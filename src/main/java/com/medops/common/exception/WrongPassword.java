package com.medops.common.exception;

public class WrongPassword extends RuntimeException{
    public WrongPassword() {
        super("패스워드가 일치하지 않습니다.");
    }
}
