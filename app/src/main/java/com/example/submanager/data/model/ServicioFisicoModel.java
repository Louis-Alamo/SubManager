package com.example.submanager.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "servicios_fisicos")
public class ServicioFisicoModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String nombre;

    @NonNull
    @ColumnInfo(name = "monto_estimado")
    private double montoEstimado = 0.0;

    @NonNull
    @ColumnInfo(name = "monto_variable")
    private boolean montoVariable = false;

    @NonNull
    @ColumnInfo(name = "ciclo_facturacion")
    private String cicloFacturacion;

    @NonNull
    @ColumnInfo(name = "fecha_proximo_cobro")
    private String fechaProximoCobro;

    @NonNull
    @ColumnInfo(name = "es_compartido")
    private boolean esCompartido = false;

    @ColumnInfo(name = "monto_total_recibo")
    private Double montoTotalRecibo;

    @ColumnInfo(name = "monto_parte_usuario")
    private Double montoParteUsuario;

    @NonNull
    @ColumnInfo(name = "recordatorio_habilitado")
    private boolean recordatorioHabilitado = true;

    @NonNull
    @ColumnInfo(name = "dias_anticipacion")
    private int diasAnticipacion = 3;

    @NonNull
    @ColumnInfo(name = "notificacion_silenciada")
    private boolean notificacionSilenciada = false;

    @ColumnInfo(name = "ruta_imagen_comprobante")
    private String rutaImagenComprobante;

    @NonNull
    @ColumnInfo(name = "esta_activo")
    private boolean estaActivo = true;

    @NonNull
    @ColumnInfo(name = "creado_en")
    private String creadoEn;

    @NonNull
    @ColumnInfo(name = "actualizado_en")
    private String actualizadoEn;

    public ServicioFisicoModel() {
    }

    public ServicioFisicoModel(@NonNull String nombre, double montoEstimado, boolean montoVariable, @NonNull String cicloFacturacion, @NonNull String fechaProximoCobro, boolean esCompartido, Double montoTotalRecibo, Double montoParteUsuario, boolean recordatorioHabilitado, int diasAnticipacion, boolean notificacionSilenciada, String rutaImagenComprobante, boolean estaActivo, @NonNull String creadoEn, @NonNull String actualizadoEn) {
        this.nombre = nombre;
        this.montoEstimado = montoEstimado;
        this.montoVariable = montoVariable;
        this.cicloFacturacion = cicloFacturacion;
        this.fechaProximoCobro = fechaProximoCobro;
        this.esCompartido = esCompartido;
        this.montoTotalRecibo = montoTotalRecibo;
        this.montoParteUsuario = montoParteUsuario;
        this.recordatorioHabilitado = recordatorioHabilitado;
        this.diasAnticipacion = diasAnticipacion;
        this.notificacionSilenciada = notificacionSilenciada;
        this.rutaImagenComprobante = rutaImagenComprobante;
        this.estaActivo = estaActivo;
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

    public double getMontoEstimado() {
        return montoEstimado;
    }

    public void setMontoEstimado(double montoEstimado) {
        this.montoEstimado = montoEstimado;
    }

    public boolean isMontoVariable() {
        return montoVariable;
    }

    public void setMontoVariable(boolean montoVariable) {
        this.montoVariable = montoVariable;
    }

    @NonNull
    public String getCicloFacturacion() {
        return cicloFacturacion;
    }

    public void setCicloFacturacion(@NonNull String cicloFacturacion) {
        this.cicloFacturacion = cicloFacturacion;
    }

    @NonNull
    public String getFechaProximoCobro() {
        return fechaProximoCobro;
    }

    public void setFechaProximoCobro(@NonNull String fechaProximoCobro) {
        this.fechaProximoCobro = fechaProximoCobro;
    }

    public boolean isEsCompartido() {
        return esCompartido;
    }

    public void setEsCompartido(boolean esCompartido) {
        this.esCompartido = esCompartido;
    }

    public Double getMontoTotalRecibo() {
        return montoTotalRecibo;
    }

    public void setMontoTotalRecibo(Double montoTotalRecibo) {
        this.montoTotalRecibo = montoTotalRecibo;
    }

    public Double getMontoParteUsuario() {
        return montoParteUsuario;
    }

    public void setMontoParteUsuario(Double montoParteUsuario) {
        this.montoParteUsuario = montoParteUsuario;
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

    public String getRutaImagenComprobante() {
        return rutaImagenComprobante;
    }

    public void setRutaImagenComprobante(String rutaImagenComprobante) {
        this.rutaImagenComprobante = rutaImagenComprobante;
    }

    public boolean isEstaActivo() {
        return estaActivo;
    }

    public void setEstaActivo(boolean estaActivo) {
        this.estaActivo = estaActivo;
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