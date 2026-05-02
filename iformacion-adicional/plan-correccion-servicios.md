# Plan de Corrección para la Pantalla de Nuevo Servicio

## 1. Problema de los Iconos "Raros" (Mojibake y Emojis)
**Análisis:**
Al inspeccionar el archivo `activity_nuevo_servicio.xml`, se detectó que los botones de selección (chips) correspondientes a **Luz**, **Gas** y **Teléfono** se estructuraron utilizando caracteres de texto (emojis) insertados directamente en etiquetas `<TextView>` (por ejemplo, `text="ðŸ’¡"` para Luz). 
Esto genera dos problemas:
1. **Errores de codificación (Mojibake):** Al guardar el archivo en distintos formatos o leerlos desde ciertos sistemas, los emojis colapsan en caracteres irreconocibles ("ðŸ’¡").
2. **Inconsistencia de diseño:** Los emojis son renderizados por el sistema operativo, lo que significa que se verán con estilos totalmente distintos si abres la app en un Samsung, un Xiaomi o un Pixel, rompiendo la estética limpia de la aplicación.
En contraste, los servicios de **Agua** e **Internet** se ven perfectamente porque utilizan `<ImageView>` referenciando iconos vectoriales (ej. `@drawable/ic_service_agua`).

**Solución a implementar:**
* Reemplazar las etiquetas `<TextView>` que actúan como iconos por etiquetas `<ImageView>`.
* Asignarles imágenes vectoriales (`.xml` o `.svg`) guardadas en la carpeta `res/drawable` de tu proyecto (por ejemplo, creando `@drawable/ic_service_luz`, `@drawable/ic_service_gas`, `@drawable/ic_service_telefono`).

## 2. Problema de Selección de Servicios (Efecto visual estancado)
**Análisis:**
Mencionaste que al seleccionar otro servicio "no funciona" o no se ve bien. Al revisar `NuevoServicioActivity.java`, la función encargada de esto es `selectChip()`. 
Actualmente, esta función está incompleta:
1. **No actualiza el interior:** Cuando seleccionas un chip, el código actualiza el color de fondo de la tarjeta (el cuadrito exterior), pero ignora los elementos hijos. El texto y el icono conservan su color original permanentemente. (Ej. El texto de "Luz" siempre será azul fuerte, y el de "Agua" siempre será gris, sin importar cuál esté activo).
2. **Uso de colores quemados:** En la clase de Java se declararon variables con colores en formato hexadecimal duro (ej. `0xFFEFF6FF`). Esto puede causar que la pantalla se vea mal si en el futuro se implementa un Modo Oscuro en el teléfono.

**Solución a implementar:**
* Modificar la función `selectChip()` para que no solo cambie el color de fondo, sino que también extraiga el `TextView` y el `ImageView` correspondientes al contenedor seleccionado.
* **Estado Activo (Seleccionado):** 
  - Fondo del contenedor: `@color/primary_tint`
  - Color del `ImageView` (`imageTintList`): `@color/primary`
  - Color del `TextView` (`setTextColor`): `@color/primary`
* **Estado Inactivo (Deseleccionado):**
  - Fondo del contenedor: `@color/background`
  - Color del `ImageView` (`imageTintList`): `@color/text_secondary`
  - Color del `TextView` (`setTextColor`): `@color/text_secondary`
* Utilizar `ContextCompat.getColor(this, R.color.nombre_color)` y `ContextCompat.getColorStateList()` para llamar a los colores de forma segura y soportar temas dinámicos.