package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;





public class SuscripcionDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("usuario_id")
    public Long usuarioId;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("monto")
    public Double monto;

    @SerializedName("ciclo_facturacion")
    public String cicloFacturacion;

    @SerializedName("color")
    public String color;

    @SerializedName("categoria")
    public String categoria;

    @SerializedName("metodo_pago")
    public String metodoPago;

    @SerializedName("fecha_primer_cobro")
    public String fechaPrimerCobro;

    @SerializedName("fecha_proximo_cobro")
    public String fechaProximoCobro;

    @SerializedName("fecha_limite_cancelacion")
    public String fechaLimiteCancelacion;

    @SerializedName("recordatorio_habilitado")
    public Boolean recordatorioHabilitado;

    @SerializedName("dias_anticipacion")
    public Integer diasAnticipacion;

    @SerializedName("notificacion_silenciada")
    public Boolean notificacionSilenciada;

    @SerializedName("esta_activa")
    public Boolean estaActiva;


    @SerializedName("icono_nombre")
    public String iconoNombre;

    @SerializedName("creado_en")
    public String creadoEn;

    @SerializedName("actualizado_en")
    public String actualizadoEn;

    public SuscripcionDto() {}
}
