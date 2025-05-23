package com.example.muzic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ProfilePicture implements Parcelable {
    public String _150x150;
    public String _480x480;
    public String _1000x1000;

    public ProfilePicture() {}

    protected ProfilePicture(Parcel in) {
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

    public static final Creator<ProfilePicture> CREATOR = new Creator<ProfilePicture>() {
        @Override
        public ProfilePicture createFromParcel(Parcel in) {
            return new ProfilePicture(in);
        }

        @Override
        public ProfilePicture[] newArray(int size) {
            return new ProfilePicture[size];
        }
    };
}

