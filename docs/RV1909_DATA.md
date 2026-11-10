# Datos bíblicos RV1909

`app/src/main/assets/rv1909.json` es un corpus local de lectura para el módulo opcional **Modo Fe / Lectura Bíblica**. La app no consulta una API ni transmite búsquedas, capítulos o progreso de lectura.

## Procedencia y licencia

- Fuente: [EvanBible, descarga JSON RV1909](https://www.evanbible.com/descargar.php?f=json&v=rv1909).
- Versión: `RV1909` / Reina-Valera 1909.
- Licencia declarada por la fuente: dominio público.
- SHA-256 del archivo incluido: `cce57f4e88fa13d24dfdb6207e07f46f29048d185f9db014f9e089cc38e3d86e`.
- Fecha de incorporación: 2026-11-09.

La edición elegida es la Reina-Valera 1909. No se incluyen textos de la Reina-Valera 1960 ni de otras ediciones con derechos de editor.

## Esquema admitido

El parser propio consume el formato distribuido:

```text
meta → libros[] → { id, nombre, capitulos[] } → capítulo[] → [número_de_versículo, texto]
```

El lector verifica que haya al menos un libro antes de presentar contenido. Ante una lectura inválida, muestra un error local y no hace descarga ni reemplaza silenciosamente el corpus.
