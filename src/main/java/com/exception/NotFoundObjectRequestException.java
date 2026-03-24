package com.exception;

public class NotFoundObjectRequestException extends RuntimeException{
    public NotFoundObjectRequestException(String message){
        super(message);
    }
}

