package com.example.submanager.data.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "registros_pago",
        foreignKeys = {
                @ForeignKey(entity = SuscripcionModel.class, parentColumns = "id", childColumns = "suscripcion_id", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = ServicioFisicoModel.class, parentColumns = "id", childColumns = "servicio_id", onDelete = ForeignKey.SET_NULL)
        })
public class RegistrosPagoModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "suscripcion_id", index = true)
    private Integer suscripcionId;

    @ColumnInfo(name = "servicio_id", index = true)
    private Integer servicioId;

    @NonNull
    @ColumnInfo(name = "nombre_origen")
    private String nombreOrigen;

    @NonNull
    @ColumnInfo(name = "color_origen")
    private String colorOrigen;

    @NonNull
    private String categoria;

    @NonNull
    private double monto;

    @NonNull
    private String estado;

    @NonNull
    @ColumnInfo(name = "fecha_vencimiento")
    private String fechaVencimiento;

    @ColumnInfo(name = "fecha_pago")
    private String fechaPago;

    @ColumnInfo(name = "mes_facturacion")
    private int mesFacturacion;

    @ColumnInfo(name = "anio_facturacion")
    private int anioFacturacion;

    @NonNull
    @ColumnInfo(name = "creado_en")
    private String creadoEn;

    @NonNull
    @ColumnInfo(name = "actualizado_en")
    private String actualizadoEn;

    public RegistrosPagoModel() {}

    @Ignore
    public RegistrosPagoModel(Integer suscripcionId, Integer servicioId, @NonNull String nombreOrigen, @NonNull String colorOrigen, @NonNull String categoria, double monto, @NonNull String estado, @NonNull String fechaVencimiento, String fechaPago, int mesFacturacion, int anioFacturacion, @NonNull String creadoEn, @NonNull String actualizadoEn) {
        this.suscripcionId = suscripcionId;
        this.servicioId = servicioId;
        this.nombreOrigen = nombreOrigen;
        this.colorOrigen = colorOrigen;
        this.categoria = categoria;
        this.monto = monto;
        this.estado = estado;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.mesFacturacion = mesFacturacion;
        this.anioFacturacion = anioFacturacion;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSuscripcionId() {
        return suscripcionId;
    }

    public void setSuscripcionId(Integer suscripcionId) {
        this.suscripcionId = suscripcionId;
    }

    public Integer getServicioId() {
        return servicioId;
    }

    public void setServicioId(Integer servicioId) {
        this.servicioId = servicioId;
    }

    @NonNull
    public String getNombreOrigen() {
        return nombreOrigen;
    }

    public void setNombreOrigen(@NonNull String nombreOrigen) {
        this.nombreOrigen = nombreOrigen;
    }

    @NonNull
    public String getColorOrigen() {
        return colorOrigen;
    }

    public void setColorOrigen(@NonNull String colorOrigen) {
        this.colorOrigen = colorOrigen;
    }

    @NonNull
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(@NonNull String categoria) {
        this.categoria = categoria;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @NonNull
    public String getEstado() {
        return estado;
    }

    public void setEstado(@NonNull String estado) {
        this.estado = estado;
    }

    @NonNull
    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(@NonNull String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public int getMesFacturacion() {
        return mesFacturacion;
    }

    public void setMesFacturacion(int mesFacturacion) {
        this.mesFacturacion = mesFacturacion;
    }

    public int getAnioFacturacion() {
        return anioFacturacion;
    }

    public void setAnioFacturacion(int anioFacturacion) {
        this.anioFacturacion = anioFacturacion;
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