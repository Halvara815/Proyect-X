# Remodelado visual basado en el ZIP autorizado

## Fuente y alcance

- Artefacto inspeccionado estáticamente: `docs/architecture/proyect-x-—-blovix-focus-&-blocker.zip`
- SHA-256: `dc8de375f8564a30852d9e75fee0e76752817e070817f7bb66dbecac092ec924`
- Inventario REA: `ev_3a6798a81e67ad015cfbaf69259d51584a39f8997c4bf566bb45bc5de80d5f88`
- Extracción segura REA: `ev_14918308aa5e3e8314bbaa4bf54ad11a97788e7fe624e9f5fd3c00c81448f219`

La evidencia anterior es estática: enumera y permite estudiar los archivos del ZIP, pero no
demuestra ejecución de la aplicación de referencia.

## Fuentes de layout estudiadas

| Archivo del ZIP | Aporte visual reutilizado | Fuera de alcance |
| --- | --- | --- |
| `src/App.tsx` | Cascarón con cabecera persistente y destinos inferiores | Estados, datos y textos de ejemplo |
| `src/components/BloqueoScreen.tsx` | Tarjetas compactas, superficies y CTA jerarquizados | Lógica y contenido de la referencia |
| `src/components/ListaBloqueadaScreen.tsx` | Agrupación de controles y estado visible de una regla | Reglas de muestra |
| `src/components/ForoYMenuScreen.tsx` | Proporción y jerarquía del panel lateral local | Cualquier texto, enlaces o contenido remoto |
| `src/index.css` | Contraste entre cabecera oscura, fondo frío y superficies claras | CSS, tipografías y tokens literales |

## Implementación propia

`MainActivity` ahora usa una cabecera oscura, fondo frío, tarjetas con borde sutil, CTA azules,
navegación inferior persistente y un panel lateral de centro local. Todos los títulos, detalles,
contadores, reglas, permisos, acciones y datos continúan provenientes de Proyect X y de sus
almacenes locales existentes. No se importaron imágenes, corpus, datos estáticos ni código del ZIP.

La validación posterior se realizó con `:app:lintDebug`, `:app:assembleDebug` y capturas del AVD
para Inicio, Barreras y el centro local.
