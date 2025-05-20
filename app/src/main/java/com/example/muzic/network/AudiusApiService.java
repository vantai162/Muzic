package com.example.muzic.network;

import com.example.muzic.model.AudiusTrackResponse;
import com.example.muzic.model.AudiusUserResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AudiusApiService {

    @GET("/v1/users/{handle}")
    Call<AudiusUserResponse> getUserByHandle(@Path("handle") String handle);

    @GET("/v1/tracks/trending")
    Call<AudiusTrackResponse> getTrendingTracks(@Query("limit") int limit);
    @GET("/v1/tracks/stream/{id}")
    Call<ResponseBody> streamTrack(@Path("id") String trackId);
}