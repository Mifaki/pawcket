package com.mobile.pawcket.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;

public class UserManager {
    private static UserManager instance;
    private SharedPreferences sharedPreferences;

    private UserManager(Context context) {
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(DataSnapshot userSnapshot) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userSnapshot.getKey());
        editor.putString("email", userSnapshot.child("email").getValue(String.class));
        editor.putString("name", userSnapshot.child("name").getValue(String.class));
        editor.putString("username", userSnapshot.child("username").getValue(String.class));
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString("userId", null);
    }

    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    public String getName() {
        return sharedPreferences.getString("name", null);
    }

    public String getUsername() {
        return sharedPreferences.getString("username", null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }

    public void logOut() {
        sharedPreferences.edit().clear().apply();
    }
}