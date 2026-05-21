package com.async.app.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.async.app.R;
import com.async.app.model.User;
import com.async.app.repository.MockDataRepository;
import com.async.app.view.customs.CustomCropView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView imgProfileAvatar;
    private EditText etProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;
    private Button btnChangePhoto;
    private Button btnSaveProfile;
    private Button btnLogout;

    // Crop UI overlays
    private LinearLayout layoutCropContainer;
    private CustomCropView cropView;
    private Button btnCropCancel;
    private Button btnCropApply;

    private Bitmap croppedBitmap = null;
    private MockDataRepository repository;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = MockDataRepository.getInstance();
        currentUser = repository.getCurrentUser();

        initViews();
        setupToolbar();
        bindUserData();
        setupListeners();
    }

    private void initViews() {
        imgProfileAvatar = findViewById(R.id.img_profile_avatar);
        etProfileName = findViewById(R.id.et_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        tvProfileRole = findViewById(R.id.tv_profile_role);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);

        layoutCropContainer = findViewById(R.id.layout_crop_container);
        cropView = findViewById(R.id.crop_view);
        btnCropCancel = findViewById(R.id.btn_crop_cancel);
        btnCropApply = findViewById(R.id.btn_crop_apply);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
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

        etProfileName.setText(currentUser.getFullName());
        tvProfileEmail.setText(currentUser.getEmail());
        tvProfileRole.setText(currentUser.getRole());

        // Load avatar if present
        if (currentUser.getProfileImage() != null) {
            try {
                byte[] decodedBytes = Base64.decode(currentUser.getProfileImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imgProfileAvatar.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imgProfileAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } else {
            imgProfileAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        btnCropCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropView.clear();
                layoutCropContainer.setVisibility(View.GONE);
            }
        });

        btnCropApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyImageCrop();
            }
        });
    }

    private static final int STORAGE_PERMISSION_CODE = 202;

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
        repository.logout();
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
                    layoutCropContainer.setVisibility(View.VISIBLE);
                    cropView.setBitmap(selectedBmp);
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
        Bitmap cropped = cropView.getCroppedBitmap();
        if (cropped != null) {
            // Recycle old croppedBitmap if present to free memory
            if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
                croppedBitmap.recycle();
            }
            croppedBitmap = cropped;
            imgProfileAvatar.setImageBitmap(croppedBitmap);
            cropView.clear(); // Clears source image of cropView to free memory immediately
            layoutCropContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Avatar crop applied! Save your profile to persist changes.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cropping failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileData() {
        String newName = etProfileName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update name
        currentUser.setFullName(newName);

        // Update profile picture
        if (croppedBitmap != null) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // Compress as JPEG with 80% quality to save memory and avoid OutOfMemory or SharedPreferences limits
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                repository.updateUserProfileImage(currentUser.getEmail(), base64Image);
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(this, "Failed to save profile photo due to low memory.", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Return success result
        setResult(RESULT_OK);
        finish();
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
