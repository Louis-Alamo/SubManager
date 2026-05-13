# Plan de Pruebas - Información General del Proyecto "SubManager" (Actualizado)

## 1. Descripción General del Proyecto
**SubManager** es una aplicación móvil para Android desarrollada en Kotlin bajo una arquitectura **MVVM + Clean Architecture**. Su propósito es funcionar como un centro de control personal para gestionar gastos recurrentes (suscripciones digitales y servicios del hogar). 

La aplicación opera bajo un modelo **Freemium** e incluye soporte de base de datos local mediante **Room (SQLite)** y un sistema de sincronización en la nube mejorado mediante **Supabase (PostgreSQL)** exclusivo para usuarios Premium. Recientemente, se ha incorporado la estrategia de "Offline-first" con UPSERT en red para prevención segura de conflictos y recuperación individualizada.

---

## 2. Arquitectura de Datos y Campos (Cloud / Local)
La persistencia de datos y sincronización considera las siguientes tablas principales (todas enlazadas explícitamente mediante la foránea `usuario_id` para garantizar el aislamiento total de cuentas multiusuario):

### `usuarios` (Solo para cuentas Premium)
*   **Campos clave:** `id`, `nombre`, `correo` (único), `hash_contrasena`, `tipo_plan` (MENSUAL / ANUAL), `fecha_inicio_plan`, `fecha_renovacion`, `esta_activo`.

### `configuracion_app` (Configuración del dispositivo)
*   **Campos clave:** `id` (único=1), `notificaciones_habilitadas`, `hora_notificacion`, `minuto_notificacion`, `tono_notificacion`, `usuario_id` (FK), `ultima_sincronizacion`.

### `suscripciones` (Vida Digital)
*   **Campos clave:** `id`, `nombre`, `monto`, `ciclo_facturacion`, `color`, `categoria`, `metodo_pago`, `fecha_primer_cobro`, `fecha_proximo_cobro`, `fecha_limite_cancelacion`, `dias_anticipacion`, `esta_activa`, `usuario_id` (FK).

### `servicios_fisicos` (Servicios del Hogar)
*   **Campos clave:** `id`, `nombre`, `monto_estimado`, `monto_variable` (booleano), `ciclo_facturacion`, `fecha_proximo_cobro`, `es_compartido`, `monto_total_recibo`, `monto_parte_usuario`, `ruta_imagen_comprobante`, `usuario_id` (FK).

### `terceros_compartidos` (Gastos divididos)
*   **Campos clave:** `id`, `servicio_id` (FK), `nombre_tercero`, `monto_aportacion`, `usuario_id` (FK). *(Validación estricta: suma de aportaciones <= monto_total_recibo).*

### `registros_pago` (Historial)
*   **Campos clave:** `id`, `suscripcion_id` (FK), `monto`, `fecha_pago`, `estado` (PENDIENTE, PAGADO), `usuario_id` (FK).

---

## 3. Estructura de Pantallas (UI / Navigation)
La aplicación cuenta con diferentes Activities y Fragments principales:

1.  **AuthActivity**: Pantalla de registro e inicio de sesión.
2.  **MainActivity**: Contenedor principal de navegación (Bottom Navigation) que aloja:
    *   `DashboardFragment`: Métricas globales (dinero pendiente, pagado) y Próximos cobros.
    *   `SuscripcionesFragment`: Lista general interactiva y filtros por categoría.
    *   `HistorialFragment`: Gráficas y listado histórico de pagos (Conectado a BD real).
    *   `AlertasFragment`: Notificaciones y pagos vencidos (Conectado a BD real).
    *   `PerfilFragment`: Ajustes, configuración, y triggers directos de Sincronización Nube.
3.  **NuevaSuscripcionActivity**: Pantalla base para alta/edición de una suscripción digital.
4.  **DetalleSuscripcionActivity**: Visualización detallada, incluye Marcar como Pagado, Editar y Eliminar de BD.
5.  **NuevoServicioActivity**: Alta de servicios del hogar, botones de OCR y configuración de Gastos compartidos.
6.  **PremiumActivity / CompraExitosaActivity**: Entorno paywall y pasarela para la conversión a usuario Premium.

---

## 4. Estado de las Funcionalidades (Para priorización de QA)
El proyecto avanzó significativamente. Anteriormente presentaba bugs de inestabilidad y datos pre-cargados falsos que **ya han sido depurados**. Es indispensable adecuar las pruebas a la nueva realidad de la app:

### 🟢 Funcionalidades Operativas (Conectadas localmente y Nube)
*   **Autenticación y Limpieza de Sesión:** El Login se maneja directo contra la API y en Room. **(Corregido)** Al hacer Logout, ya se ejecuta un vaciado estricto (`clearAllTables`) previniendo fugas de datos entre cuentas locales.
*   **Suscripciones y Registros:** Añadir, Editar, Eliminar y Marcar Pago funcionan completamente vinculados a sus respectivos ViewModel y DAOs.
*   **Módulos de Historial y Alertas:** Anteriormente dependían de Mock Data pura. Ahora ya consultan a Room las métricas verdaderas y las grafican (PieChart).
*   **Sincronización Segura Cloud (Push y Pull):** **(Corregido)** El problema destructivo HTTP desapareció. La importación/exportación usa filtrado exacto (`eq.usuario_id`) e inserción "UPSERT", salvaguardando todos los repositorios remotos sin borrarlos.
*   **Correcciones UI:** **(Corregido)** Se solucionaron los "Mojibake" (íconos y textos rotos) y errores de renderizado en selecciones múltiples.

### 🟡 Funcionalidades "Espejismo" o Faltantes (MockUI)
*   **Guardado Total de Nuevo Servicio (`NuevoServicioActivity`):** El entorno estético fue perfeccionado, **PERO**, la acción de guardar sigue siendo de "simulación". Suelta un mensaje "Guardado" y cierra la App sin llamar a las funciones del DAO de Inserción.
*   **Escáner OCR:** Componente bloqueado (muestra "Próximamente").
*   **Compartir Gastos:** Bloqueado en "Próximamente".
*   **Alertas Nativas del Sistema Operativo:** Hay botón de activarlas en Perfil pero aún no se atan propiamente al `AlarmManager`.

---

## 5. Integración en la Nube / Entorno
*   **Backend:** Supabase (PostgreSQL para almacenamiento).
*   **Local Backend:** Room (SQLite).
*   **Módulos de sincronización en red implementados:** Las cinco entidades vitales (`Suscripciones`, `Servicios Físicos`, `Terceros Compartidos`, `Registros de Pago`, `Configuración`) conectan con Supabase blindadas contra choques.
*   **Permisos de Android:**
    *   `INTERNET`, `ACCESS_NETWORK_STATE` (Requerido para Supabase Sync).
    *   `POST_NOTIFICATIONS` (Reservado para recordatorios locales).
    *   `RECEIVE_BOOT_COMPLETED` (Reactivación de Alertas).
