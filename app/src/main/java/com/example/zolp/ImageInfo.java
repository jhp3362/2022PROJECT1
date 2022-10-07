package com.example.zolp;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {
    private final String location;
    private final String name;
    private final String keyword;
    private float rating;

    public ImageInfo( String location, String name, String keyword, float rating) {
        this.location = location;
        this.name = name;
        this.keyword = keyword;
        this.rating = rating;
    }

    public ImageInfo(Parcel source) {
        this.location = source.readString();
        this.name = source.readString();
        this.keyword = source.readString();
        this.rating = source.readFloat();
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
        dest.writeString(keyword);
        dest.writeFloat(rating);
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

