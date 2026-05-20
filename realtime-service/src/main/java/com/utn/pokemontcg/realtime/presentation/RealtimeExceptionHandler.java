package com.utn.pokemontcg.realtime.presentation;

import com.utn.pokemontcg.realtime.application.InvalidGameEventException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class RealtimeExceptionHandler {

    @ExceptionHandler(InvalidGameEventException.class)
    public ResponseEntity<Map<String, Object>> invalidEvent(InvalidGameEventException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Evento invalido");
        return badRequest(message);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", Instant.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Bad Request",
            "message", message
        ));
    }
}
