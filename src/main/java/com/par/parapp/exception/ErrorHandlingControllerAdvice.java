package com.par.parapp.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.par.parapp.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.hibernate.exception.GenericJDBCException;
import org.postgresql.util.PSQLException;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({ ConstraintViolationException.class,
            BadCredentialsException.class, SQLException.class, NotEnoughBalanceException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ UsernameNotFoundException.class, ResourceNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException e) {
        ErrorResponse errorResponse = new ErrorResponse("Неверный формат, поле: " + e.getValue());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<ErrorResponse> handleMismatchedInputException(MismatchedInputException e) {
        ErrorResponse errorResponse = new ErrorResponse("Неверный формат, поле: " + e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException e) {
        String message = "Ошибка базы данных: " + extractDatabaseErrorMessage(e);
        ErrorResponse errorResponse = new ErrorResponse(message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String extractDatabaseErrorMessage(Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            // Проверяем, связано ли исключение с PostgreSQL
            if (cause instanceof PSQLException) {
                PSQLException psqlException = (PSQLException) cause;
                if (psqlException.getServerErrorMessage() != null) {
                    return psqlException.getServerErrorMessage().getMessage();
                }
            }
            cause = cause.getCause(); // Пробуем получить более глубокую причину
        }
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("Ошибка: " + e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
