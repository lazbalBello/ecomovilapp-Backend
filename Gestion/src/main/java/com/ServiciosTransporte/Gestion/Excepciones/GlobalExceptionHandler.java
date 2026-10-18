package com.ServiciosTransporte.Gestion.Excepciones;

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
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        String msg = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", ex.getStatusCode().value());
        body.put("error", msg);
        body.put("mensage", msg);
        body.put("message", msg);
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<?> handleUnexpectedRollback(UnexpectedRollbackException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.CONFLICT.value());
        body.put("error", "Error de integridad: la transacción fue revertida debido a una inconsistencia de datos.");
        body.put("mensage", ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerGeneralException(Exception ex, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Error interno del servidor");
        body.put("mensage", ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDuplicatedKey(DataIntegrityViolationException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.CONFLICT.value());
        body.put("error", "Valor duplicado " +  obtenerValorDuplicado(ex));

        String mensajeExtra = obtenerValorDuplicado(ex);
        body.put("mensage", mensajeExtra);
        body.put("message", mensajeExtra);
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
        body.put("message", ex.getMessage());
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
        body.put("message", ex.getMessage());
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
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request){
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage() != null ? error.getDefaultMessage() : "Campo inválido")
        );

        String generalMessage = fieldErrors.values().stream().findFirst().orElse("Error de validación en la petición");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error de validación");
        body.put("mensage", generalMessage);
        body.put("message", generalMessage);
        body.put("fieldErrors", fieldErrors);
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handelConstraintViolationException(ConstraintViolationException ex, WebRequest request){
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        String generalMessage = ex.getMessage();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("estado", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error de validación");
        body.put("mensage", generalMessage);
        body.put("message", generalMessage);
        body.put("fieldErrors", fieldErrors);
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
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
