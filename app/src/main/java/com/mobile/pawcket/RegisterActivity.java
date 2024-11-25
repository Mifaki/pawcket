package com.mobile.pawcket;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobile.pawcket.model.RegisterModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btRegister;
    private TextView tvLoginRedirect, tvPrivacyPolicy;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btRegister = findViewById(R.id.btRegister);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);

        String privacyPolicyText = "Dengan melanjutkan Anda menyetujui Ketentuan Layanan dan Kebijakan Pawkect";

        SpannableString privacyPolicySpannable = new SpannableString(privacyPolicyText);

        int startKetentuan = privacyPolicyText.indexOf("Ketentuan Layanan");
        int endKetentuan = startKetentuan + "Ketentuan Layanan".length();

        int startKebijakan = privacyPolicyText.indexOf("Kebijakan");
        int endKebijakan = startKebijakan + "Kebijakan".length();

        if (startKetentuan != -1) {
            privacyPolicySpannable.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_500)),
                    startKetentuan,
                    endKetentuan,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            privacyPolicySpannable.setSpan(
                    new UnderlineSpan(),
                    startKetentuan,
                    endKetentuan,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        if (startKebijakan != -1) {
            privacyPolicySpannable.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_500)),
                    startKebijakan,
                    endKebijakan,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            privacyPolicySpannable.setSpan(
                    new UnderlineSpan(),
                    startKebijakan,
                    endKebijakan,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        tvPrivacyPolicy.setText(privacyPolicySpannable);

        String redirectText = "Sudah memiliki akun? Masuk";

        SpannableString redirectSpannable = new SpannableString(redirectText);

        int start = redirectText.indexOf("Masuk");
        if (start != -1) {
            redirectSpannable.setSpan(new StyleSpan(Typeface.BOLD), start, redirectText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            redirectSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_500)), start, redirectText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvLoginRedirect.setText(redirectSpannable);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (email.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    etConfirmPassword.setError("Password is not the same");
                    return;
                }

                database = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_URL);
                reference = database.getReference("users");

                RegisterModel registerModel = new RegisterModel(email, name, username, password);

                reference.child(username).setValue(registerModel)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.d("test", "Error on register" + e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });

        tvLoginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}