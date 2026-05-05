package com.example.submanager.ui.viewmodel;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.repository.SuscripcionRepository;

import java.util.List;

public class SuscripcionViewModel extends AndroidViewModel {

    private final SuscripcionRepository repository;

    private final LiveData<List<SuscripcionModel>> todasLasSuscripciones;
    private final LiveData<List<SuscripcionModel>> suscripcionesActivasOrdenadas;
    private final LiveData<List<SuscripcionModel>> suscripcionesProximas;
    private final LiveData<Double> montoTotalActivas;

    public SuscripcionViewModel(@NonNull Application application) {
        super(application);
        repository = new SuscripcionRepository(application);

        todasLasSuscripciones = repository.getTodasLasSuscripciones();
        suscripcionesActivasOrdenadas = repository.getSuscripcionesActivasOrdenadas();
        suscripcionesProximas = repository.getSuscripcionesProximas();
        montoTotalActivas = repository.getMontoTotalActivas();
    }

    public LiveData<List<SuscripcionModel>> getTodasLasSuscripciones() {
        return todasLasSuscripciones;
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesActivasOrdenadas() {
        return suscripcionesActivasOrdenadas;
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesProximas() {
        return suscripcionesProximas;
    }

    public LiveData<Double> getMontoTotalActivas() {
        return montoTotalActivas;
    }

    public LiveData<SuscripcionModel> getSuscripcionById(int id) {
        return repository.getSuscripcionById(id);
    }

    public void insertar(SuscripcionModel suscripcion) {
        repository.insertar(suscripcion);
    }

    public void actualizar(SuscripcionModel suscripcion) {
        repository.actualizar(suscripcion);
    }

    public void eliminar(int id) {
        repository.eliminar(id);
    }

    public LiveData<List<com.example.submanager.data.model.RegistrosPagoModel>> getPagosBySuscripcion(int suscripcionId) {
        return repository.getPagosBySuscripcion(suscripcionId);
    }

    public void marcarComoPagado(SuscripcionModel suscripcion) {
        String fechaPago = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String timestampActual = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());

        com.example.submanager.data.model.RegistrosPagoModel pago = new com.example.submanager.data.model.RegistrosPagoModel(
                suscripcion.getId(),
                null,
                suscripcion.getNombre(),
                suscripcion.getColor(),
                suscripcion.getCategoria(),
                suscripcion.getMonto(),
                "Pagado",
                suscripcion.getFechaProximoCobro(),
                fechaPago,
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                timestampActual,
                timestampActual
        );

        repository.insertRegistroPago(pago);

        // Avanzar la fecha de próximo cobro
        String nuevaFecha = calcularProximaFecha(suscripcion.getFechaProximoCobro(), suscripcion.getCicloFacturacion());
        if (!nuevaFecha.isEmpty()) {
            suscripcion.setFechaProximoCobro(nuevaFecha);
            repository.actualizar(suscripcion);
        }
    }

    private String calcularProximaFecha(String fechaStr, String ciclo) {
        if (fechaStr == null || fechaStr.isEmpty() || ciclo == null) return "";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date firstDate = sdf.parse(fechaStr);
            if (firstDate == null) return "";

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(firstDate);

            switch (ciclo.toLowerCase()) {
                case "diario": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                case "semanal": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                case "quincenal": cal.add(java.util.Calendar.DAY_OF_YEAR, 15); break;
                case "mensual": cal.add(java.util.Calendar.MONTH, 1); break;
                case "bimestral": cal.add(java.util.Calendar.MONTH, 2); break;
                case "trimestral": cal.add(java.util.Calendar.MONTH, 3); break;
                case "semestral": cal.add(java.util.Calendar.MONTH, 6); break;
                case "anual": cal.add(java.util.Calendar.YEAR, 1); break;
                default: return "";
            }
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}