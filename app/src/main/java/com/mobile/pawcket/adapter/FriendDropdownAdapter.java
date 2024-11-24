package com.mobile.pawcket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.pawcket.R;
import com.mobile.pawcket.model.FriendDropdownModel;

import java.util.List;

public class FriendDropdownAdapter extends RecyclerView.Adapter<FriendDropdownAdapter.FriendDropdownViewHolder> {
    private List<FriendDropdownModel> friends;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(String username);
    }

    public FriendDropdownAdapter(List<FriendDropdownModel> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendDropdownViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dropdown, parent, false);
        return new FriendDropdownViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendDropdownViewHolder holder, int position) {
        FriendDropdownModel friend = friends.get(position);
        holder.tvUsername.setText(friend.getUsername());
        holder.ivProfilePic.setImageResource(friend.getProfilePicResource());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend.getUsername());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendDropdownViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView ivProfilePic;

        FriendDropdownViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
        }
    }
}
