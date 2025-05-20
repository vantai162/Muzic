package com.example.muzic.network;


import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class AudiusApiClient {

    private static final String BASE_URL = "https://discoveryprovider2.audius.co/";
    private static final String APP_NAME = "MUZIC"; // ← Đặt tên app của bạn ở đây
    private static AudiusApiService apiService;

    public static AudiusApiService getInstance() {
        if (apiService == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            HttpUrl originalUrl = chain.request().url();
                            HttpUrl newUrl = originalUrl.newBuilder()
                                    .addQueryParameter("app_name", APP_NAME)
                                    .build();
                            Request newRequest = chain.request().newBuilder()
                                    .url(newUrl)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(AudiusApiService.class);
        }
        return apiService;
    }
}
