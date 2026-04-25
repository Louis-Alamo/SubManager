# Reporte de Análisis de Funcionalidades - SubManager

Tras analizar a fondo la estructura de código, Activities, Fragments, base de datos local (Room) y las conexiones remotas (Supabase), a continuación presento el estado actual de todas las funcionalidades de la aplicación.

Se han categorizado en tres secciones principales:

---

## 🟢 1. Funcionalidades Completas y Conectadas a la Base de Datos
*Estas funciones operan con información real, interactúan con la base de datos local (Room) y/o con la nube (Supabase).*

*   **Autenticación y Cuentas de Usuario (`AuthActivity`, `PerfilFragment`)**: 
    Conectado. Valida logins locales, consulta Supabase, compara hashes criptográficos y guarda la sesión correctamente. (Incluye las mejoras recientes de limpieza segura de base de datos en los cierres de sesión).
*   **Dashboard Principal (`DashboardFragment`)**: 
    Conectado. Calcula el dinero pendiente, pagado y total sumando directamente las entradas reales guardadas en Room. La lista de próximos cobros es dinámica.
*   **Gestión de Suscripciones (`SuscripcionesFragment`)**: 
    Conectado. La lista principal y el filtro por categorías (chips) reaccionan en tiempo real usando LiveData y reflejan los datos reales de Room.
*   **Crear Nueva Suscripción (`NuevaSuscripcionActivity`)**: 
    Conectado. El formulario construye el `SuscripcionModel` y lo inserta en la base de datos local a través del ViewModel.
*   **Sincronización de Datos / Respaldos (`RemoteSyncRepository`)**: 
    Conectado. La aplicación es capaz de exportar e importar `Suscripciones`, `Servicios Físicos` y `Registros de Pago` a la nube si el usuario cuenta con membresía Premium.
*   **Gestión de Suscripción Premium (`PremiumActivity`)**: 
    Conectado. Modifica localmente la sesión e impacta directamente la tabla de usuarios en Supabase para registrar la membresía activa.

---

## 🟡 2. Funcionalidades "Espejismo" (Tienen UI pero les falta conexión a la DB)
*Estas funciones te permiten llenar formularios o hacer clics, parecen funcionar y muestran mensajes de éxito, pero internamente **NO guardan ni modifican nada** en la base de datos.*

*   **Agregar un Nuevo Servicio de Hogar (`NuevoServicioActivity`)**: 
    La pantalla está lista, puedes llenar el recibo de luz o gas, pero el botón "Guardar Servicio" **solo arroja un mensaje de éxito ("Servicio guardado") y se cierra**. No crea ningún objeto ni interactúa con `ServicioFisicoModel`.
*   **Eliminar Suscripción (`DetalleSuscripcionActivity`)**: 
    Muestra una alerta preguntando "¿Estás seguro?", y al aceptar tira el mensaje "Suscripción eliminada". Sin embargo, **la suscripción sigue existiendo en SQLite**, no ejecuta el comando `delete`.
*   **Marcar Suscripción como Pagada (`DetalleSuscripcionActivity`)**: 
    El botón de check (Marcar pagado) muestra un mensaje de éxito pero no crea ningún registro en `RegistrosPagoModel`.
*   **Recordatorios y Notificaciones (`PerfilFragment`)**: 
    El botón o switch de "Alertas" cambia visualmente pero no está enlazado a ningún servicio de AlarmManager, WorkManager, ni altera configuración de notificaciones reales en el sistema operativo Android.

---

## 🔴 3. Funcionalidades Incompletas (Solo Dummies y Mock Data)
*Estas secciones usan información 100% falsa (hardcodeada) en el código. Sirven solo como maquetas de diseño.*

*   **Pantalla de Historial y Gráficas (`HistorialFragment`)**: 
    **Totalmente falsa.** La gráfica de dona siempre mostrará "65%, 25%, 10%" y "Marzo 2026". Toda la lista inferior está creada desde código con objetos `PagoMock` (Netflix, Spotify, Luz Eléctrica, etc.). Las flechas de cambiar de mes no hacen nada.
*   **Pantalla de Alertas de Pago (`AlertasFragment`)**: 
    **Totalmente falsa.** La lista de pendientes, pagados y vencidos se inyecta manualmente (con una clase llamada `PagoAlerta`). Los botones de pagar dentro de estas alertas solo arrojan *Snackbars*.
*   **Historial dentro del Detalle de Suscripción (`DetalleSuscripcionActivity`)**: 
    Los "últimos pagos" que ves al fondo (ej. *11 Mar 2026 - Pagado*) son 3 líneas fijas y estáticas.
*   **Gastos Compartidos (`NuevoServicioActivity`)**: 
    Al activar el switch de compartir, aparece un recuadro. Sin embargo, el botón "Agregar Persona" te avisa con un "Próximamente".
*   **Escáner OCR de Recibos (`NuevoServicioActivity`)**: 
    El botón con la cámara lanza un "Escáner próximamente".
*   **Edición de Suscripción (`DetalleSuscripcionActivity`)**: 
    El botón del lápiz (`btnEdit`) no abre nada, solo indica "Función próximamente".
*   **Exportar Reportes CSV/PDF (`HistorialFragment`)**: 
    El menú superior (tres puntitos) tiene las opciones, pero seleccionarlas solo dispara un mensaje genérico.
*   **Selector de Hora de Recordatorios (`PerfilFragment`)**: 
    Pulsar la fila de la hora indica "Selector de hora próximamente".

---

**Conclusión:**
SubManager tiene un esqueleto muy sólido y el módulo de Suscripciones principales + Login/Sincronización ya está operativo. El siguiente paso lógico para su evolución debería ser atacar los "Espejismos" (Sección 2), concretamente el Guardado de Servicios y el Borrado de Suscripciones, para después empezar a reemplazar la información falsa de Historial y Alertas (Sección 3) por consultas reales a Room.
