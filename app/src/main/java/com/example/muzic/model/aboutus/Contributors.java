package com.example.muzic.model.aboutus;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Contributors(
        @SerializedName("projectName") String projectName,
        @SerializedName("projectOwner") String projectOwner,
        @SerializedName("files") List<String> files,
        @SerializedName("imageSize") int imageSize,
        @SerializedName("contributors") List<Contributor> contributors,
        @SerializedName("repoType") String repoType,
        @SerializedName("contributorsPerLine") int contributorsPerLine,
        @SerializedName("repoHost") String repoHost,
        @SerializedName("commitConvention") String commitConvention,
        @SerializedName("skipCi") boolean skipCi,
        @SerializedName("commitType") String commitType
) {
    public record Contributor(
            @SerializedName("login") String login,
            @SerializedName("name") String name,
            @SerializedName("avatar_url") String avatar_url,
            @SerializedName("profile") String profile,
            @SerializedName("contributions") List<String> contributions
    ) {
    }
}
