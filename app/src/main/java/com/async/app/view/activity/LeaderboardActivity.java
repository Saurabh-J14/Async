package com.async.app.view.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.R;
import com.async.app.databinding.ActivityLeaderboardBinding;
import com.async.app.databinding.ItemLeaderboardUserBinding;
import com.async.app.model.User;
import com.async.app.network.ApiClient;
import com.async.app.util.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.async.app.model.LeaderboardItem;
import com.async.app.view.adapters.LeaderboardAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private ActivityLeaderboardBinding binding;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        currentUser = sessionManager.getUser();

        if (currentUser == null) {
            Toast.makeText(this, "Session expired, please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        setupToolbar();
        fetchLeaderboardData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarLeaderboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchLeaderboardData() {
        String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.scrollContainer.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        ApiClient.getApiService().getLeaderboard(authHeader).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        List<LeaderboardItem> items = parseLeaderboard(jsonString);
                        if (items.isEmpty()) {
                            showEmptyState();
                        } else {
                            bindLeaderboardData(items);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private List<LeaderboardItem> parseLeaderboard(String jsonString) {
        List<LeaderboardItem> list = new ArrayList<>();
        try {
            JsonArray array = new JsonParser().parse(jsonString).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();

                String name = "User";
                if (obj.has("fullName")) {
                    name = obj.get("fullName").getAsString();
                } else if (obj.has("name")) {
                    name = obj.get("name").getAsString();
                } else if (obj.has("username")) {
                    name = obj.get("username").getAsString();
                }

                String email = obj.has("email") ? obj.get("email").getAsString() : "";
                String dept = obj.has("department") ? obj.get("department").getAsString() : "";

                String avatar = null;
                if (obj.has("profileImage") && !obj.get("profileImage").isJsonNull()) {
                    avatar = obj.get("profileImage").getAsString();
                } else if (obj.has("avatar") && !obj.get("avatar").isJsonNull()) {
                    avatar = obj.get("avatar").getAsString();
                }

                int score = 0;
                if (obj.has("score")) {
                    score = obj.get("score").getAsInt();
                } else if (obj.has("points")) {
                    score = obj.get("points").getAsInt();
                } else if (obj.has("completedTasks")) {
                    score = obj.get("completedTasks").getAsInt();
                } else if (obj.has("tasksCompleted")) {
                    score = obj.get("tasksCompleted").getAsInt();
                } else if (obj.has("completed_tasks")) {
                    score = obj.get("completed_tasks").getAsInt();
                }

                list.add(new LeaderboardItem(name, email, dept, avatar, score));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(list, (a, b) -> Integer.compare(b.score, a.score));
        return list;
    }

    private void bindLeaderboardData(List<LeaderboardItem> items) {
        binding.scrollContainer.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        LeaderboardItem first = items.get(0);
        binding.txtFirstName.setText(first.name);
        binding.txtFirstScore.setText(first.score + " pts");
        loadAvatar(first.avatar, binding.imgFirstAvatar);
        binding.podiumFirst.setVisibility(View.VISIBLE);

        if (items.size() >= 2) {
            LeaderboardItem second = items.get(1);
            binding.txtSecondName.setText(second.name);
            binding.txtSecondScore.setText(second.score + " pts");
            loadAvatar(second.avatar, binding.imgSecondAvatar);
            binding.podiumSecond.setVisibility(View.VISIBLE);
        } else {
            binding.podiumSecond.setVisibility(View.INVISIBLE);
        }

        if (items.size() >= 3) {
            LeaderboardItem third = items.get(2);
            binding.txtThirdName.setText(third.name);
            binding.txtThirdScore.setText(third.score + " pts");
            loadAvatar(third.avatar, binding.imgThirdAvatar);
            binding.podiumThird.setVisibility(View.VISIBLE);
        } else {
            binding.podiumThird.setVisibility(View.INVISIBLE);
        }

        List<LeaderboardItem> remaining = new ArrayList<>();
        if (items.size() > 3) {
            remaining = items.subList(3, items.size());
        }

        LeaderboardAdapter adapter = new LeaderboardAdapter(remaining, 4);
        binding.recyclerLeaderboard.setAdapter(adapter);
    }

    private void showEmptyState() {
        binding.scrollContainer.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
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

}
