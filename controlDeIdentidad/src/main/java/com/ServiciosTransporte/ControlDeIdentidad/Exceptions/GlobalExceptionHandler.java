package com.ServiciosTransporte.ControlDeIdentidad.Exceptions;

import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.OperationNotAllowedException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserAlreadyExistsException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserCreationException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<Object> handleOperationNotAllowedException(OperationNotAllowedException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.FORBIDDEN.value(), ex.getMessage(), request),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.CONFLICT.value(), ex.getMessage(), request),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<Object> handleUserCreationException(UserCreationException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handlerAuthenticationException(AuthenticationException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.UNAUTHORIZED.value(),
                "Autenticación fallida", request), HttpStatus.UNAUTHORIZED);
    }

    /** Lanzada por TokenClient al autenticar contra el token endpoint de Keycloak. */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<?> handlerAuthenticationFailedException(AuthenticationFailedException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(), request), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handlerIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage() != null ? error.getDefaultMessage() : "Campo inválido")
        );

        String generalMessage = fieldErrors.values().stream().findFirst().orElse("Error de validación en la petición");

        Map<String, Object> body = cuerpoBase(HttpStatus.BAD_REQUEST.value(), generalMessage, request);
        body.put("fieldErrors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handelConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        String generalMessage = ex.getMessage();

        Map<String, Object> body = cuerpoBase(HttpStatus.BAD_REQUEST.value(), generalMessage, request);
        body.put("fieldErrors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.NOT_FOUND.value(),
                "Ruta no encontrada: el endpoint solicitado no existe", request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return new ResponseEntity<>(cuerpoBase(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método HTTP " + ex.getMethod() + " no soportado para este endpoint", request), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerGeneralException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(cuerpoBase(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor", request));
    }

    private Map<String, Object> cuerpoBase(int estado, String error, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", estado);
        body.put("error", error);
        body.put("path", request.getDescription(false));
        return body;
    }
}
