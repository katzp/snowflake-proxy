package com.pkatz.snowflake.proxy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidConnectionException extends Exception {
    public InvalidConnectionException(String message) {
        super(message);
    }
}
