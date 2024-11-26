package com.mobile.pawcket;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pawcket.adapter.FriendDropdownAdapter;
import com.mobile.pawcket.adapter.HistoryAdapter;
import com.mobile.pawcket.model.FriendDropdownModel;
import com.mobile.pawcket.model.HistoryModel;
import com.mobile.pawcket.utils.UserManager;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ViewPager2 viewPager;
    private HistoryAdapter historyAdapter;
    private DatabaseReference reference;
    private UserManager userManager;
    private List<HistoryModel> histories;
    private ImageButton ibDelete, ibDownload, ibCameraAction;
    private ImageView ivUserProfile;

    private RecyclerView rvFriends;
    private ConstraintLayout clFriendContainer, clFriendListContainer;
    private TextView tvFriend;
    private List<FriendDropdownModel> friends;
    private FriendDropdownAdapter friendDropdownAdapter;
    private String currentUsername = "Everyone";
    private static final int[] PROFILE_PICS = {
            R.drawable.cat_profile_blue,
            R.drawable.cat_profile_green,
            R.drawable.cat_profile_lime,
            R.drawable.cat_profile_light_green,
            R.drawable.cat_profile_orange,
            R.drawable.cat_profile_pink,
            R.drawable.cat_profile_purple,
            R.drawable.cat_profile_red
    };
    private float startY;
    private static final float SWIPE_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        userManager = UserManager.getInstance(this);

        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        reference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_URL).getReference().child("histories");
        histories = new ArrayList<>();

        ibDelete = findViewById(R.id.ibDelete);
        ibDownload = findViewById(R.id.ibDownload);
        ibCameraAction = findViewById(R.id.ibCameraAction);
        ivUserProfile = findViewById(R.id.ivUserProfile);

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = viewPager.getCurrentItem();
                if (currentPosition >= 0 && currentPosition < histories.size()) {
                    HistoryModel currentHistory = histories.get(currentPosition);
                    if (currentHistory.getUsername().equals(userManager.getUsername())) {
                        deleteHistory(currentHistory.getId());
                    }
                }
            }
        });

        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = viewPager.getCurrentItem();
                if (currentPosition >= 0 && currentPosition < histories.size()) {
                    HistoryModel currentHistory = histories.get(currentPosition);
                    downloadImageWithMediaStore(currentHistory.getImageUrl());
                }
            }
        });

        ibCameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        setupFriendsDropdown();

        if (!histories.isEmpty()) {
            viewPager.setCurrentItem(0, false);
        }

        setupViewPager();
        loadHistories();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_down_enter, R.anim.slide_down_exit);
                finish();
            }
        });
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.vpHistory);
        historyAdapter = new HistoryAdapter(histories, userManager.getUsername());

        viewPager.setAdapter(historyAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
            }
        });

        View touchInterceptor = viewPager.getChildAt(0);
        touchInterceptor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return false;

                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();
                        float deltaY = endY - startY;

                        if (viewPager.getCurrentItem() == 0 && deltaY > SWIPE_THRESHOLD) {
                            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_down_enter, R.anim.slide_down_exit);
                            finish();
                            return true;
                        }
                        return false;
                }
                return false;
            }
        });

        viewPager.setCurrentItem(0, false);
    }

    private void updateButtonVisibility(int position) {
        if (position >= 0 && position < histories.size()) {
            HistoryModel currentHistory = histories.get(position);
            ibDelete.setVisibility(
                    currentHistory.getUsername().equals(userManager.getUsername())
                            ? View.VISIBLE : View.INVISIBLE
            );
        }
    }

    private void loadHistories() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                histories.clear();
                for (DataSnapshot historySnapshot : snapshot.getChildren()) {
                    HistoryModel history = new HistoryModel(
                            historySnapshot.getKey(),
                            historySnapshot.child("caption").getValue(String.class),
                            historySnapshot.child("imageUrl").getValue(String.class),
                            historySnapshot.child("username").getValue(String.class),
                            historySnapshot.child("species").getValue(String.class),
                            historySnapshot.child("sex").getValue(String.class),
                            historySnapshot.child("age").getValue(String.class),
                            historySnapshot.child("timestamp").getValue(Long.class)
                    );
                    histories.add(history);
                }
                Collections.sort(histories, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));
                historyAdapter.notifyDataSetChanged();

                initializeFriendsList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFriendsDropdown() {
        clFriendContainer = findViewById(R.id.clFriendContainer);
        clFriendListContainer = findViewById(R.id.clFriendListContainer);
        tvFriend = findViewById(R.id.tvFriend);
        rvFriends = clFriendListContainer.findViewById(R.id.rvFriends);

        friends = new ArrayList<>();
        friendDropdownAdapter = new FriendDropdownAdapter(friends, username -> {
            tvFriend.setText(username);
            currentUsername = username;
            filterHistories();
            clFriendListContainer.setVisibility(View.GONE);
        });

        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(friendDropdownAdapter);

        clFriendContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clFriendListContainer.setVisibility(
                        clFriendListContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                );
            }
        });

        findViewById(R.id.main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clFriendListContainer.getVisibility() == View.VISIBLE) {
                    clFriendListContainer.setVisibility(View.GONE);
                }
            }
        });

        clFriendListContainer.setOnClickListener(v -> {
            // Do nothing, just prevent click from propagating to main layout
        });
    }

    private void initializeFriendsList() {
        Set<String> uniqueUsernames = new HashSet<>();
        friends.clear();

        friends.add(new FriendDropdownModel("Everyone", PROFILE_PICS[0]));

        String[] species = {"Kucing", "Anjing"};
        String[] sex = {"Jantan", "Betina"};
        String[] ages = {"1", "2", "3", "4", "5"};

        for (HistoryModel history : histories) {
            String username = history.getUsername();
            if (!uniqueUsernames.contains(username)) {
                uniqueUsernames.add(username);

                int speciesIndex = Math.abs(username.hashCode()) % species.length;
                int sexIndex = Math.abs(username.hashCode() * 31) % sex.length;
                int ageIndex = Math.abs(username.hashCode() * 37) % ages.length;

                for (HistoryModel h : histories) {
                    if (h.getUsername().equals(username)) {
                        h.setSpecies(species[speciesIndex]);
                        h.setSex(sex[sexIndex]);
                        h.setAge(ages[ageIndex]);
                    }
                }

                int profilePic = PROFILE_PICS[Math.abs(username.hashCode()) % PROFILE_PICS.length];
                friends.add(new FriendDropdownModel(username, profilePic));
            }
        }

        friendDropdownAdapter.notifyDataSetChanged();
        historyAdapter.notifyDataSetChanged();
    }

    private void filterHistories() {
        List<HistoryModel> filteredHistories = new ArrayList<>();
        if (currentUsername.equals("Everyone")) {
            filteredHistories.addAll(histories);
        } else {
            for (HistoryModel history : histories) {
                if (history.getUsername().equals(currentUsername)) {
                    filteredHistories.add(history);
                }
            }
        }

        historyAdapter.updateHistories(filteredHistories);

        if (!filteredHistories.isEmpty()) {
            viewPager.setCurrentItem(0, false);
        }

        updateButtonVisibility(0);
    }


    private void deleteHistory(String historyId) {
        reference.child(historyId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    histories.removeIf(history -> history.getId().equals(historyId));

                    historyAdapter.updateHistories(histories);

                    if (histories.isEmpty()) {
                        Toast.makeText(this, "No more history items", Toast.LENGTH_SHORT).show();
                    } else {
                        viewPager.setCurrentItem(Math.min(viewPager.getCurrentItem(), histories.size() - 1), false);
                    }

                    Toast.makeText(this, "History deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete history: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void downloadImageWithMediaStore(String imageUrl) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                String filename = "pawcket_" + System.currentTimeMillis() + ".jpg";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                ContentResolver resolver = getContentResolver();
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri != null) {
                    try (OutputStream out = resolver.openOutputStream(imageUri)) {
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            runOnUiThread(() -> Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to download image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}