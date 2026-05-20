package com.utn.pokemontcg.card.presentation;

import com.utn.pokemontcg.card.application.CardProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class CardApiExceptionHandler {

    @ExceptionHandler(CardProviderException.class)
    public ResponseEntity<Map<String, Object>> providerUnavailable(CardProviderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
            "timestamp", Instant.now(),
            "status", HttpStatus.BAD_GATEWAY.value(),
            "error", "Bad Gateway",
            "message", ex.getMessage()
        ));
    }
}
