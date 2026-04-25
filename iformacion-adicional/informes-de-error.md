# Problemas Críticos en la Gestión de Sesiones y Sincronización

Tras un análisis a fondo del código base, se han identificado múltiples fallos críticos que causan que las sesiones se mezclen, los IDs de suscripciones cambien y haya pérdida de datos entre usuarios.

## 1. El Logout no limpia la base de datos local (Room)
En `PerfilFragment.java`, el botón de cerrar sesión llama a `sessionManager.clearSession()`, lo cual solo limpia las SharedPreferences. La base de datos local de Room (`AppDatabase`) **no se vacía**. 
- **Consecuencia:** Cuando el Usuario B inicia sesión en el mismo dispositivo, hereda automáticamente todas las suscripciones, servicios físicos y registros que dejó el Usuario A en el teléfono. 

## 2. SyncAll y PullAll no filtran por usuario (Borrado global)
En `RemoteSyncRepository.java`, el método `syncAll` elimina todas las suscripciones de Supabase antes de subir las locales usando `api.deleteAllSuscripciones("gte.0")`. 
- **Consecuencia:** Esto significa que **¡borra las suscripciones de TODOS los usuarios de la aplicación!** y luego sube las que están en el teléfono. Si el Usuario B hereda la base de datos local del Usuario A y sincroniza, re-subirá las suscripciones bajo su propio `remoteUserId`, "robando" los datos de otras personas.
- Por su parte, `pullAll` usa `getSuscripciones()` sin ningún filtro, por lo que se descarga las suscripciones de **todos los usuarios registrados** en la plataforma.

## 3. Ausencia de Filtros en la API de Retrofit
En `SupabaseApi.java`, las consultas HTTP no incluyen el ID del usuario actual:
- `@GET("suscripciones")` -> Debería ser algo como `@GET("suscripciones") Call<List<SuscripcionDto>> getSuscripciones(@Query("usuario_id") String userIdFilter);`
- `@DELETE("suscripciones")` -> Debería filtrar `usuario_id=eq.{id}` en lugar de `id=gte.0`.

## 4. Problema de Seguridad en Supabase
La app usa la llave `anon` en `SupabaseClient.java` para hacer peticiones a PostgREST sin un token de autenticación JWT propio de Supabase, porque usa un esquema manual de usuarios. 
- Al no tener contexto de "Logueado" del lado de Supabase, el backend no puede proteger los registros con políticas RLS (Row Level Security). Cualquiera puede manipular la base global entera.

## Solución Recomendada:
1. **Vaciar Room al hacer Logout:** Añadir una limpieza de la BD local (ej: ejecutar `AppDatabase.getInstance(requireContext()).clearAllTables();` en un hilo de fondo) justo al ejecutar `sessionManager.clearSession()`.
2. **Filtrar peticiones HTTP:** Modificar `SupabaseApi.java` para que tanto los `GET` como los `DELETE` fuercen y envíen el parámetro `?usuario_id=eq.X` perteneciente al ID actual (obtenido de `SessionManager.getRemoteUserId()`).
3. **Refactorizar lógica de Backup:** No recurrir a la estrategia "borrar todo y resubir".
