package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la tabla `servicios_fisicos` de Supabase.
 */
public class ServicioFisicoDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("usuario_id")
    public Long usuarioId;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("monto_estimado")
    public Double montoEstimado;

    @SerializedName("monto_variable")
    public Boolean montoVariable;

    @SerializedName("ciclo_facturacion")
    public String cicloFacturacion;

    @SerializedName("fecha_proximo_cobro")
    public String fechaProximoCobro;

    @SerializedName("es_compartido")
    public Boolean esCompartido;

    @SerializedName("monto_total_recibo")
    public Double montoTotalRecibo;

    @SerializedName("monto_parte_usuario")
    public Double montoParteUsuario;

    @SerializedName("recordatorio_habilitado")
    public Boolean recordatorioHabilitado;

    @SerializedName("dias_anticipacion")
    public Integer diasAnticipacion;

    @SerializedName("notificacion_silenciada")
    public Boolean notificacionSilenciada;

    @SerializedName("ruta_imagen_comprobante")
    public String rutaImagenComprobante;

    @SerializedName("esta_activo")
    public Boolean estaActivo;

    @SerializedName("creado_en")
    public String creadoEn;

    @SerializedName("actualizado_en")
    public String actualizadoEn;

    public ServicioFisicoDto() {}
}
