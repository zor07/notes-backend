package com.zor07.notesbackend.exception;

public class IllegalAuthorizationHeaderException  extends RuntimeException {
    public IllegalAuthorizationHeaderException(String message) {
        super(message);
    }
}
