package com.mobile.pawcket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pawcket.adapter.FriendAdapter;
import com.mobile.pawcket.model.FriendModel;
import com.mobile.pawcket.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {
    private TextView tvNotFound;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FriendAdapter friendAdapter, suggestionAdapter;
    private ConstraintLayout clFriendContainer, clSuggestionContainer;
    private RecyclerView rvFriend, rvSuggestion;
    private ImageButton ibBack;
    private SearchView svSearchFriend;
    private UserManager userManager;
    private List<FriendModel> allFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend);

        userManager = UserManager.getInstance(this);

        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        tvNotFound = findViewById(R.id.tvNotFound);
        ibBack = findViewById(R.id.ibBack);
        clFriendContainer = findViewById(R.id.clFriendContainer);
        clSuggestionContainer = findViewById(R.id.clSuggestionContainer);
        rvFriend = findViewById(R.id.rvFriend);
        rvSuggestion = findViewById(R.id.rvSuggestion);
        svSearchFriend = findViewById(R.id.svSearchFriend);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupSearchView();

        database = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_URL);
        reference = database.getReference();

        setupAdapters();
        loadData();
    }

    private void setupSearchView() {
        svSearchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFriends(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                rvSuggestion.setVisibility(newText.isEmpty() ? View.VISIBLE : View.GONE);
                filterFriends(newText);
                return true;
            }
        });

        svSearchFriend.setOnCloseListener(() -> {
            clFriendContainer.setVisibility(View.VISIBLE);
            clSuggestionContainer.setVisibility(View.VISIBLE);
            friendAdapter.submitList(allFriends);
            tvNotFound.setVisibility(View.GONE);
            return false;
        });
    }

    private void filterFriends(String query) {
        List<FriendModel> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allFriends);
        } else {
            String lowercaseQuery = query.toLowerCase().trim();
            for (FriendModel friend : allFriends) {
                if (friend.getName().toLowerCase().contains(lowercaseQuery)) {
                    filteredList.add(friend);
                }
            }
        }

        friendAdapter.submitList(filteredList);

        tvNotFound.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        clSuggestionContainer.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        clFriendContainer.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupAdapters() {
        friendAdapter = new FriendAdapter(false, this::removeFriend);
        rvFriend.setLayoutManager(new LinearLayoutManager(this));
        rvFriend.setAdapter(friendAdapter);

        suggestionAdapter = new FriendAdapter(true, this::addFriend);
        rvSuggestion.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestion.setAdapter(suggestionAdapter);
    }

    private void loadData() {
        reference.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFriends.clear();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    FriendModel friend = friendSnapshot.getValue(FriendModel.class);
                    if (friend != null) {
                        allFriends.add(friend);
                    }
                }
                friendAdapter.submitList(allFriends);

                if (!svSearchFriend.getQuery().toString().isEmpty()) {
                    filterFriends(svSearchFriend.getQuery().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendActivity.this,
                        "Error loading friends: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        reference.child("suggestions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FriendModel> suggestionsList = new ArrayList<>();
                for (DataSnapshot suggestionSnapshot : snapshot.getChildren()) {
                    FriendModel suggestion = suggestionSnapshot.getValue(FriendModel.class);
                    if (suggestion != null) {
                        suggestionsList.add(suggestion);
                    }
                }
                suggestionAdapter.submitList(suggestionsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendActivity.this,
                        "Error loading suggestions: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriend(FriendModel friend, int position) {
        String key = reference.child("friends").push().getKey();
        if (key != null) {
            DatabaseReference suggestionsRef = reference.child("suggestions");
            Query query = suggestionsRef.orderByChild("name").equalTo(friend.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot suggestionSnapshot : snapshot.getChildren()) {
                        suggestionSnapshot.getRef().removeValue();
                    }

                    reference.child("friends").child(key).setValue(friend)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(FriendActivity.this,
                                            "Friend added successfully",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(FriendActivity.this,
                                            "Failed to add friend",
                                            Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FriendActivity.this,
                            "Error updating lists: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void removeFriend(FriendModel friend, int position) {
        DatabaseReference friendRef = reference.child("friends");
        Query query = friendRef.orderByChild("name").equalTo(friend.getName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    friendSnapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                String newKey = reference.child("suggestions").push().getKey();
                                if (newKey != null) {
                                    reference.child("suggestions").child(newKey).setValue(friend)
                                            .addOnSuccessListener(aVoid2 ->
                                                    Toast.makeText(FriendActivity.this,
                                                            "Friend removed sucessfully",
                                                            Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(FriendActivity.this,
                                                            "Failed to friend add to suggestions",
                                                            Toast.LENGTH_SHORT).show());
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(FriendActivity.this,
                                            "Failed to remove friend",
                                            Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendActivity.this,
                        "Error updating lists: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}