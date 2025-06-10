package com.example.muzic.network;

import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.AudiusUserResponse;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.User;
import com.example.muzic.records.UserResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AudiusApiService {

    @GET("/v1/users/{handle}")
    Call<AudiusUserResponse> getUserByHandle(@Path("handle") String handle);

    @GET("/v1/users/{id}")
    Call<UserResponse> getUserById(@Path("id") String id);

    @GET("/v1/tracks/trending")
    Call<AudiusTrackResponse> getTrendingTracks(@Query("limit") int limit);

    @GET("/v1/tracks/{id}")
    Call<AudiusTrackResponse> getTrack(@Path("id") String trackId);

    @GET("/v1/tracks/stream/{id}")
    Call<ResponseBody> streamTrack(@Path("id") String trackId);

    @GET("v1/playlists/trending")
    Call<PlaylistResponse> getTrendingPlaylists(
            @Query("limit") int limit
    );

    @GET("v1/playlists/{playlist_id}")
    Call<PlaylistResponse> getPlaylist(@Path("playlist_id") String playlistId);

    @GET("v1/playlists/{playlist_id}/tracks")
    Call<AudiusTrackResponse> getPlaylistTracks(@Path("playlist_id") String playlistId);

    @GET("/v1/users/{id}/tracks")
    Call<AudiusTrackResponse> getUserTracks(@Path("id") String id);

    // Search endpoints
    @GET("/v1/tracks/search")
    Call<AudiusTrackResponse> searchTracks(@Query("query") String query);

    @GET("/v1/playlists/search")
    Call<PlaylistResponse> searchPlaylists(@Query("query") String query);

    @GET("/v1/users/search")
    Call<AudiusUserResponse> searchUsers(@Query("query") String query);
}