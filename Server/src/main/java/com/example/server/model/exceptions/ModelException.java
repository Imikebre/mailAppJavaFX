package com.example.server.model.exceptions;

public class ModelException extends Exception{

    public enum ErrorCode {
        FILE_NOT_FOUND,
        OPERATION_FAILED,
        USER_NOT_FOUND,
    }

    private final ErrorCode code;

    public ModelException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() { return code; }
}
