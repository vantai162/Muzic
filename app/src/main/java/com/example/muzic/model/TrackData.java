package com.example.muzic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackData implements Parcelable {
    public Artwork artwork;
    public String description;
    public String genre;
    public String id;
    public String track_cid;
    public String mood;
    public String release_date;
    public RemixOf remix_of;
    public int repost_count;
    public int favorite_count;
    public String tags;
    public String title;
    public User user;
    public int duration;
    public boolean downloadable;
    public int play_count;
    public String permalink;
    public boolean is_streamable;

    public TrackData() {}

    protected TrackData(Parcel in) {
        artwork = in.readParcelable(Artwork.class.getClassLoader());
        description = in.readString();
        genre = in.readString();
        id = in.readString();
        track_cid = in.readString();
        mood = in.readString();
        release_date = in.readString();
        remix_of = in.readParcelable(RemixOf.class.getClassLoader());
        repost_count = in.readInt();
        favorite_count = in.readInt();
        tags = in.readString();
        title = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        duration = in.readInt();
        downloadable = in.readByte() != 0;
        play_count = in.readInt();
        permalink = in.readString();
        is_streamable = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(artwork, flags);
        dest.writeString(description);
        dest.writeString(genre);
        dest.writeString(id);
        dest.writeString(track_cid);
        dest.writeString(mood);
        dest.writeString(release_date);
        dest.writeParcelable((Parcelable) remix_of, flags);
        dest.writeInt(repost_count);
        dest.writeInt(favorite_count);
        dest.writeString(tags);
        dest.writeString(title);
        dest.writeParcelable(user, flags);
        dest.writeInt(duration);
        dest.writeByte((byte) (downloadable ? 1 : 0));
        dest.writeInt(play_count);
        dest.writeString(permalink);
        dest.writeByte((byte) (is_streamable ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackData> CREATOR = new Creator<TrackData>() {
        @Override
        public TrackData createFromParcel(Parcel in) {
            return new TrackData(in);
        }

        @Override
        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };
}
