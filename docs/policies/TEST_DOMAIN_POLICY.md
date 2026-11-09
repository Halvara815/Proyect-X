# Política de dominios de prueba v1

El archivo `app/src/main/assets/policies/test-domain-policy-v1.txt` es un fixture propio bajo CC0-1.0. No contiene dominios reales, listas de terceros ni datos de navegación.

Propósito: validar coincidencia exacta/subdominio, allowlist, formato, hashes y futura integración DNS en emuladores o pruebas JVM. No ofrece cobertura de contenido adulto y no debe presentarse como tal.

El `DnsVpnService` carga el fixture desde el APK antes de establecer la interfaz, verifica el SHA-256 indicado y combina sus reglas con las reglas personales. La interfaz muestra `test-domain-policy-v1` como versión de la política incorporada. Una alteración, formato inválido o una entrada fuera de `.invalid` impide iniciar el servicio.

Formato deliberadamente pequeño:

```text
block adult-test.invalid
block media-test.invalid
allow safe.adult-test.invalid
```

Las extensiones `.invalid` están reservadas para ejemplos y no deben resolver en Internet público. Cualquier lista de producción requerirá fuente, licencia, versión, hash, firma y rollback verificables antes de entrar al APK.

SHA-256 del fixture v1: `a7e3b377476b2f374d304dba4d3f18411afdc5cd1c40ed0a97d1c0728f8a453e`.
