package com.hoanvo.vongtaythongminh.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hoanvo.vongtaythongminh.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIManager {
    private static Retrofit mRetrofit;
    private static Gson gson = new GsonBuilder().create();
    public static API getAPI()
    {
        if(mRetrofit == null)
        {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(Utils.IP+"/")
                    .addConverterFactory(GsonConverterFactory.create(gson));
            mRetrofit = builder.build();
        }
        return mRetrofit.create(API.class);
    }
    public static Call<String> getUserLogin(Callback<String> callback, String nhietdo)
    {
        Call<String> call = getAPI().InsertNhietDo(nhietdo);
        call.enqueue(callback);
        return call;
    }
}
