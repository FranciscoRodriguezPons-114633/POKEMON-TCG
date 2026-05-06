# Fase 0 — Alcance MVP Evaluable (Congelado)

## Objetivo del MVP
Entregar una versión **jugable de punta a punta** para 2 jugadores, con reglas XY1 críticas implementadas en backend y sincronización en tiempo real.

## Incluye (IN)

### Backend (obligatorio)
1. Crear mazo válido para set `xy1`.
2. Crear partida y unir segundo jugador.
3. Setup inicial:
   - robar 7,
   - mulligan completo,
   - activo + banca,
   - 6 premios,
   - sorteo de inicio.
4. Turno completo con fases:
   - DRAW,
   - MAIN,
   - ATTACK,
   - BETWEEN_TURNS.
5. Resolución de ataque en 7 pasos RF-01c.
6. Knockout y toma de premios (1 o 2 para EX).
7. Condiciones especiales (5) con incompatibilidades y orden entre turnos.
8. Condiciones de victoria:
   - por premios,
   - por KO total,
   - por deck-out.
9. Persistencia de estado completo por acción + log inmutable.
10. Realtime WebSocket:
   - sincronización de estado,
   - eventos de juego,
   - reconexión con rehidratación.

### Frontend (obligatorio)
1. Lobby: crear/unir partida.
2. Deck Builder:
   - búsqueda de cartas (`xy1`),
   - armado y validación de mazo,
   - guardar/editar/eliminar/listar mazos.
3. Tablero:
   - zonas de juego,
   - panel de acciones,
   - log visible,
   - estados/HP/condiciones en tiempo real.
4. Flujo básico drag & drop para:
   - bajar básico a banca,
   - unir energía,
   - jugar entrenador con target.

### Calidad mínima
1. Cobertura global backend >= 80%.
2. Cobertura >= 90% en RuleValidator, DamageCalculator, StatusEffectManager.
3. Integración backend para partida completa + mulligan + KO + victoria.
4. 1 E2E frontend mínimo del flujo completo.

## No incluye en MVP (OUT)
1. Ranking/historial avanzado.
2. Chat in-game.
3. Megaevoluciones (opcional).
4. Animaciones avanzadas.
5. Optimizaciones no críticas.

## Criterio de congelamiento de alcance
Una vez iniciada Fase 1, **todo pedido nuevo** va al backlog `POST-MVP` salvo que destrabe un RF obligatorio.
