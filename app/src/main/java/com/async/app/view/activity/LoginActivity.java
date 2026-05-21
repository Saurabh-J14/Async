package com.async.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.async.app.databinding.ActivityLoginBinding;
import com.async.app.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Prepopulate with mock credentials for easy verification
        binding.etEmail.setText("user@async.com");
        binding.etPassword.setText("password123");

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Observe field errors
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

        // Observe authorization failure
        viewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe loading indicator
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.loginProgressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
            } else {
                binding.loginProgressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(email, password);
        });

        binding.btnGotoSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
