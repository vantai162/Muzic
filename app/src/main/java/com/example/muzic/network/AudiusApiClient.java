package com.example.muzic.network;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudiusApiClient {

    private static final String BASE_URL = "https://discoveryprovider3.audius.co/";
    private static final String APP_NAME = "MUZIC"; // ← Đặt tên app của bạn ở đây
    private static AudiusApiService apiService;

    // Timeout constants
    private static final int CONNECT_TIMEOUT = 150;  // seconds
    private static final int READ_TIMEOUT = 150;     // seconds
    private static final int WRITE_TIMEOUT = 150;    // seconds
    private static final int MAX_RETRIES = 3;

    public static AudiusApiService getInstance() {
        if (apiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        
                        // Add app_name parameter
                        HttpUrl originalUrl = original.url();
                        HttpUrl newUrl = originalUrl.newBuilder()
                                .addQueryParameter("app_name", APP_NAME)
                                .build();
                        
                        // Build new request
                        Request newRequest = original.newBuilder()
                                .url(newUrl)
                                .build();

                        Response response = null;
                        IOException lastException = null;
                        
                        for (int retry = 0; retry < MAX_RETRIES; retry++) {
                            try {
                                if (response != null) {
                                    response.close();
                                }
                                response = chain.proceed(newRequest);
                                if (response.isSuccessful()) {
                                    return response;
                                }
                                response.close();
                            } catch (IOException e) {
                                lastException = e;
                                if (response != null) {
                                    response.close();
                                }
                                if (retry == MAX_RETRIES - 1) {
                                    throw lastException;
                                }
                                try {
                                    Thread.sleep((1 << retry) * 1000L);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw lastException;
                                }
                            }
                        }
                        
                        throw new IOException("Request failed after " + MAX_RETRIES + " retries");
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
