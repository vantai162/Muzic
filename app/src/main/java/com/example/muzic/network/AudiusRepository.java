package com.example.muzic.network;

import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.PlaylistResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudiusRepository {
    private AudiusApiService apiService;

    public AudiusRepository() {
        this.apiService = AudiusApiClient.getInstance();
    }

    public void getTrendingTracks(int limit, Callback<AudiusTrackResponse> callback) {
        Call<AudiusTrackResponse> call = apiService.getTrendingTracks(limit);
        call.enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Error: " + response.code() + " - " + response.message());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                System.out.println("Network Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void streamTrack(String id, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = apiService.streamTrack(id);
        call.enqueue(callback);
    }
    public void getTracksByMood(String mood, Consumer<List<Track>> onSuccess, Consumer<Throwable> onError) {
        AudiusApiService apiService = AudiusApiClient.getInstance();
        apiService.getTrendingTracks(70).enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> allTracks = response.body().data();
                    List<Track> filteredTracks = new ArrayList<>();
                    for (Track track : allTracks) {
                        if (track.mood() != null && track.mood().equalsIgnoreCase(mood)) {
                            filteredTracks.add(track);
                        }
                    }
                    onSuccess.accept(filteredTracks);
                } else {
                    onError.accept(new Exception("API không trả về dữ liệu hợp lệ"));
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                onError.accept(t);
            }
        });
    }

    public void getTracksByGenre(String gen, Consumer<List<Track>> onSuccess, Consumer<Throwable> onError) {
        AudiusApiService apiService = AudiusApiClient.getInstance();
        apiService.getTrendingTracks(50).enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> allTracks = response.body().data();
                    List<Track> filteredTracks = new ArrayList<>();
                    for (Track track : allTracks) {
                        if (track.genre() != null && track.genre().equalsIgnoreCase(gen)) {
                            filteredTracks.add(track);
                        }
                    }
                    onSuccess.accept(filteredTracks);
                } else {
                    onError.accept(new Exception("API không trả về dữ liệu hợp lệ"));
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                onError.accept(t);
            }
        });
    }

    public void getTrendingPlaylists(int limit, Callback<PlaylistResponse> callback) {
        Call<PlaylistResponse> call = apiService.getTrendingPlaylists(limit);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Error: " + response.code() + " - " + response.message());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                System.out.println("Network Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }
}

