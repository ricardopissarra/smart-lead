package com.rpissarra.smartleadqualification.exception;

import com.rpissarra.smartleadqualification.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
