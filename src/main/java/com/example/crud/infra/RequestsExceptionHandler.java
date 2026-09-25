package com.example.crud.infra;

import com.example.crud.service.ViaCepException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RequestsExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionDTO> handle404() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO("Data not found with provided ID", 404));
    }

    @ExceptionHandler(ViaCepException.class)
    public ResponseEntity<ExceptionDTO> handleViaCepException(ViaCepException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(exception.getMessage(), 400));
    }
}
