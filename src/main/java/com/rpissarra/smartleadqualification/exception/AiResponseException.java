package com.rpissarra.smartleadqualification.exception;

import com.rpissarra.smartleadqualification.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class AiResponseException extends AppException {
    public AiResponseException(String message, HttpStatus status) {
        super(message, status);
    }
}
