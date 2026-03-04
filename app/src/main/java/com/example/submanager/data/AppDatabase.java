package com.example.submanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {


    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "submanager_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}