package com.example.muzic.network;

import com.example.muzic.model.AudiusTrackResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AudiusRepository {
    private AudiusApiService apiService;

    public AudiusRepository() {
        this.apiService = AudiusApiClient.getInstance();
    }

    public void getTrendingTracks(int limit, Callback<AudiusTrackResponse> callback) {
        Call<AudiusTrackResponse> call = apiService.getTrendingTracks(limit);
        call.enqueue(callback);
    }

    public void streamTrack(String id, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = apiService.streamTrack(id);
        call.enqueue(callback);
    }
}

