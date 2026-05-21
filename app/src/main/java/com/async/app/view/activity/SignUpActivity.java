package com.async.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.async.app.databinding.ActivitySignupBinding;
import com.async.app.viewmodel.SignUpViewModel;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getSignupSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(SignUpActivity.this, "Account Created Successfully! 🎉", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        viewModel.getNameError().observe(this, error -> {
            if (error != null) {
                binding.txtFullNameError.setVisibility(View.VISIBLE);
                binding.txtFullNameError.setText(error);
            } else {
                binding.txtFullNameError.setVisibility(View.GONE);
            }
        });

        viewModel.getEmailError().observe(this, error -> {
            if (error != null) {
                binding.txtEmailError.setVisibility(View.VISIBLE);
                binding.txtEmailError.setText(error);
            } else {
                binding.txtEmailError.setVisibility(View.GONE);
            }
        });

        viewModel.getPasswordError().observe(this, error -> {
            if (error != null) {
                binding.txtPasswordError.setVisibility(View.VISIBLE);
                binding.txtPasswordError.setText(error);
            } else {
                binding.txtPasswordError.setVisibility(View.GONE);
            }
        });

        viewModel.getConfirmPasswordError().observe(this, error -> {
            if (error != null) {
                binding.txtConfirmPasswordError.setVisibility(View.VISIBLE);
                binding.txtConfirmPasswordError.setText(error);
            } else {
                binding.txtConfirmPasswordError.setVisibility(View.GONE);
            }
        });

        viewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.signupProgressBar.setVisibility(View.VISIBLE);
                binding.btnSignup.setEnabled(false);
            } else {
                binding.signupProgressBar.setVisibility(View.GONE);
                binding.btnSignup.setEnabled(true);
            }
        });
    }

    private void setupListeners() {
        binding.btnSignup.setOnClickListener(v -> {
            String name = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
            viewModel.signUp(name, email, password, confirmPassword);
        });

        binding.btnGotoLogin.setOnClickListener(v -> finish());
    }
}
