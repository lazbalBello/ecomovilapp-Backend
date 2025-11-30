package com.ServiciosTransporte.Gestion.Exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.AuthenticationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerGeneralException(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDuplicatedKey(DataIntegrityViolationException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.CONFLICT.value());
        body.put("error", "Valor duplicado " +  obtenerValorDuplicado(ex));

        String mensajeExtra = obtenerValorDuplicado(ex);
        body.put("mensage", mensajeExtra);
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handlerEntityNotfoundException(EntityNotFoundException ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.NOT_FOUND.value());
        body.put("error", "Entidad no encontrada");
        body.put("mensage", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handlerAuthenticationException(AuthenticationException ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Autenticación fallida");
        body.put("mensage", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handlerIllegalArgumentException(IllegalArgumentException ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.BAD_REQUEST);
        body.put("error", "Argumentos inválidos");
        body.put("mensage", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.BAD_REQUEST);
        body.put("error", "Error de validación");
        body.put("mensage", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handelConstraintViolationException(ConstraintViolationException ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.BAD_REQUEST);
        body.put("error", "Error de validación");
        body.put("mensage", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

    private String obtenerValorDuplicado(DataIntegrityViolationException ex) {
        Throwable causa = ex.getRootCause();
        if (causa != null && causa.getMessage() != null) {
            String msg = causa.getMessage();
            Pattern patron = Pattern.compile("\\((.*?)\\)=\\((.*?)\\)");
            Matcher matcher = patron.matcher(msg);
            if (matcher.find()) {
                String campo = matcher.group(1);
                String valor = matcher.group(2);
                return "Ya existe -> " + campo + ": " + valor;
            }
            if (msg.toLowerCase().contains("duplicate entry")) {
                Pattern patronMySQL = Pattern.compile("Duplicate entry '(.+?)' for key");
                Matcher matcherMySQL = patronMySQL.matcher(msg);
                if (matcherMySQL.find()) {
                    return "Ya existe este valor: " + matcherMySQL.group(1);
                }
            }
        }
        return "Ya existe un valor con un campo único repetido.";
    }
}
