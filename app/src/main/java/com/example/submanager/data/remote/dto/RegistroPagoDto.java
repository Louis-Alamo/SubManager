package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la tabla `registros_pago` de Supabase.
 */
public class RegistroPagoDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("suscripcion_id")
    public Long suscripcionId;

    @SerializedName("servicio_id")
    public Long servicioId;

    @SerializedName("nombre_origen")
    public String nombreOrigen;

    @SerializedName("color_origen")
    public String colorOrigen;

    @SerializedName("categoria")
    public String categoria;

    @SerializedName("monto")
    public Double monto;

    @SerializedName("estado")
    public String estado;

    @SerializedName("fecha_vencimiento")
    public String fechaVencimiento;

    @SerializedName("fecha_pago")
    public String fechaPago;

    @SerializedName("mes_facturacion")
    public Integer mesFacturacion;

    @SerializedName("anio_facturacion")
    public Integer anioFacturacion;

    @SerializedName("creado_en")
    public String creadoEn;

    @SerializedName("actualizado_en")
    public String actualizadoEn;

    public RegistroPagoDto() {}
}
