# Proyect X — bienestar digital local-first

**Proyect X** es una aplicación Android independiente de bienestar digital. La investigación comparativa utiliza comportamientos observables de aplicaciones de bloqueo, sin incorporar su código, recursos, marcas ni secretos.

El código y la documentación propios de este repositorio se publican bajo [Apache-2.0](LICENSE). Los artefactos de terceros y sus APK no forman parte del repositorio ni de esta licencia.

## Estado

| Área | Estado | Evidencia |
| --- | --- | --- |
| Motor de reglas local | Implementado | Reglas de dominio, palabra y paquete; Allow tiene prioridad |
| Bloqueo de paquetes Android | Implementado, opcional | Servicio de accesibilidad que usa solo el paquete en primer plano |
| Panel y sesión de enfoque | Implementado | Dashboard propio y temporizador local sin red |
| Límite diario por aplicación | Implementado localmente, pendiente de validación OEM | Requiere Acceso de uso y Accesibilidad habilitados por la persona; no guarda totales |
| Filtro DNS de dominios | Prototipo local verificado en AVD, pendiente de validación multi-navegador | Política incorporada `test-domain-policy-v1` verificada por SHA-256; bloqueo DNS UDP verificado en WebView y resolvedor nativo de AVD API 34 |

## Investigación de referencia

La investigación comparativa puede consultarse en [el libro mayor de evidencia](docs/INVESTIGACION.md). La aplicación se desarrolla como una implementación propia y no busca replicar código ni apariencia ajenos.

## Cómo continuar la ingeniería inversa

1. Conserva una copia del APK obtenida por un canal autorizado fuera del repositorio.
2. Registra SHA-256, versión y límites de la evidencia en `docs/INVESTIGACION.md`.
3. Ejecuta REA de manera estática y convierte solo comportamientos confirmados en requisitos propios.

No subas APKs de terceros, datos de usuarios, tokens, claves ni capturas con información personal al repositorio.

## Ejecutar Proyect X

Requisitos: JDK 17 y Android SDK con plataforma 37. El wrapper de Gradle está incluido.

```powershell
# Crea local.properties en tu copia local (no se versiona) con esta línea:
# sdk.dir=C\:\ruta\al\Android\Sdk

.\gradlew.bat :app:assembleDebug
```

El APK de desarrollo se genera en `app/build/outputs/apk/debug/`. Puede devolver al inicio al abrir una app cuyo paquete esté bloqueado, sólo después de que la persona active el servicio de accesibilidad desde Ajustes. Incluye un prototipo de transporte DNS UDP para pruebas de ingeniería; no se debe presentar como filtro web ni publicar hasta resolver la evidencia de bypass documentada.

## Configurar y desactivar protección

1. Crea una regla de paquete o, para un límite diario, usa **Configurar presupuesto** y elige una aplicación.
2. Para que se aplique un presupuesto, revisa la divulgación y habilita **Acceso de uso** desde Ajustes; después habilita el servicio de **Accesibilidad** de Proyect X. Sin ambas autorizaciones, un presupuesto no bloquea.
3. Para quitar una regla o presupuesto, abre Proyect X y usa su botón **Quitar**. Para detener toda intervención, deshabilita el servicio de Accesibilidad en Ajustes.
4. Para el filtro DNS opcional, abre **Cobertura web**, lee su divulgación, concede notificaciones y aprueba el diálogo VPN del sistema. Usa **Detener filtro DNS**, la notificación o Ajustes para apagarlo.
5. Puedes revocar Acceso de uso o VPN en cualquier momento desde Ajustes. Proyect X no conserva el historial ni los totales de uso. Desinstalar la app elimina su configuración local conforme a Android.

El prototipo DNS solo trata DNS UDP. Private DNS, DNS TCP, DoH/DoT, QUIC, acceso por IP, resolutores propios y VPN de terceros quedan fuera de cobertura. En AVD API 34 se comprobó que `DnsVpnService` bloquea dominios con `NXDOMAIN` local en `WebView Browser Tester` y en el resolvedor nativo cuando Private DNS está desactivado, y delega las consultas permitidas a `1.1.1.1:53` sin que Proyect X las registre. Faltan pruebas en varios navegadores comerciales y redes móviles/físicas antes de declararlo listo para producción.

Para validar la lógica sin Android:

```powershell
javac -encoding UTF-8 -d build\smoke app\src\main\java\com\proyectx\focus\Rule.java app\src\main\java\com\proyectx\focus\RuleEngine.java app\src\main\java\com\proyectx\focus\FocusSession.java tools\RuleEngineSmokeTest.java tools\FocusSessionSmokeTest.java
java -cp build\smoke com.proyectx.focus.RuleEngineSmokeTest
java -cp build\smoke com.proyectx.focus.FocusSessionSmokeTest
```

## Documentación

- [Investigación y libro mayor de evidencia](docs/INVESTIGACION.md)
- [Especificación de reconstrucción limpia](docs/RECONSTRUCCION_LIMPIA.md)
- [Guía de diseño original y límites de referencia](docs/DISENO_ORIGINAL.md)
- [ADR-001: límites y arquitectura inicial](docs/adr/ADR-001-reconstruccion-limpia.md)
- [ADR-003: no observación de navegadores](docs/adr/ADR-003-no-observacion-de-navegador.md)
- [ADR-004: VPN DNS local opcional](docs/adr/ADR-004-vpn-dns-local-opcional.md)
- [Ruta administrada Device Owner/Profile Owner](docs/DEVICE_ADMIN_MODE.md)
- [Laboratorio DNS controlado](docs/DNS_LAB.md)
- [Revisión de publicación y permisos](docs/PLAY_POLICY_REVIEW.md)
- [Revisión de dependencias y licencias](docs/DEPENDENCY_REVIEW.md)
- [Modelo de seguridad y límites](docs/SEGURIDAD.md)
- [Política de privacidad de prelanzamiento](docs/PRIVACIDAD.md)
- [Matriz de compatibilidad y cobertura](docs/COMPATIBILIDAD.md)
- [Proceso de seguridad](SECURITY.md)
- [Runbook de incidencias](docs/RUNBOOK.md)
- [Arquitectura actual](docs/architecture/ARCHITECTURE_0.md)
- [Plan de construcción por slices](docs/BUILD_PLAN.md)
- [Fases para una app completa](docs/FASES_PRODUCTO.md)
- [Firma de release, verificación, notas de versión y rollback](docs/RELEASE.md)
- [Investigación REA del APK de referencia](docs/INVESTIGACION_REA_BLOCKERX_5_0_61.md)
- [Mapa RAE de capacidades y layout propio](docs/RAE_LAYOUT_EVIDENCE.md)
- [Matriz RAE de capacidades y exclusiones](docs/RAE_CAPABILITY_MATRIX.md)
- [Procedencia y esquema de los datos RV1909](docs/RV1909_DATA.md)
- [Evidencia RAE y adaptación del módulo Refugio](docs/RAE_BIBLE_LAYOUT_EVIDENCE.md)

## Licencia y nombre

El código y la documentación propios de Proyect X se publican bajo [Apache-2.0](LICENSE). La investigación comparativa no implica afiliación con Atmana Tech.
