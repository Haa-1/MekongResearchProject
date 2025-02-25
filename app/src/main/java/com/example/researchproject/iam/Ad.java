package com.example.researchproject.iam;
public class Ad {
    private String adId;
    private String title;
    private String imageUrl;
    public Ad() {} // Needed for Firebase
    public Ad(String adId, String title, String imageUrl) {
        this.adId = adId;
        this.title = title;
        this.imageUrl = imageUrl;
    }
    public String getAdId() { return adId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
