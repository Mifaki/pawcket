package com.mobile.pawcket.model;

public class FriendDropdownModel {
    private String username;
    private int profilePicResource;

    public FriendDropdownModel() {
    }

    public FriendDropdownModel(String username, int profilePicResource) {
        this.username = username;
        this.profilePicResource = profilePicResource;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getProfilePicResource() {
        return profilePicResource;
    }

    public void setProfilePicResource(int profilePicResource) {
        this.profilePicResource = profilePicResource;
    }
}
