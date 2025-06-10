package com.example.muzic.network;

import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.AudiusUserResponse;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.SearchResult;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRepository {
    private final AudiusApiService audiusApiService;

    public SearchRepository() {
        this.audiusApiService = AudiusApiClient.getInstance();
    }

    public interface SearchCallback {
        void onSuccess(SearchResult result);
        void onError(String message);
    }

    private void checkComplete(int[] completedCalls, SearchResult searchResult, SearchCallback callback) {
        completedCalls[0]++;
        if (completedCalls[0] == 3) {
            callback.onSuccess(searchResult);
        }
    }

    public void search(String query, SearchCallback callback) {
        SearchResult searchResult = new SearchResult();
        int[] completedCalls = {0};

        // Search Tracks
        audiusApiService.searchTracks(query).enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    searchResult.setTracks(response.body().data());
                }
                checkComplete(completedCalls, searchResult, callback);
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                checkComplete(completedCalls, searchResult, callback);
            }
        });

        // Search Playlists
        audiusApiService.searchPlaylists(query).enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    searchResult.setPlaylists(response.body().data());
                }
                checkComplete(completedCalls, searchResult, callback);
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                checkComplete(completedCalls, searchResult, callback);
            }
        });

        // Search Users
        audiusApiService.searchUsers(query).enqueue(new Callback<AudiusUserResponse>() {
            @Override
            public void onResponse(Call<AudiusUserResponse> call, Response<AudiusUserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    searchResult.setUsers(response.body().data());
                }
                checkComplete(completedCalls, searchResult, callback);
            }

            @Override
            public void onFailure(Call<AudiusUserResponse> call, Throwable t) {
                checkComplete(completedCalls, searchResult, callback);
            }
        });
    }

    public void searchTracks(String query, SearchCallback callback) {
        audiusApiService.searchTracks(query).enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    SearchResult result = new SearchResult();
                    result.setTracks(response.body().data());
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to search tracks");
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void searchPlaylists(String query, SearchCallback callback) {
        audiusApiService.searchPlaylists(query).enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    SearchResult result = new SearchResult();
                    result.setPlaylists(response.body().data());
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to search playlists");
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void searchUsers(String query, SearchCallback callback) {
        audiusApiService.searchUsers(query).enqueue(new Callback<AudiusUserResponse>() {
            @Override
            public void onResponse(Call<AudiusUserResponse> call, Response<AudiusUserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                    SearchResult result = new SearchResult();
                    result.setUsers(response.body().data());
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to search users");
                }
            }

            @Override
            public void onFailure(Call<AudiusUserResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
} 