package com.example.zolp;

public class RestaurantInfo {
    public String id, name, imageUrl, x, y, location, phoneNumber, webUrl, visitorRating;
    public String keyword;
    public Boolean isFavorites;

    public RestaurantInfo(String id, String name, String imageUrl, String location, String x, String y, String phoneNumber, String visitorRating, String webUrl, String keyword, Boolean isFavorites) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.location = location;
        this.x = x;
        this.y = y;
        this.phoneNumber = phoneNumber;
        this.visitorRating = visitorRating;
        this.webUrl = webUrl;
        this.keyword = keyword;
        this.isFavorites = isFavorites;
    }
}
