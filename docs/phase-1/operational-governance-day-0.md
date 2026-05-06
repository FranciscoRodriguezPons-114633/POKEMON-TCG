# Etapa 0 — Gobernanza operativa (Día 1)

## Objetivo
Evitar dispersión, reducir retrabajo y fijar una mecánica de toma de decisiones única para Sprint 3.

## 1) Rama activa única (backend)
- Rama activa oficial: `codex/generate-complete-backend-code-structure-uuswmf`.
- Política:
  - No abrir ramas paralelas para el mismo objetivo funcional.
  - Toda implementación backend de Sprint 3 sale de esta rama.
  - Ramas `hotfix/*` solo para incidentes críticos bloqueantes.

## 2) Responsables de decisión por dominio
- **Owner A — Game rules**
  - Decide: reglas de motor, setup, turnos, KO, victoria, validaciones de acción.
  - Aprueba cambios en `game-service/domain` y `game-service/application`.

- **Owner B — Realtime/contratos**
  - Decide: contrato de eventos, versionado de payloads, reconexión, sincronización estado/eventos.
  - Aprueba cambios en `realtime-service` + integración con `game-service`.

- **Owner C — Calidad/tests**
  - Decide: estrategia de tests, umbrales de cobertura, criterios de merge.
  - Aprueba cambios en tests, pipeline de calidad y evidencia de validación.

## 3) Tablero operativo
Columnas obligatorias:
1. `Ready`
2. `In Progress`
3. `Blocked`
4. `Review`
5. `Done`

Reglas de flujo:
- Solo entra a `In Progress` si tiene owner, criterio técnico y prueba obligatoria.
- Si aparece dependencia externa, pasa a `Blocked` con motivo explícito.
- Ningún ticket entra en `Done` sin evidencia de test.

## 4) Definición de entrada obligatoria por historia
Cada historia/ticket debe incluir:
1. **Criterio de aceptación técnico**
2. **Riesgo principal**
3. **Prueba obligatoria**

Plantilla mínima sugerida:
- Historia:
- Criterio técnico:
- Riesgo:
- Prueba obligatoria:
- Owner:

## 5) Acuerdos de merge
- Merge permitido únicamente si:
  - cumple aceptación técnica,
  - tiene evidencia de prueba obligatoria,
  - no deja conflictos de contrato con otros servicios.
- Si hay conflicto de contrato realtime, define Owner B.
- Si hay conflicto de exactitud de reglas, define Owner A.
- Si hay conflicto por cobertura o deuda de test, define Owner C.

## 6) Checklist de ejecución inmediata
- [ ] Confirmar rama activa única y comunicarla al equipo.
- [ ] Asignar Owner A, Owner B y Owner C con nombres concretos.
- [ ] Crear tablero con columnas estándar.
- [ ] Migrar backlog Sprint 3 a tickets con plantilla obligatoria.
- [ ] Bloquear inicio de tickets sin prueba obligatoria definida.
