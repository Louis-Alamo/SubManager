package com.example.submanager.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlertWorker extends Worker {

    public AlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lógica de validación de notificaciones / alertas
        return Result.success();
    }
}

