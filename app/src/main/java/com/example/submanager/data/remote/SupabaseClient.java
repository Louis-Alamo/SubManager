package com.example.submanager.data.remote;

import com.example.submanager.BuildConfig;
import com.example.submanager.data.remote.api.SupabaseApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;










public class SupabaseClient {

    private static volatile SupabaseApi INSTANCE;

    private SupabaseClient() {}

    public static SupabaseApi getApi() {
        if (INSTANCE == null) {
            synchronized (SupabaseClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildRetrofit().create(SupabaseApi.class);
                }
            }
        }
        return INSTANCE;
    }

    private static Retrofit buildRetrofit() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", BuildConfig.SUPABASE_KEY)
                            .header("Authorization", "Bearer " + BuildConfig.SUPABASE_KEY)
                            .header("Content-Type", "application/json")



                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                });


        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addInterceptor(logging);
        }

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
                .client(httpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
