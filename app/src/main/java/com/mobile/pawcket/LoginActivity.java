package com.mobile.pawcket;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText etName, etPassword;
    private Button btLogin;
    private TextView tvRegisterRedirect;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        btLogin = findViewById(R.id.btLogin);

        String text = "Belum punya akun? Daftar";

        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf("Daftar");
        if (start != -1) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_500)), start, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvRegisterRedirect.setText(spannable);

        tvRegisterRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUser();
            }
        });
    }

    public void checkUser() {
        String name = etName.getText().toString();
        String password = etPassword.getText().toString();

        reference = FirebaseDatabase.getInstance("https://pawcket-9c810-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        Query checkUserDatabase = reference.orderByChild("name").equalTo(name);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    etName.setError(null);
                    for(DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                        if (Objects.equals(passwordFromDB, password)) {
                            etPassword.setError(null);
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            etPassword.setError("Invalid Credentials");
                            etPassword.requestFocus();
                        }
                    }
                } else {
                    etName.setError("User does not exist");
                    etName.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error.getMessage());
            }
        });
    }
}
