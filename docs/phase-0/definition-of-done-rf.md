# Definition of Done por Requerimiento Funcional (RF)

## RF-01 Reglas del juego
- [ ] Setup completo implementado y testeado: mulligan, premios, activo/banca, moneda.
- [ ] Turn manager bloquea acciones inválidas por fase/flags.
- [ ] Ataque resuelve exactamente 7 pasos definidos en RF-01c.
- [ ] Knockout aplica descarte + premio correcto + reemplazo activo.
- [ ] Condiciones especiales implementadas con incompatibilidades y orden fijo.
- [ ] Victoria/derrota implementadas (premios/KO total/deck-out) + muerte súbita.
- [ ] Tests unitarios + integración de cada punto crítico.

## RF-02 Tipos de cartas
- [ ] Modelado de Pokémon/Básico/Fase/EX/Energía/Entrenador.
- [ ] Reglas por subtipo aplicadas en motor (supporter 1 por turno, etc.).
- [ ] AS TÁCTICO validado en deck builder.

## RF-03 Gestión del juego
- [ ] Estados WAITING/SETUP/ACTIVE/FINISHED funcionales.
- [ ] Backend como fuente de verdad (frontend sin lógica de reglas).
- [ ] Action log completo por turno/jugador/resultado.
- [ ] Persistencia por acción y reconstrucción de partida.

## RF-04 Construcción de mazos
- [ ] Deck builder integrado con `pokemontcg.io` filtrando `xy1`.
- [ ] Validaciones oficiales completas de mazo.
- [ ] CRUD mazos por jugador.

## RF-05 Persistencia
- [ ] Snapshot de estado completo después de cada acción relevante.
- [ ] Rehidratación completa desde persistencia.
- [ ] Log inmutable y consultable.

## RF-06 Tiempo real
- [ ] WebSocket bidireccional funcional.
- [ ] Broadcast de estado y eventos relevantes a ambos clientes.
- [ ] Reconexión robusta con sync del último estado.

## RF-07 Interfaz
- [ ] Lobby funcional.
- [ ] Tablero con zonas y panel de acciones por fase.
- [ ] Drag & drop de acciones mínimas.
- [ ] Feedback visual y log de eventos.
