package com.mobile.pawcket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.pawcket.R;
import com.mobile.pawcket.model.HistoryModel;

import java.util.ArrayList;
import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<HistoryModel> histories;
    private final String currentUsername;

    public HistoryAdapter(List<HistoryModel> histories, String currentUsername) {
        this.histories = histories;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(histories.get(position));
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public void updateHistories(List<HistoryModel> newHistories) {
        this.histories = new ArrayList<>(newHistories);
        notifyDataSetChanged();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCapturedImage;
        private final TextView tvHistoryUsername, tvCaption, tvHistorySex, tvHistoryAge, tvHistorySpecies;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCapturedImage = itemView.findViewById(R.id.ivCapturedImage);
            tvHistoryUsername = itemView.findViewById(R.id.tvHistoryUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvHistorySpecies = itemView.findViewById(R.id.tvHistorySpecies);
            tvHistorySex = itemView.findViewById(R.id.tvHistorySex);
            tvHistoryAge = itemView.findViewById(R.id.tvHistoryAge);
        }

        public void bind(HistoryModel history) {
            Glide.with(itemView.getContext())
                    .load(history.getImageUrl())
                    .into(ivCapturedImage);

            tvHistoryUsername.setText(history.getUsername());
            tvHistorySpecies.setText(history.getSpecies());
            tvHistorySex.setText(history.getSex());
            tvHistoryAge.setText(String.format("%s Tahun", history.getAge()));

            if (history.getCaption() == null || history.getCaption().isBlank()) {
                tvCaption.setVisibility(View.INVISIBLE);
            } else {
                tvCaption.setVisibility(View.VISIBLE);
                tvCaption.setText(history.getCaption());
            }
        }
    }
}