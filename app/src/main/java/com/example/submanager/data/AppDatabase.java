package com.example.submanager.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.submanager.data.dao.SuscripcionDao;
import com.example.submanager.data.model.ConfiguracionAppModel;
import com.example.submanager.data.model.EscaneosOcrModel;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.ServicioFisicoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.model.TercerosCompartidosModel;

@Database(entities = {
        SuscripcionModel.class,
        ServicioFisicoModel.class,
        TercerosCompartidosModel.class,
        RegistrosPagoModel.class,
        EscaneosOcrModel.class,
        ConfiguracionAppModel.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SuscripcionDao suscripcionDao();

    private static volatile AppDatabase INSTANCE;

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Insertamos datos iniciales ("semilla" o "seed") como dummies.

            String sqlDelete = "DELETE FROM suscripciones;";
            db.execSQL(sqlDelete);

            String sqlInsert = "INSERT INTO suscripciones (\n" +
                    "    nombre, monto, ciclo_facturacion, color, categoria, metodo_pago, \n" +
                    "    fecha_primer_cobro, fecha_proximo_cobro, fecha_limite_cancelacion, \n" +
                    "    recordatorio_habilitado, dias_anticipacion, notificacion_silenciada, \n" +
                    "    esta_activa, nombre_icono, creado_en, actualizado_en\n" +
                    ") VALUES \n" +
                    "('Netflix Premium', 199.00, 'Mensual', '#E50914', 'Entretenimiento', 'Tarjeta de crédito', '2025-10-15', '2026-03-15', '2026-03-12', 1, 3, 0, 1, 'ic_app_netflix', '2025-10-15T10:00:00', '2026-02-15T10:00:00'),\n" +
                    "('Spotify Duo', 129.00, 'Mensual', '#1DB954', 'Música', 'Tarjeta de débito', '2025-10-20', '2026-03-20', '2026-03-17', 1, 3, 0, 1, 'ic_app_spotify', '2025-10-20T10:00:00', '2026-02-20T10:00:00'),\n" +
                    "('GitHub Copilot', 200.00, 'Mensual', '#24292F', 'Software', 'Tarjeta de crédito', '2025-10-15', '2026-03-15', '2026-03-12', 1, 3, 0, 1, 'ic_app_copilot', '2025-10-15T10:00:00', '2026-02-15T10:00:00'),\n" +
                    "('Amazon Prime', 99.00, 'Mensual', '#00A8E0', 'Entretenimiento', 'Tarjeta de crédito', '2025-11-02', '2026-04-02', '2026-03-30', 1, 3, 0, 1, 'ic_app_prime_video', '2025-11-02T10:00:00', '2026-02-02T10:00:00');";
            db.execSQL(sqlInsert);
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
