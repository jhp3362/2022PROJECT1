package com.example.zolp;

public class RestaurantInfo {
    public String id, name, imageUrl, location, phoneNumber, webUrl;
    public String[] keywords;
    public Boolean isFavorites;

    public RestaurantInfo(String id, String name, String imageUrl, String location, String phoneNumber, String webUrl, String[] keywords, Boolean isFavorites) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.webUrl = webUrl;
        this.keywords = keywords;
        this.isFavorites = isFavorites;
    }
}
