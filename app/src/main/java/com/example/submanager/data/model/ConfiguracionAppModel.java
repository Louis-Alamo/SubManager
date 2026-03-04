package com.example.submanager.data.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "configuracion_app")
public class ConfiguracionAppModel {

    @PrimaryKey
    private int id = 1;

    @NonNull
    @ColumnInfo(name = "notificaciones_habilitadas")
    private boolean notificacionesHabilitadas = true;

    @NonNull
    @ColumnInfo(name = "hora_notificacion")
    private int horaNotificacion = 9;

    @NonNull
    @ColumnInfo(name = "minuto_notificacion")
    private int minutoNotificacion = 0;

    @NonNull
    @ColumnInfo(name = "tono_notificacion")
    private String tonoNotificacion = "predeterminado";

    @ColumnInfo(name = "usuario_id")
    private Integer usuarioId;

    @ColumnInfo(name = "ultima_sincronizacion")
    private String ultimaSincronizacion;

    public ConfiguracionAppModel() {}

    @Ignore
    public ConfiguracionAppModel(int id, boolean notificacionesHabilitadas, int horaNotificacion, int minutoNotificacion, @NonNull String tonoNotificacion, Integer usuarioId, String ultimaSincronizacion) {
        this.id = id;
        this.notificacionesHabilitadas = notificacionesHabilitadas;
        this.horaNotificacion = horaNotificacion;
        this.minutoNotificacion = minutoNotificacion;
        this.tonoNotificacion = tonoNotificacion;
        this.usuarioId = usuarioId;
        this.ultimaSincronizacion = ultimaSincronizacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNotificacionesHabilitadas() {
        return notificacionesHabilitadas;
    }

    public void setNotificacionesHabilitadas(boolean notificacionesHabilitadas) {
        this.notificacionesHabilitadas = notificacionesHabilitadas;
    }

    public int getHoraNotificacion() {
        return horaNotificacion;
    }

    public void setHoraNotificacion(int horaNotificacion) {
        this.horaNotificacion = horaNotificacion;
    }

    public int getMinutoNotificacion() {
        return minutoNotificacion;
    }

    public void setMinutoNotificacion(int minutoNotificacion) {
        this.minutoNotificacion = minutoNotificacion;
    }

    @NonNull
    public String getTonoNotificacion() {
        return tonoNotificacion;
    }

    public void setTonoNotificacion(@NonNull String tonoNotificacion) {
        this.tonoNotificacion = tonoNotificacion;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUltimaSincronizacion() {
        return ultimaSincronizacion;
    }

    public void setUltimaSincronizacion(String ultimaSincronizacion) {
        this.ultimaSincronizacion = ultimaSincronizacion;
    }
}