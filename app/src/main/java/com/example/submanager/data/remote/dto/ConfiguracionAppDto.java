package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la tabla `configuracion_app` de Supabase.
 * Es un registro Singleton (id siempre = 1).
 */
public class ConfiguracionAppDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("notificaciones_habilitadas")
    public Boolean notificacionesHabilitadas;

    @SerializedName("hora_notificacion")
    public Integer horaNotificacion;

    @SerializedName("minuto_notificacion")
    public Integer minutoNotificacion;

    @SerializedName("tono_notificacion")
    public String tonoNotificacion;

    @SerializedName("usuario_id")
    public Long usuarioId;

    @SerializedName("ultima_sincronizacion")
    public String ultimaSincronizacion;

    public ConfiguracionAppDto() {}
}
