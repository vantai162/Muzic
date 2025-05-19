package com.example.muzic.model;


import com.example.muzic.records.Track;

import java.util.List;

public record AudiusTrackResponse(
        List<Track> data
) {}