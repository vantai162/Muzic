package com.example.muzic.api;

import com.example.muzic.object.Song;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("songs") // Nếu backend là http://10.0.2.2:3000/api/songs
    Call<List<Song>> getAllSongs();

    @GET("songs/search")
    Call<List<Song>> searchSongs(@Query("query") String keyword);
}
