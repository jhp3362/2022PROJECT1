package com.example.zolp;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {
    private final String location;
    private final String name;

    public ImageInfo( String location, String name) {
        this.location = location;
        this.name = name;
    }

    public ImageInfo(Parcel source) {
        this.location = source.readString();
        this.name = source.readString();
    }

    public static final Parcelable.Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel source) {
            return new ImageInfo(source);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(location);
        dest.writeString(name);
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

}

