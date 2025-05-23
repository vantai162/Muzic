package com.example.muzic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CoverPhoto implements Parcelable {
    public String _640x;
    public String _2000x;

    public CoverPhoto() {}

    protected CoverPhoto(Parcel in) {
        _640x = in.readString();
        _2000x = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_640x);
        dest.writeString(_2000x);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CoverPhoto> CREATOR = new Creator<CoverPhoto>() {
        @Override
        public CoverPhoto createFromParcel(Parcel in) {
            return new CoverPhoto(in);
        }

        @Override
        public CoverPhoto[] newArray(int size) {
            return new CoverPhoto[size];
        }
    };
}
