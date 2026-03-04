package com.example.submanager.data.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "terceros_compartidos",
        foreignKeys = @ForeignKey(
                entity = ServicioFisicoModel.class,
                parentColumns = "id",
                childColumns = "servicio_id",
                onDelete = ForeignKey.CASCADE
        ))
public class TercerosCompartidosModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "servicio_id", index = true)
    private int servicioId;

    @NonNull
    @ColumnInfo(name = "nombre_tercero")
    private String nombreTercero;

    @NonNull
    @ColumnInfo(name = "monto_aportacion")
    private double montoAportacion;

    @NonNull
    @ColumnInfo(name = "creado_en")
    private String creadoEn;

    public TercerosCompartidosModel() {}

    @Ignore
    public TercerosCompartidosModel(int servicioId, @NonNull String nombreTercero, double montoAportacion, @NonNull String creadoEn) {
        this.servicioId = servicioId;
        this.nombreTercero = nombreTercero;
        this.montoAportacion = montoAportacion;
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
    public String getNombreTercero() {
        return nombreTercero;
    }

    public void setNombreTercero(@NonNull String nombreTercero) {
        this.nombreTercero = nombreTercero;
    }

    public double getMontoAportacion() {
        return montoAportacion;
    }

    public void setMontoAportacion(double montoAportacion) {
        this.montoAportacion = montoAportacion;
    }

    @NonNull
    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(@NonNull String creadoEn) {
        this.creadoEn = creadoEn;
    }
}