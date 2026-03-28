package com.maxin.config;

import com.maxin.exception.WebException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(RuntimeException e) {
        throw new WebException(e.getMessage());
    }
}
