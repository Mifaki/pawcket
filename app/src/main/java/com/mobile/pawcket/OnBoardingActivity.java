package com.mobile.pawcket;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.mobile.pawcket.adapter.OnBoardingAdapter;

public class OnBoardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImageButton ibPrevious;
    private ImageButton ibNext;
    private TextView tvNavigation;
    private View[] indicators;
    private static final int ANIMATION_DURATION = 300;
    private static final int ACTIVE_INDICATOR_WIDTH = 48;
    private static final int INACTIVE_INDICATOR_WIDTH = 16;
    private static final int ACTIVE_COLOR = 0xFFEBB1E5;
    private static final int INACTIVE_COLOR = 0xFFCCCBCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);

        viewPager = findViewById(R.id.vpOnBoardingSlider);
        ibPrevious = findViewById(R.id.ibPrevious);
        ibNext = findViewById(R.id.ibNext);
        tvNavigation = findViewById(R.id.tvNavigation);

        indicators = new View[]{
                findViewById(R.id.indicator1),
                findViewById(R.id.indicator2),
                findViewById(R.id.indicator3)
        };

        OnBoardingAdapter adapter = new OnBoardingAdapter();
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
                animateIndicators(position);
            }
        });

        ibPrevious.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        ibNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < indicators.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                Intent intent = new Intent(OnBoardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateUI(int position) {
        ibPrevious.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        tvNavigation.setText(position == indicators.length - 1 ? "Masuk" : "Lanjut");
    }

    private void animateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            View indicator = indicators[i];
            boolean isActive = i == position;

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.indicator_corner_radius));
            indicator.setBackground(drawable);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(
                    indicator.getLayoutParams().width,
                    (int) ((int) (isActive ? ACTIVE_INDICATOR_WIDTH : INACTIVE_INDICATOR_WIDTH) *
                            getResources().getDisplayMetrics().density)
            );

            widthAnimator.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = indicator.getLayoutParams();
                params.width = (Integer) animation.getAnimatedValue();
                indicator.setLayoutParams(params);
            });

            ObjectAnimator colorAnimator = ObjectAnimator.ofArgb(
                    drawable,
                    "color",
                    drawable.getColor() != null ? drawable.getColor().getDefaultColor() :
                            (isActive ? INACTIVE_COLOR : ACTIVE_COLOR),
                    isActive ? ACTIVE_COLOR : INACTIVE_COLOR
            );

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, colorAnimator);
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.start();
        }
    }
}