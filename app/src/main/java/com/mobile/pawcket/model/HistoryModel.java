package com.mobile.pawcket.model;

public class HistoryModel {
    private String id, imageUrl, caption, username, species, sex, age;
    private Long timestamp;

    public HistoryModel() {

    }

    public HistoryModel(String id, String caption, String imageUrl, String username, String species, String sex, String age, Long timestamp) {
        this.id = id;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.username = username;
        this.species = species;
        this.sex = sex;
        this.age = age;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
