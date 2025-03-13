package com.example.researchproject.iam;
public class Ad {
    private String adId;
    private String title;
    private String imageUrl;
    private Long timestamp;
    public Ad() {} // Needed for Firebase
    public Ad(String adId, String title, String imageUrl, Long timestamp) {
        this.adId = adId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.timestamp=timestamp;
    }
    public String getAdId() { return adId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    // Getter và Setter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
