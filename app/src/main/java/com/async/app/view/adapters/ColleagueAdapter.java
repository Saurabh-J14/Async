package com.async.app.view.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.R;
import com.async.app.databinding.ItemDepartmentColleagueBinding;
import com.async.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class ColleagueAdapter extends RecyclerView.Adapter<ColleagueAdapter.ColleagueViewHolder> {

    private final List<User> colleagues = new ArrayList<>();

    public void setColleagues(List<User> newColleagues) {
        colleagues.clear();
        if (newColleagues != null) {
            colleagues.addAll(newColleagues);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColleagueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepartmentColleagueBinding binding = ItemDepartmentColleagueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ColleagueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColleagueViewHolder holder, int position) {
        holder.bind(colleagues.get(position));
    }

    @Override
    public int getItemCount() {
        return colleagues.size();
    }

    static class ColleagueViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepartmentColleagueBinding binding;

        public ColleagueViewHolder(@NonNull ItemDepartmentColleagueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.txtColleagueName.setText(user.getFullName());
            binding.txtColleagueRole.setText(user.getRole() != null ? user.getRole().toUpperCase() : "EMPLOYEE");

            // Handle Profile Image
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        binding.imgColleagueAvatar.setImageBitmap(bitmap);
                    } else {
                        binding.imgColleagueAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                } catch (Exception e) {
                    binding.imgColleagueAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                binding.imgColleagueAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        }
    }
}
