package com.mobile.pawcket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.pawcket.R;

public class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder> {

    private static final int[] images = {
            R.drawable.on_boarding_cat_1,
            R.drawable.on_boarding_cat_2,
            R.drawable.on_boarding_cat_3
    };

    private static final int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };

    private static final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    @NonNull
    @Override
    public OnBoardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_on_boarding_slider, parent, false);
        return new OnBoardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnBoardingViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class OnBoardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public OnBoardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSlider);
            titleTextView = itemView.findViewById(R.id.tvSliderTitle);
            descriptionTextView = itemView.findViewById(R.id.tvSliderDescription);
        }

        void bind(int position) {
            imageView.setImageResource(images[position]);
            titleTextView.setText(titles[position]);
            descriptionTextView.setText(descriptions[position]);
        }
    }
}