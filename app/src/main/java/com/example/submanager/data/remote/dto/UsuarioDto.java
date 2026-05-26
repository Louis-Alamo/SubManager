package com.example.submanager.data.remote.dto;

import com.google.gson.annotations.SerializedName;





public class UsuarioDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("correo")
    public String correo;

    @SerializedName("hash_contrasena")
    public String hashContrasena;

    @SerializedName("tipo_plan")
    public String tipoPlan;

    @SerializedName("fecha_inicio_plan")
    public String fechaInicioPlan;

    @SerializedName("fecha_renovacion")
    public String fechaRenovacion;

    @SerializedName("esta_activo")
    public Boolean estaActivo;

    @SerializedName("creado_en")
    public String creadoEn;

    @SerializedName("actualizado_en")
    public String actualizadoEn;

    public UsuarioDto() {}
}
