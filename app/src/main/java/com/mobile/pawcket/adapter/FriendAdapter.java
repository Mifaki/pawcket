package com.mobile.pawcket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.pawcket.R;
import com.mobile.pawcket.model.FriendModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<FriendModel> friends = new ArrayList<>();
    private boolean isSuggestion;
    private OnFriendActionListener listener;
    private int[] profileImages = {
            R.drawable.cat_profile_blue,
            R.drawable.cat_profile_green,
            R.drawable.cat_profile_light_green,
            R.drawable.cat_profile_lime,
            R.drawable.cat_profile_orange,
            R.drawable.cat_profile_pink,
            R.drawable.cat_profile_purple,
            R.drawable.cat_profile_red
    };

    private Random random = new Random();

    public interface OnFriendActionListener {
        void onFriendAction(FriendModel friend, int position);
    }

    public FriendAdapter(boolean isSuggestion, OnFriendActionListener listener) {
        this.isSuggestion = isSuggestion;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friends.get(position), position);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void submitList(List<FriendModel> newFriends) {
        this.friends.clear();
        this.friends.addAll(newFriends);
        notifyDataSetChanged();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfile;
        private TextView tvName;
        private Button btnAdd;
        private ImageButton btnRemove;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);

            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(FriendModel friend, int position) {
            ivProfile.setImageResource(profileImages[random.nextInt(profileImages.length)]);

            tvName.setText(friend.getName());

            if (isSuggestion) {
                btnAdd.setVisibility(View.VISIBLE);
                btnRemove.setVisibility(View.GONE);
                btnAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFriendAction(friend, position);
                    }
                });
            } else {
                btnAdd.setVisibility(View.GONE);
                btnRemove.setVisibility(View.VISIBLE);
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFriendAction(friend, position);
                    }
                });
            }
        }
    }
}
