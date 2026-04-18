package com.example.submanager.data.remote.api;

import com.example.submanager.data.remote.dto.ConfiguracionAppDto;
import com.example.submanager.data.remote.dto.RegistroPagoDto;
import com.example.submanager.data.remote.dto.ServicioFisicoDto;
import com.example.submanager.data.remote.dto.SuscripcionDto;
import com.example.submanager.data.remote.dto.TerceroCompartidoDto;
import com.example.submanager.data.remote.dto.UsuarioDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interfaz Retrofit para la API REST de Supabase (PostgREST).
 *
 * Convenciones de Supabase PostgREST:
 *   - GET    /tabla             → SELECT *
 *   - POST   /tabla             → INSERT
 *   - PATCH  /tabla?id=eq.{n}  → UPDATE WHERE id = n
 *   - DELETE /tabla?id=eq.{n}  → DELETE WHERE id = n
 *
 * Los filtros siguen el patrón: campo=operador.valor
 *   Ej: id=eq.5, correo=eq.usuario@email.com
 */
public interface SupabaseApi {

    // ─────────────────────────────────────────────────────────────────────────
    // SUSCRIPCIONES
    // ─────────────────────────────────────────────────────────────────────────

    @GET("suscripciones")
    Call<List<SuscripcionDto>> getSuscripciones();

    @POST("suscripciones")
    @Headers("Prefer: return=minimal")
    Call<Void> insertSuscripcion(@Body SuscripcionDto dto);

    @POST("suscripciones")
    @Headers("Prefer: return=minimal")
    Call<Void> insertSuscripciones(@Body List<SuscripcionDto> dtos);

    @DELETE("suscripciones")
    Call<Void> deleteSuscripciones(@Query("esta_activa") String filter);

    @DELETE("suscripciones")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllSuscripciones(@Query("id") String filter);

    // ─────────────────────────────────────────────────────────────────────────
    // SERVICIOS FÍSICOS
    // ─────────────────────────────────────────────────────────────────────────

    @GET("servicios_fisicos")
    Call<List<ServicioFisicoDto>> getServiciosFisicos();

    @POST("servicios_fisicos")
    @Headers("Prefer: return=minimal")
    Call<Void> insertServiciosFisicos(@Body List<ServicioFisicoDto> dtos);

    @DELETE("servicios_fisicos")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllServiciosFisicos(@Query("id") String filter);

    // ─────────────────────────────────────────────────────────────────────────
    // TERCEROS COMPARTIDOS
    // ─────────────────────────────────────────────────────────────────────────

    @GET("terceros_compartidos")
    Call<List<TerceroCompartidoDto>> getTercerosCompartidos();

    @POST("terceros_compartidos")
    @Headers("Prefer: return=minimal")
    Call<Void> insertTercerosCompartidos(@Body List<TerceroCompartidoDto> dtos);

    @DELETE("terceros_compartidos")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllTerceros(@Query("id") String filter);

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTROS DE PAGO
    // ─────────────────────────────────────────────────────────────────────────

    @GET("registros_pago")
    Call<List<RegistroPagoDto>> getRegistrosPago();

    @POST("registros_pago")
    @Headers("Prefer: return=minimal")
    Call<Void> insertRegistrosPago(@Body List<RegistroPagoDto> dtos);

    @DELETE("registros_pago")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllRegistrosPago(@Query("id") String filter);

    // ─────────────────────────────────────────────────────────────────────────
    // CONFIGURACIÓN APP
    // ─────────────────────────────────────────────────────────────────────────

    @GET("configuracion_app")
    Call<List<ConfiguracionAppDto>> getConfiguracion();

    @POST("configuracion_app")
    @Headers("Prefer: return=minimal")
    Call<Void> insertConfiguracion(@Body ConfiguracionAppDto dto);

    @PATCH("configuracion_app")
    @Headers("Prefer: return=minimal")
    Call<Void> updateConfiguracion(@Query("id") String filter, @Body ConfiguracionAppDto dto);

    // ─────────────────────────────────────────────────────────────────────────
    // USUARIOS (solo Premium)
    // ─────────────────────────────────────────────────────────────────────────

    @GET("usuarios")
    Call<List<UsuarioDto>> getUsuarioPorCorreo(@Query("correo") String correoFilter);

    @POST("usuarios")
    @Headers("Prefer: return=representation")
    Call<List<UsuarioDto>> createUsuario(@Body UsuarioDto dto);

    @PATCH("usuarios")
    @Headers("Prefer: return=minimal")
    Call<Void> updateUsuario(@Query("correo") String correoFilter, @Body UsuarioDto dto);
}
