# ADR-001: Proyect X, reconstrucción independiente y offline-first

**Estado:** aceptada inicialmente
**Fecha:** 2026-11-08

## Contexto

El repositorio parte sin código fuente ni artefacto verificable del producto de referencia. La información disponible procede de fichas públicas de tiendas y debe distinguirse de evidencia de un binario.

## Decisión

Construir una aplicación Android nueva, con módulos locales y permisos opcionales, a partir de una especificación de comportamientos. No incorporar código decompilado, recursos, marca, endpoints ni datos de la aplicación de referencia.

## Consecuencias

- Se puede publicar el repositorio sin distribuir APKs de terceros.
- La primera versión puede cubrir flujos propios de reglas locales sin depender de backend.
- La paridad de comportamiento permanece como hipótesis hasta completar el análisis estático y las pruebas autorizadas.
- Cualquier cuenta, sincronización, pago o mecanismo de bloqueo resistente a desinstalación necesita una decisión de arquitectura y seguridad posterior.
