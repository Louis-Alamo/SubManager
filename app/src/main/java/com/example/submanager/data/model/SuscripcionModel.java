package com.example.submanager.data.model;

import androidx.room.Entity;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "suscripciones")
public class SuscripcionModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String nombre;

    @NonNull
    private double monto;

    @NonNull
    @ColumnInfo(name = "ciclo_facturacion")
    private String cicloFacturacion;

    @NonNull
    private String color;

    @NonNull
    private String categoria;

    @NonNull
    @ColumnInfo(name = "metodo_pago")
    private String metodoPago;

    @NonNull
    @ColumnInfo(name = "fecha_primer_cobro")
    private String fechaPrimerCobro;

    @NonNull
    @ColumnInfo(name = "fecha_proximo_cobro")
    private String fechaProximoCobro;

    @ColumnInfo(name = "fecha_limite_cancelacion")
    private String fechaLimiteCancelacion;

    @NonNull
    @ColumnInfo(name = "recordatorio_habilitado")
    private boolean recordatorioHabilitado = true;

    @NonNull
    @ColumnInfo(name = "dias_anticipacion")
    private int diasAnticipacion = 3;

    @NonNull
    @ColumnInfo(name = "notificacion_silenciada")
    private boolean notificacionSilenciada = false;

    @NonNull
    @ColumnInfo(name = "esta_activa")
    private boolean estaActiva = true;

    @NonNull
    @ColumnInfo(name = "nombre_icono")
    private String nombreIcono;

    @NonNull
    @ColumnInfo(name = "creado_en")
    private String creadoEn;

    @NonNull
    @ColumnInfo(name = "actualizado_en")
    private String actualizadoEn;

    public SuscripcionModel() {
    }

    public SuscripcionModel(@NonNull String nombre, double monto, @NonNull String cicloFacturacion, @NonNull String color, @NonNull String categoria, @NonNull String metodoPago, @NonNull String fechaPrimerCobro, @NonNull String fechaProximoCobro, String fechaLimiteCancelacion, boolean recordatorioHabilitado, int diasAnticipacion, boolean notificacionSilenciada, boolean estaActiva, String nombreIcono, @NonNull String creadoEn, @NonNull String actualizadoEn) {
        this.nombre = nombre;
        this.monto = monto;
        this.cicloFacturacion = cicloFacturacion;
        this.color = color;
        this.categoria = categoria;
        this.metodoPago = metodoPago;
        this.fechaPrimerCobro = fechaPrimerCobro;
        this.fechaProximoCobro = fechaProximoCobro;
        this.fechaLimiteCancelacion = fechaLimiteCancelacion;
        this.recordatorioHabilitado = recordatorioHabilitado;
        this.diasAnticipacion = diasAnticipacion;
        this.notificacionSilenciada = notificacionSilenciada;
        this.estaActiva = estaActiva;
        this.nombreIcono = nombreIcono;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getNombre() {
        return nombre;
    }

    public void setNombre(@NonNull String nombre) {
        this.nombre = nombre;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @NonNull
    public String getCicloFacturacion() {
        return cicloFacturacion;
    }

    public void setCicloFacturacion(@NonNull String cicloFacturacion) {
        this.cicloFacturacion = cicloFacturacion;
    }

    @NonNull
    public String getColor() {
        return color;
    }

    public void setColor(@NonNull String color) {
        this.color = color;
    }

    @NonNull
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(@NonNull String categoria) {
        this.categoria = categoria;
    }

    @NonNull
    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(@NonNull String metodoPago) {
        this.metodoPago = metodoPago;
    }

    @NonNull
    public String getFechaPrimerCobro() {
        return fechaPrimerCobro;
    }

    public void setFechaPrimerCobro(@NonNull String fechaPrimerCobro) {
        this.fechaPrimerCobro = fechaPrimerCobro;
    }

    @NonNull
    public String getFechaProximoCobro() {
        return fechaProximoCobro;
    }

    public void setFechaProximoCobro(@NonNull String fechaProximoCobro) {
        this.fechaProximoCobro = fechaProximoCobro;
    }

    public String getFechaLimiteCancelacion() {
        return fechaLimiteCancelacion;
    }

    public void setFechaLimiteCancelacion(String fechaLimiteCancelacion) {
        this.fechaLimiteCancelacion = fechaLimiteCancelacion;
    }

    public boolean isRecordatorioHabilitado() {
        return recordatorioHabilitado;
    }

    public void setRecordatorioHabilitado(boolean recordatorioHabilitado) {
        this.recordatorioHabilitado = recordatorioHabilitado;
    }

    public int getDiasAnticipacion() {
        return diasAnticipacion;
    }

    public void setDiasAnticipacion(int diasAnticipacion) {
        this.diasAnticipacion = diasAnticipacion;
    }

    public boolean isNotificacionSilenciada() {
        return notificacionSilenciada;
    }

    public void setNotificacionSilenciada(boolean notificacionSilenciada) {
        this.notificacionSilenciada = notificacionSilenciada;
    }

    public boolean isEstaActiva() {
        return estaActiva;
    }

    public void setEstaActiva(boolean estaActiva) {
        this.estaActiva = estaActiva;
    }

    public String getNombreIcono() {
        return nombreIcono;
    }

    public void setNombreIcono(String nombreIcono) {
        this.nombreIcono = nombreIcono;
    }

    @NonNull
    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(@NonNull String creadoEn) {
        this.creadoEn = creadoEn;
    }

    @NonNull
    public String getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(@NonNull String actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
