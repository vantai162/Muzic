package com.example.muzic.records;

import com.example.muzic.model.User;

import java.util.List;

public record AudiusUserResponse(
        List<User> data
) {}