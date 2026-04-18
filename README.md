# Pokémon TCG - Digital Implementation

> Implementación digital del Pokémon Trading Card Game (TCG) como Trabajo Práctico Integrador de Programación III - UTN FRC

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21+-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-Academic-success.svg)]()

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Testing](#-testing)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [API Documentation](#-api-documentation)
- [Roadmap](#-roadmap)
- [Equipo](#-equipo)
- [Licencia](#-licencia)

## 📖 Descripción

Versión digital completamente funcional del Pokémon TCG que permite a dos jugadores competir en tiempo real siguiendo las reglas oficiales del reglamento XY1. El proyecto implementa un motor de juego completo con todas las mecánicas del juego original, incluyendo sistema de turnos, resolución de ataques, condiciones especiales y múltiples condiciones de victoria.

### Objetivos del Proyecto

- ✅ Implementar todas las reglas oficiales del Pokémon TCG (basadas en XY1 Rulebook)
- ✅ Comunicación en tiempo real mediante WebSockets
- ✅ Arquitectura cliente-servidor robusta y escalable
- ✅ Aplicación de patrones de diseño y principios SOLID
- ✅ Cobertura de tests > 80% (>90% en componentes críticos)
- ✅ Integración con API pública pokemontcg.io

## ✨ Características

### Funcionalidades Core

#### 🎴 Deck Builder
- Construcción y validación de mazos según reglas oficiales
- Exactamente 60 cartas por mazo
- Máximo 4 copias por carta (excepto Energía Básica)
- Máximo 1 AS TÁCTICO por mazo
- Mínimo 1 Pokémon Básico
- Integración con set XY (xy1 - 146 cartas)

#### 🎮 Motor de Juego Completo
- Preparación de partida con sistema de Mulligan
- Gestión de turnos con fases: DRAW → MAIN → ATTACK → BETWEEN_TURNS
- Resolución de ataques con pipeline de 7 pasos
- Sistema de knockout y toma de cartas de Premio
- 5 condiciones especiales: Dormido, Quemado, Confundido, Paralizado, Envenenado
- Múltiples condiciones de victoria

#### 🔄 Tiempo Real
- Sincronización de estado vía WebSockets
- Notificaciones de eventos en tiempo real
- Reconexión automática tras desconexión

#### 🖥️ Interfaz Interactiva
- Tablero visual con zonas de juego claramente definidas
- Sistema drag & drop para acciones de juego
- Feedback visual inmediato
- Log de acciones en tiempo real

### Características Técnicas

- 🏗️ **Arquitectura en Capas** (Presentation → Application → Domain → Infrastructure)
- 🎯 **Patrones de Diseño**: State, Strategy, Chain of Responsibility, Observer, Repository, Facade
- 💾 **Persistencia Dual**: PostgreSQL (estado persistente) + Redis (caché + sesiones)
- 🔒 **Validación Backend**: Toda lógica de juego validada en servidor
- 📊 **Trazabilidad**: Log completo e inmutable de todas las acciones
- 🧪 **Alta Cobertura de Tests**: JUnit, Mockito, tests E2E

## 🏛️ Arquitectura

### Diagrama de Arquitectura General
