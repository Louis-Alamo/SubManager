# Análisis del Problema de Sesiones y Sincronización en SubManager

## 1. El Problema Raíz
El error se debe a que el proceso de **cierre de sesión** actual (ubicado en `PerfilFragment.java`) únicamente borra los datos de `SharedPreferences` (las preferencias de sesión como `email`, `isPremium`, `remoteUserId`, etc. a través de `sessionManager.clearSession()`), pero **no elimina la base de datos local (Room)**.

Cuando el *Usuario A* cierra sesión, sus suscripciones, servicios físicos y registros de pago se quedan almacenados en el dispositivo (en `AppDatabase`).

Cuando el *Usuario B* inicia sesión en ese mismo dispositivo, la aplicación "hereda" instantáneamente toda la información local del Usuario A porque sigue existiendo en SQLite/Room.

## 2. Por qué cambian los IDs de Supabase y no sincroniza
El repositorio `RemoteSyncRepository` tiene una estrategia de sincronización que confía ciegamente en la base de datos local (Room) como fuente de verdad cuando hace un *Push* (Respaldar).

1. Al iniciar la sincronización `syncAll()`, el código lee todas las suscripciones de Room.
2. Al convertirlas a DTO (`toDto()`), asigna el ID del usuario remoto actual basándose en la sesión activa:
   ```java
   long userId = session.getRemoteUserId();
   if (userId != -1) {
       dto.usuarioId = userId;
   }
   ```
   Como la sesión activa ahora es del *Usuario B*, todas las suscripciones locales que eran del *Usuario A* se reasignan silenciosamente al `usuarioId` del *Usuario B*.
3. Posteriormente, hace un borrado remoto en Supabase (`api.deleteAllSuscripciones("gte.0")`) y sube todas estas suscripciones con el ID del *Usuario B*.

Esto provoca que las suscripciones del *Usuario A* sean robadas por el *Usuario B* en la base de datos en la nube. Entre diferentes celulares, si uno hace *Respaldar* y otro hace *Restaurar*, se terminan sobrescribiendo y mezclando los datos de las distintas cuentas.

## 3. Solución Propuesta

Para resolver esto de forma definitiva, se debe limpiar la base de datos local (excepto tal vez la cuenta de usuario para inicios de sesión offline) al momento de cerrar sesión.

### Corrección en `PerfilFragment.java`

Busca el botón de Logout en `setupListeners` y modifícalo para limpiar la base de datos local en un hilo secundario:

```java
View btnLogout = root.findViewById(R.id.btnLogout);
if (btnLogout != null) {
    btnLogout.setOnClickListener(v -> {
        // 1. Borrar preferencias de sesión
        sessionManager.clearSession();
        
        // 2. Borrar base de datos local en hilo secundario (Room no permite hacerlo en el principal)
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            
            // Borrado selectivo (Mantiene a los usuarios registrados localmente en UsuarioModel para login offline)
            db.suscripcionDao().deleteAllSuscripciones();
            db.suscripcionDao().deleteAllServiciosFisicos();
            db.suscripcionDao().deleteAllTerceros();
            db.suscripcionDao().deleteAllRegistrosPago();
            
            // Volver al hilo principal para actualizar la UI
            new Handler(Looper.getMainLooper()).post(() -> {
                updateAuthUI();
                showSnackbar(root, "Sesión cerrada de forma segura");
            });
        });
    });
}
```

### Consideración Adicional tras el Login
Con esta solución, cada vez que un usuario inicie sesión, entrará a un panel vacío (sin suscripciones locales). 
Si el usuario es **Premium**, deberás recordarle que presione el botón de **"Restaurar"** para traer su información desde la nube, o bien, invocar `remoteSyncRepository.pullAll(...)` automáticamente después del inicio de sesión exitoso si detectas que es Premium.
