package com.example.muzic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Artwork implements Parcelable {
    public String _150x150;
    public String _480x480;
    public String _1000x1000;

    public Artwork() {}

    protected Artwork(Parcel in) {
        _150x150 = in.readString();
        _480x480 = in.readString();
        _1000x1000 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_150x150);
        dest.writeString(_480x480);
        dest.writeString(_1000x1000);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Artwork> CREATOR = new Creator<Artwork>() {
        @Override
        public Artwork createFromParcel(Parcel in) {
            return new Artwork(in);
        }

        @Override
        public Artwork[] newArray(int size) {
            return new Artwork[size];
        }
    };
}