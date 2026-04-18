package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la tabla `terceros_compartidos` de Supabase.
 */
public class TerceroCompartidoDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("servicio_id")
    public Long servicioId;

    @SerializedName("nombre_tercero")
    public String nombreTercero;

    @SerializedName("monto_aportacion")
    public Double montoAportacion;

    @SerializedName("creado_en")
    public String creadoEn;

    public TerceroCompartidoDto() {}
}
