package com.l3mdev.AppCruzada.interfaces;

import com.l3mdev.AppCruzada.DB.LecturaModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("ApiLecturas/api/v1/VerRutas/{token}")
    Call<ResponseBody> getRutas(@Path("token") String authToken);

    @GET("api/v1/Beneficiarios/ObtenBeneficiario")
    Call<ResponseBody> getBeneficiario(
            @Header("Authorization") String authToken,
            @Query("curp") String curp
    );

    @POST("ApiLecturas/api/v1/GuardarRutas/{token}")
    Call<ResponseBody> sendLectura(@Path("token") String authToken, @Body LecturaModel lectura);
}

