package com.couriertracking.demo.infrastructure.exception;

import com.couriertracking.demo.infrastructure.config.i18n.MessageTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final MessageTranslator translator;

    @ExceptionHandler(CourierNotFoundException.class)
    public ResponseEntity<String> handleCourierNotFound(CourierNotFoundException ex) {
        String message = translator.translate(ex.getMessage(), ex.getCourierId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<String> handleStoreNotFound(StoreNotFoundException ex) {
        String message = translator.translate(ex.getMessage(), ex.getStoreId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("ERR-001", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }


    record ErrorResponse(String code, String message) {}

}