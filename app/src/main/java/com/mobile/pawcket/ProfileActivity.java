package com.mobile.pawcket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobile.pawcket.utils.UserManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvUsername;
    private ImageButton ibBack;
    private ConstraintLayout clFriendActionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        UserManager userManager = UserManager.getInstance(this);

        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        ibBack = findViewById(R.id.ibBack);
        clFriendActionContainer = findViewById(R.id.clFriendActionContainer);

        tvName.setText(userManager.getName());
        tvUsername.setText(userManager.getUsername());

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        clFriendActionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
    }
}