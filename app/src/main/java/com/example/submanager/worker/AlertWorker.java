package com.example.submanager.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertWorker extends Worker {

    public AlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationHelper.createNotificationChannel(context);

        AppDatabase db = AppDatabase.getInstance(context);
        List<SuscripcionModel> suscripciones = db.suscripcionDao().getAllSuscripcionesSync();

        if (suscripciones == null || suscripciones.isEmpty()) {
            return Result.success();
        }

        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        try {
            today = sdfDB.parse(sdfDB.format(today));
        } catch (Exception ignored) {}

        for (SuscripcionModel s : suscripciones) {
            if (!s.isEstaActiva() || s.getFechaProximoCobro() == null || s.getFechaProximoCobro().isEmpty()) {
                continue;
            }

            try {
                Date fechaCobro = sdfDB.parse(s.getFechaProximoCobro());
                if (fechaCobro == null) continue;

                long diffMillis = fechaCobro.getTime() - today.getTime();
                long diasFaltantes = diffMillis / (1000 * 60 * 60 * 24);

                int diasAnticipacion = s.getDiasAnticipacion() > 0 ? s.getDiasAnticipacion() : 3;

                if (diasFaltantes < 0) {
                    // Vencido
                    NotificationHelper.sendNotification(
                            context,
                            s.getId() + 1000,
                            "Pago vencido: " + s.getNombre(),
                            "El pago debió realizarse hace " + Math.abs(diasFaltantes) + " días."
                    );
                } else if (diasFaltantes <= diasAnticipacion) {
                    // Próximo a vencer
                    String mensaje = diasFaltantes == 0
                            ? "¡Atención! El cobro es hoy."
                            : "Faltan " + diasFaltantes + " días para el próximo cobro.";

                    NotificationHelper.sendNotification(
                            context,
                            s.getId() + 2000,
                            "Próximo pago: " + s.getNombre(),
                            mensaje
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Result.success();
    }
}
