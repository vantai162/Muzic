package com.example.muzic.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.muzic.model.AudiusTrackResponse;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.records.Track;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AudiusAPI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudiusApiService apiService = AudiusApiClient.getInstance();
        Call<AudiusTrackResponse> call = apiService.getTrendingTracks(10);

        call.enqueue(new retrofit2.Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().data();
                    for (Track track : tracks) {
                        Log.d(TAG, "Track: " + track.title() + " - By: " + track.user().name());
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
            }
        });
    }
}