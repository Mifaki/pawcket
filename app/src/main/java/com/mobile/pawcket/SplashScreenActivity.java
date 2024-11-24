package com.mobile.pawcket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobile.pawcket.widget.CircleExpandView;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private static final int ANIMATION_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startRevealAnimation();
    }

    private void startRevealAnimation() {
        CircleExpandView circleView = findViewById(R.id.circleView);

        float maxRadius = (float) Math.hypot(
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels
        ) / 2f;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, maxRadius);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            circleView.setRadius(animatedValue);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                circleView.postDelayed(() -> {
                    startMainActivity();
                }, 1500);
            }
        });

        animator.start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }
}