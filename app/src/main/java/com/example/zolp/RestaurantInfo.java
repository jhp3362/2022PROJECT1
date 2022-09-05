package com.example.zolp;

public class RestaurantInfo {
    public String name, imageUrl, location, phoneNumber, webUrl;
    public String[] keywords;
    int id;

    public RestaurantInfo(int id, String name, String imageUrl, String location, String phoneNumber, String webUrl, String[] keywords) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.webUrl = webUrl;
        this.keywords = keywords;
    }
}
