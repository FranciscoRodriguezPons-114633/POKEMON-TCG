# Consolidated Code Structure (May 6, 2026)

This document identifies the branch baseline intended to replace older Codex branches and serve as the canonical integration point before merge to `main`.

## Target branch
- `codex/RodriguezPonsFrancisco/code-structure`

## Included architecture baseline
- Maven multi-module parent project.
- `game-service` for game domain/application logic.
- `card-service` for Pokémon TCG API access + Redis cache.
- `realtime-service` for STOMP/WebSocket event broadcasting.
- Docker compose orchestration for local 3-service runtime + PostgreSQL + Redis.

## Build consistency
- Parent `pom.xml` defines centralized plugin management to pin:
  - `spring-boot-maven-plugin` to `${spring.boot.version}`
  - `maven-compiler-plugin` to `3.15.0`

## Test suite baseline in `game-service`
- `AttackPipelineTest`
- `SetupEngineServiceTest`
- `TurnActionValidatorTest`
- `GameEngineFacadeTest`
- `GameApplicationServiceSetupTest`
- `GameApplicationServiceFlowIntegrationTest`

## Repository cleanup intent
After this PR is merged, stale Codex branches can be safely deleted and ongoing work should continue from `main` plus feature branches created from it.
