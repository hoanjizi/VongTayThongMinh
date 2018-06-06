package com.hoanvo.vongtaythongminh.API;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface API {
    @POST("insertnhietdo")
    @FormUrlEncoded
    Call<String> InsertNhietDo(@Field("nhietdo") String nhietdo);
}
