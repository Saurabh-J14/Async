package com.async.app.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.async.app.R;
import com.async.app.databinding.ActivityProfileBinding;
import com.async.app.model.User;
import com.async.app.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int STORAGE_PERMISSION_CODE = 202;

    private ActivityProfileBinding binding;
    private Bitmap croppedBitmap = null;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        currentUser = sessionManager.getUser();

        setupToolbar();
        bindUserData();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarProfile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void bindUserData() {
        if (currentUser == null) {
            Toast.makeText(this, "Session expired, please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.etProfileName.setText(currentUser.getFullName());
        binding.etProfileUsername.setText(currentUser.getUsername());
        binding.etProfilePassword.setText(currentUser.getPassword());
        binding.tvProfileEmail.setText(currentUser.getEmail());
        binding.tvProfileRole.setText(currentUser.getRole());
        binding.switchTheme.setChecked(sessionManager.isDarkModeEnabled());

        // Load avatar if present
        if (currentUser.getProfileImage() != null) {
            try {
                byte[] decodedBytes = Base64.decode(currentUser.getProfileImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    binding.imgProfileAvatar.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                binding.imgProfileAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } else {
            binding.imgProfileAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });

        binding.btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        binding.switchTheme.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                sessionManager.setDarkModeEnabled(isChecked);
                sessionManager.applyTheme();
                recreate();
            }
        });

        binding.btnCropCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cropView.clear();
                binding.layoutCropContainer.setVisibility(View.GONE);
            }
        });

        binding.btnCropApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyImageCrop();
            }
        });
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        } else {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to read storage. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Downsample image to a maximum dimension of 1024px to prevent OutOfMemoryError
                Bitmap selectedBmp = getDownsampledBitmap(imageUri, 1024);
                if (selectedBmp != null) {
                    binding.layoutCropContainer.setVisibility(View.VISIBLE);
                    binding.cropView.setBitmap(selectedBmp);
                } else {
                    Toast.makeText(this, "Failed to load the selected image.", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(this, "Error reading image file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap getDownsampledBitmap(Uri uri, int maxDimension) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream is = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            if (is != null) {
                is.close();
            }

            int width = options.outWidth;
            int height = options.outHeight;
            if (width <= 0 || height <= 0) {
                return null;
            }

            // Calculate sample size to scale down the image
            int sampleSize = 1;
            while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
                sampleSize *= 2;
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            if (is != null) {
                is.close();
            }
            return bitmap;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void applyImageCrop() {
        Bitmap cropped = binding.cropView.getCroppedBitmap();
        if (cropped != null) {
            // Recycle old croppedBitmap if present to free memory
            if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
                croppedBitmap.recycle();
            }
            croppedBitmap = cropped;
            binding.imgProfileAvatar.setImageBitmap(croppedBitmap);
            binding.cropView.clear(); // Clears source image of cropView to free memory immediately
            binding.layoutCropContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Avatar crop applied! Save your profile to persist changes.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cropping failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileData() {
        final String newName = binding.etProfileName.getText().toString().trim();
        final String newUsername = binding.etProfileUsername.getText().toString().trim();
        final String newPassword = binding.etProfilePassword.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUsername = currentUser.getUsername();
        String currentPassword = currentUser.getPassword();
        String credentials = currentUsername + ":" + currentPassword;
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        File tempFile = null;
        if (croppedBitmap != null) {
            try {
                tempFile = new File(getCacheDir(), "temp_avatar.jpg");
                FileOutputStream fos = new FileOutputStream(tempFile);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().trim().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(currentUser.getProfileImage(), Base64.DEFAULT);
                tempFile = new File(getCacheDir(), "temp_avatar.jpg");
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(decodedBytes);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        okhttp3.RequestBody nameBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), newName);
        okhttp3.RequestBody usernameBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), newUsername);
        okhttp3.RequestBody passwordBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), newPassword);

        okhttp3.MultipartBody.Part avatarPart = null;
        if (tempFile != null && tempFile.exists()) {
            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), tempFile);
            avatarPart = okhttp3.MultipartBody.Part.createFormData("avatar", tempFile.getName(), fileBody);
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Saving...");

        final File finalTempFile = tempFile;
        com.async.app.network.ApiClient.getApiService().updateProfile(authHeader, nameBody, usernameBody, passwordBody, avatarPart)
                .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                        binding.btnSaveProfile.setEnabled(true);
                        binding.btnSaveProfile.setText("Save Profile");

                        if (finalTempFile != null && finalTempFile.exists()) {
                            finalTempFile.delete();
                        }

                        if (response.isSuccessful()) {
                            currentUser.setFullName(newName);
                            currentUser.setUsername(newUsername);
                            currentUser.setPassword(newPassword);

                            // If a new cropped image was selected, save it locally in the session too
                            if (croppedBitmap != null) {
                                try {
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                                    byte[] byteArray = outputStream.toByteArray();
                                    String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                    currentUser.setProfileImage(base64Image);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            sessionManager.saveUser(currentUser);
                            Toast.makeText(ProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            try {
                                String errorMsg = "Update failed";
                                if (response.errorBody() != null) {
                                    String errorStr = response.errorBody().string();
                                    if (errorStr.contains("\"detail\"")) {
                                        com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(errorStr).getAsJsonObject();
                                        if (obj.has("detail")) {
                                            errorMsg = obj.get("detail").getAsString();
                                        }
                                    } else if (errorStr.contains("\"message\"")) {
                                        com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(errorStr).getAsJsonObject();
                                        if (obj.has("message")) {
                                            errorMsg = obj.get("message").getAsString();
                                        }
                                    } else {
                                        errorMsg = errorStr;
                                    }
                                }
                                Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                        binding.btnSaveProfile.setEnabled(true);
                        binding.btnSaveProfile.setText("Save Profile");
                        if (finalTempFile != null && finalTempFile.exists()) {
                            finalTempFile.delete();
                        }
                        Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
            croppedBitmap.recycle();
        }
    }
}
