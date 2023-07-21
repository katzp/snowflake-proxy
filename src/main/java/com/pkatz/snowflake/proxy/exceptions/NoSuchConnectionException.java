package com.pkatz.snowflake.proxy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchConnectionException extends Exception{
    public NoSuchConnectionException(String message) {
        super(message);
    }
}
