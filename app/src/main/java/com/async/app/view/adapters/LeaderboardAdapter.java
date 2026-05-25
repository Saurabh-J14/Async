package com.async.app.view.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.R;
import com.async.app.databinding.ItemLeaderboardUserBinding;
import com.async.app.model.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardItem> items;
    private final int startRank;

    public LeaderboardAdapter(List<LeaderboardItem> items, int startRank) {
        this.items = items;
        this.startRank = startRank;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardUserBinding itemBinding = ItemLeaderboardUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardItem item = items.get(position);
        int rank = startRank + position;
        holder.itemBinding.txtRank.setText(String.valueOf(rank));
        holder.itemBinding.txtName.setText(item.name);
        holder.itemBinding.txtDept.setText(item.department != null && !item.department.isEmpty() ? item.department : "Async Colleagues");
        holder.itemBinding.txtScore.setText(item.score + " pts");
        loadAvatar(item.avatar, holder.itemBinding.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void loadAvatar(String base64Str, ImageView imageView) {
        if (base64Str != null && !base64Str.trim().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemLeaderboardUserBinding itemBinding;

        public ViewHolder(@NonNull ItemLeaderboardUserBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
