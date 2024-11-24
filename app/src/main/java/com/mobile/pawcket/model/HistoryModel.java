package com.mobile.pawcket.model;

public class HistoryModel {
    private String id, imageUrl, caption, username;
    private Long timestamp;

    public HistoryModel() {

    }

    public HistoryModel(String id, String caption, String imageUrl, Long timestamp, String username) {
        this.id = id;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
