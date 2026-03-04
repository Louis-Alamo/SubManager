package com.example.submanager.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "escaneos_ocr",
        foreignKeys = @ForeignKey(entity = ServicioFisicoModel.class, parentColumns = "id", childColumns = "servicio_id", onDelete = ForeignKey.CASCADE))
public class EscaneosOcrModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "servicio_id", index = true)
    private int servicioId;

    @NonNull
    @ColumnInfo(name = "ruta_imagen")
    private String rutaImagen;

    @ColumnInfo(name = "monto_detectado")
    private Double montoDetectado;

    @ColumnInfo(name = "fecha_detectada")
    private String fechaDetectada;

    @ColumnInfo(name = "nivel_confianza")
    private String nivelConfianza;

    @NonNull
    private boolean confirmado = false;

    @NonNull
    @ColumnInfo(name = "creado_en")
    private String creadoEn;

    public EscaneosOcrModel() {}

    @Ignore
    public EscaneosOcrModel(int servicioId, @NonNull String rutaImagen, Double montoDetectado, String fechaDetectada, String nivelConfianza, boolean confirmado, @NonNull String creadoEn) {
        this.servicioId = servicioId;
        this.rutaImagen = rutaImagen;
        this.montoDetectado = montoDetectado;
        this.fechaDetectada = fechaDetectada;
        this.nivelConfianza = nivelConfianza;
        this.confirmado = confirmado;
        this.creadoEn = creadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServicioId() {
        return servicioId;
    }

    public void setServicioId(int servicioId) {
        this.servicioId = servicioId;
    }

    @NonNull
    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(@NonNull String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public Double getMontoDetectado() {
        return montoDetectado;
    }

    public void setMontoDetectado(Double montoDetectado) {
        this.montoDetectado = montoDetectado;
    }

    public String getFechaDetectada() {
        return fechaDetectada;
    }

    public void setFechaDetectada(String fechaDetectada) {
        this.fechaDetectada = fechaDetectada;
    }

    public String getNivelConfianza() {
        return nivelConfianza;
    }

    public void setNivelConfianza(String nivelConfianza) {
        this.nivelConfianza = nivelConfianza;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    @NonNull
    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(@NonNull String creadoEn) {
        this.creadoEn = creadoEn;
    }
}