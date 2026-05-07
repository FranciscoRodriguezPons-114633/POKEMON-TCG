# Sprint 1 — Plan de ejecución (A1 + A2 + A3)

## 1) Congelar una sola rama de trabajo

## Rama objetivo
- `work` (única rama de desarrollo activa para Sprint 1).

## Política de ramas
- No abrir ramas paralelas para el mismo objetivo funcional.
- Solo se permite:
  - `work` (integración de sprint),
  - ramas cortas por bug crítico con prefijo `hotfix/`.
- Todo merge a `main` sale desde `work` al cierre del sprint.

## Checklist de congelamiento
- [ ] Cerrar PRs/ramas duplicadas previas.
- [ ] Marcar `work` como rama fuente para el sprint.
- [ ] Documentar scope cerrado del sprint (A1, A2, A3).

---

## 2) Bloque único de trabajo: A1 + A2 + A3

## Objetivo
Entregar flujo funcional mínimo de motor para:
`create game -> join -> setup -> turn phases -> attack pipeline`

## Historias incluidas
- **A1 — Setup inicial completo**
  - mulligan iterativo,
  - activo/banca,
  - premios,
  - sorteo de jugador inicial.
- **A2 — Turn manager con fases y flags**
  - fases DRAW/MAIN/ATTACK/BETWEEN_TURNS,
  - validación de flags por turno,
  - bloqueo de acciones inválidas por fase.
- **A3 — Attack pipeline RF-01c**
  - 7 pasos exactos,
  - resultados deterministas/auditables.

## Entregables técnicos mínimos
- Reglas de setup completas y testeadas.
- Validación centralizada de acciones por fase.
- Pipeline de ataque ejecutable extremo a extremo en partida real.

---

## 3) Definition of Done del sprint

## DoD funcional
- [ ] Se puede crear partida.
- [ ] Se puede unir segundo jugador.
- [ ] Setup completo termina en estado jugable.
- [ ] Se ejecuta al menos un turno válido con transición de fases.
- [ ] Se resuelve un ataque con pipeline de 7 pasos.

## DoD de calidad
- [ ] Test de integración obligatorio del flujo:
  `create game -> join -> setup -> turn -> attack`
- [ ] Cobertura mínima del flujo core >= 80%.
- [ ] Cobertura de validadores/pipeline crítico >= 90%.

## DoD de entrega
- [ ] README actualizado con cómo correr tests de integración.
- [ ] Evidencia de test run adjunta en PR (salida de Maven).

---

## 4) Secuencia recomendada de implementación
1. Cerrar A1 (setup completo + tests de unidad).
2. Cerrar A2 (fases/flags + tests de unidad).
3. Cerrar A3 (pipeline real + tests de unidad).
4. Implementar test de integración end-to-end del flujo.
5. Recién luego pasar a A4/A5/A6 (KO, condiciones, victoria).

---

## 5) Riesgos y mitigaciones
- **Riesgo:** reglas acopladas al controlador HTTP.
  - **Mitigación:** mantener lógica en application/domain services.
- **Riesgo:** pruebas frágiles por estado compartido.
  - **Mitigación:** fixtures deterministas y repositorios aislados por test.
- **Riesgo:** cambio de scope a mitad del sprint.
  - **Mitigación:** alcance congelado (solo A1, A2, A3).

---

## Anexo operativo (actualizado 2026-05-06)
- Se formaliza gobernanza de ejecución en `docs/phase-1/operational-governance-day-0.md`.
- Para todo Sprint 3 se exige owner por dominio + criterio técnico + riesgo + prueba obligatoria por historia.
