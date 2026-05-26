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













public interface SupabaseApi {





    @GET("suscripciones")
    Call<List<SuscripcionDto>> getSuscripciones(@Query("usuario_id") String filter);

    @POST("suscripciones")
    @Headers("Prefer: return=minimal")
    Call<Void> insertSuscripcion(@Body SuscripcionDto dto);

    @POST("suscripciones?on_conflict=id")
    @Headers({
        "Prefer: return=minimal",
        "Prefer: resolution=merge-duplicates"
    })
    Call<Void> insertSuscripciones(@Body List<SuscripcionDto> dtos);

    @DELETE("suscripciones")
    Call<Void> deleteSuscripciones(@Query("esta_activa") String filter);

    @DELETE("suscripciones")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllSuscripciones(@Query("usuario_id") String filter);





    @GET("servicios_fisicos")
    Call<List<ServicioFisicoDto>> getServiciosFisicos(@Query("usuario_id") String filter);

    @POST("servicios_fisicos?on_conflict=id")
    @Headers({
        "Prefer: return=minimal",
        "Prefer: resolution=merge-duplicates"
    })
    Call<Void> insertServiciosFisicos(@Body List<ServicioFisicoDto> dtos);

    @DELETE("servicios_fisicos")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllServiciosFisicos(@Query("usuario_id") String filter);





    @GET("terceros_compartidos")
    Call<List<TerceroCompartidoDto>> getTercerosCompartidos(@Query("usuario_id") String filter);

    @POST("terceros_compartidos?on_conflict=id")
    @Headers({
        "Prefer: return=minimal",
        "Prefer: resolution=merge-duplicates"
    })
    Call<Void> insertTercerosCompartidos(@Body List<TerceroCompartidoDto> dtos);

    @DELETE("terceros_compartidos")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllTerceros(@Query("usuario_id") String filter);





    @GET("registros_pago")
    Call<List<RegistroPagoDto>> getRegistrosPago(@Query("usuario_id") String filter);

    @POST("registros_pago?on_conflict=id")
    @Headers({
        "Prefer: return=minimal",
        "Prefer: resolution=merge-duplicates"
    })
    Call<Void> insertRegistrosPago(@Body List<RegistroPagoDto> dtos);

    @DELETE("registros_pago")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteAllRegistrosPago(@Query("usuario_id") String filter);





    @GET("configuracion_app")
    Call<List<ConfiguracionAppDto>> getConfiguracion();

    @POST("configuracion_app")
    @Headers("Prefer: return=minimal")
    Call<Void> insertConfiguracion(@Body ConfiguracionAppDto dto);

    @PATCH("configuracion_app")
    @Headers("Prefer: return=minimal")
    Call<Void> updateConfiguracion(@Query("usuario_id") String filter, @Body ConfiguracionAppDto dto);





    @GET("usuarios")
    Call<List<UsuarioDto>> getUsuarioPorCorreo(@Query("correo") String correoFilter);

    @POST("usuarios")
    @Headers("Prefer: return=representation")
    Call<List<UsuarioDto>> createUsuario(@Body UsuarioDto dto);

    @PATCH("usuarios")
    @Headers("Prefer: return=minimal")
    Call<Void> updateUsuario(@Query("correo") String correoFilter, @Body UsuarioDto dto);
}
