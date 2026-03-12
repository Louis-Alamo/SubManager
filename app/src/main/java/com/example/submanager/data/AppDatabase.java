package com.example.submanager.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.submanager.data.dao.SuscripcionDao;
import com.example.submanager.data.dao.UsuarioDao;
import com.example.submanager.data.model.ConfiguracionAppModel;
import com.example.submanager.data.model.EscaneosOcrModel;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.ServicioFisicoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.model.TercerosCompartidosModel;
import com.example.submanager.data.model.UsuarioModel;

import java.util.concurrent.Executors;

@Database(entities = {
        SuscripcionModel.class,
        ServicioFisicoModel.class,
        TercerosCompartidosModel.class,
        RegistrosPagoModel.class,
        EscaneosOcrModel.class,
        ConfiguracionAppModel.class,
        UsuarioModel.class
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SuscripcionDao suscripcionDao();
    public abstract UsuarioDao usuarioDao();

    private static volatile AppDatabase INSTANCE;

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Usamos un Executor para que la inserción no bloquee el hilo principal
            Executors.newSingleThreadExecutor().execute(() -> {
                // NOTA: Aquí usamos SQL puro porque en onCreate el DAO aún no está listo
                db.execSQL("DELETE FROM suscripciones;");

                String sqlInsert = "INSERT INTO suscripciones (" +
                        "nombre, monto, ciclo_facturacion, color, categoria, metodo_pago, " +
                        "fecha_primer_cobro, fecha_proximo_cobro, fecha_limite_cancelacion, " +
                        "recordatorio_habilitado, dias_anticipacion, notificacion_silenciada, " +
                        "esta_activa, nombre_icono, creado_en, actualizado_en" +
                        ") VALUES " +
                        "('Netflix Premium', 199.00, 'MENSUAL', '#E50914', 'ENTRETENIMIENTO', 'TARJETA', '2025-10-15', '2026-03-15', '2026-03-12', 1, 3, 0, 1, 'ic_app_netflix', '2025-10-15T10:00:00', '2026-02-15T10:00:00')," +
                        "('Spotify Duo', 129.00, 'MENSUAL', '#1DB954', 'MUSICA', 'TARJETA', '2025-10-20', '2026-03-20', '2026-03-17', 1, 3, 0, 1, 'ic_app_spotify', '2025-10-20T10:00:00', '2026-02-20T10:00:00')," +
                        "('GitHub Copilot', 200.00, 'MENSUAL', '#24292F', 'SOFTWARE', 'TARJETA', '2025-10-15', '2026-03-15', '2026-03-12', 1, 3, 0, 1, 'ic_app_copilot', '2025-10-15T10:00:00', '2026-02-15T10:00:00')," +
                        "('Amazon Prime', 99.00, 'MENSUAL', '#00A8E0', 'ENTRETENIMIENTO', 'TARJETA', '2025-11-02', '2026-04-02', '2026-03-30', 1, 3, 0, 1, 'ic_app_prime_video', '2025-11-02T10:00:00', '2026-02-02T10:00:00');";
                db.execSQL(sqlInsert);
            });
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "submanager_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}