package com.example.muzic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public int album_count;
    public String artist_pick_track_id;
    public String bio;
    public CoverPhoto cover_photo;
    public int followee_count;
    public int follower_count;
    public boolean does_follow_current_user;
    public String handle;
    public String id;
    public boolean is_verified;
    public String location;
    public String name;
    public int playlist_count;
    public ProfilePicture profile_picture;
    public int repost_count;
    public int track_count;
    public boolean is_deactivated;
    public boolean is_available;
    public String erc_wallet;
    public String spl_wallet;
    public int supporter_count;
    public int supporting_count;
    public int total_audio_balance;

    public User() {}

    protected User(Parcel in) {
        album_count = in.readInt();
        artist_pick_track_id = in.readString();
        bio = in.readString();
        cover_photo = in.readParcelable(CoverPhoto.class.getClassLoader());
        followee_count = in.readInt();
        follower_count = in.readInt();
        does_follow_current_user = in.readByte() != 0;
        handle = in.readString();
        id = in.readString();
        is_verified = in.readByte() != 0;
        location = in.readString();
        name = in.readString();
        playlist_count = in.readInt();
        profile_picture = in.readParcelable(ProfilePicture.class.getClassLoader());
        repost_count = in.readInt();
        track_count = in.readInt();
        is_deactivated = in.readByte() != 0;
        is_available = in.readByte() != 0;
        erc_wallet = in.readString();
        spl_wallet = in.readString();
        supporter_count = in.readInt();
        supporting_count = in.readInt();
        total_audio_balance = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(album_count);
        dest.writeString(artist_pick_track_id);
        dest.writeString(bio);
        dest.writeParcelable(cover_photo, flags);
        dest.writeInt(followee_count);
        dest.writeInt(follower_count);
        dest.writeByte((byte) (does_follow_current_user ? 1 : 0));
        dest.writeString(handle);
        dest.writeString(id);
        dest.writeByte((byte) (is_verified ? 1 : 0));
        dest.writeString(location);
        dest.writeString(name);
        dest.writeInt(playlist_count);
        dest.writeParcelable(profile_picture, flags);
        dest.writeInt(repost_count);
        dest.writeInt(track_count);
        dest.writeByte((byte) (is_deactivated ? 1 : 0));
        dest.writeByte((byte) (is_available ? 1 : 0));
        dest.writeString(erc_wallet);
        dest.writeString(spl_wallet);
        dest.writeInt(supporter_count);
        dest.writeInt(supporting_count);
        dest.writeInt(total_audio_balance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
