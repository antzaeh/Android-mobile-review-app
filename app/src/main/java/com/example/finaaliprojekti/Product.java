package com.example.finaaliprojekti;

import android.net.Uri;

import java.util.List;

public class Product {
    private String id;
    private String name;
    private String place;
    private String price;
    private String imageUri;
    private String comments;
    private double latitude;
    private double longitude;
    public Product() {
        // Default constructor required for Firestore's automatic data mapping.
    }
    public Product(String name, String place, String price, String imageUri, String comments, double latitude, double longitude) {

        this.name = name;
        this.place = place;
        this.price = price;
        this.imageUri = imageUri;
        this.comments = comments;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return imageUri;
    }

    public void setImage(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
