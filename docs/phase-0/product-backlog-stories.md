# Backlog técnico por historias (Fase 0)

> Formato: **ID — Historia — Criterios de aceptación — Estimación**

## Épica A — Motor de juego (RF-01)

### A1 — Setup inicial completo
Como motor, quiero ejecutar setup oficial para iniciar partidas válidas.
- Criterios:
  - aplica mulligan iterativo,
  - setea activo/banca/premios,
  - define primer jugador por moneda.
- Estimación: 8 pts

### A2 — Turn manager con fases y flags
Como backend, quiero validar acciones por fase y límites por turno.
- Criterios:
  - flags: energía/retiro/partidario,
  - bloqueo de ataque primer turno inicial.
- Estimación: 8 pts

### A3 — Attack pipeline RF-01c real
Como backend, quiero resolver ataques en 7 pasos exactos.
- Criterios:
  - energía/confusión/selección/pre/modificadores/daño/post,
  - resultados deterministas y auditables.
- Estimación: 13 pts

### A4 — Knockout y premios
Como motor, quiero aplicar KO y premios según reglas.
- Criterios:
  - premio 1 normal, 2 para EX,
  - reemplazo activo o derrota por KO total.
- Estimación: 5 pts

### A5 — Condiciones especiales
Como motor, quiero procesar condiciones especiales entre turnos.
- Criterios:
  - 5 condiciones,
  - incompatibilidades correctas,
  - orden fijo de resolución.
- Estimación: 8 pts

### A6 — Victory checker + muerte súbita
Como motor, quiero declarar ganador correctamente.
- Criterios:
  - premios/KO/deck-out,
  - muerte súbita si simultáneo.
- Estimación: 5 pts

## Épica B — Persistencia y trazabilidad (RF-03/RF-05)

### B1 — Snapshot completo de estado
Como sistema, quiero persistir estado total tras cada acción.
- Criterios:
  - tablero/manos/mazos/premios/flags/condiciones.
- Estimación: 8 pts

### B2 — Action log inmutable
Como sistema, quiero registrar cada acción para auditoría y replay.
- Criterios:
  - turno/jugador/acción/resultado/timestamp.
- Estimación: 5 pts

### B3 — Reconstrucción de partida
Como sistema, quiero rehidratar una partida al reconectar.
- Criterios:
  - reconstrucción exacta sin pérdida.
- Estimación: 5 pts

## Épica C — Card/Deck (RF-04)

### C1 — Cache local de cartas xy1
Como card-service, quiero cachear set xy1 para uso interno del motor.
- Criterios:
  - sync inicial + refresh programado.
- Estimación: 5 pts

### C2 — Deck builder CRUD + validación
Como jugador, quiero gestionar mazos válidos.
- Criterios:
  - 60 cartas,
  - máximo 4 copias salvo básica,
  - 1 AS TÁCTICO,
  - al menos 1 básico.
- Estimación: 8 pts

## Épica D — Realtime (RF-06)

### D1 — Contrato de eventos de juego
Como frontend, quiero eventos claros para renderizar estado.
- Criterios:
  - TURN_STARTED, ATTACK_RESOLVED, KO, PRIZE_TAKEN, STATUS_APPLIED, GAME_FINISHED.
- Estimación: 5 pts

### D2 — Reconexión robusta
Como jugador, quiero retomar partida al reconectar.
- Criterios:
  - handshake de sesión + estado actual + eventos pendientes.
- Estimación: 5 pts

## Épica E — Frontend tablero (RF-07)

### E1 — Lobby funcional
### E2 — Tablero con zonas + panel de acciones
### E3 — Drag & drop mínimo
### E4 — Log y feedback visual
- Estimación total: 21 pts

## Épica F — Calidad (RNF)

### F1 — Cobertura y JaCoCo
- Criterios:
  - 80% global,
  - >90% componentes críticos.
- Estimación: 8 pts

### F2 — Tests de integración + E2E
- Criterios:
  - flujos principales obligatorios.
- Estimación: 8 pts

---

## Priorización para sprint inmediato (orden)
1. A1, A2, A3
2. A4, A5, A6
3. B1, B2, B3
4. C2, D1, D2
5. E1-E4 + F1/F2
