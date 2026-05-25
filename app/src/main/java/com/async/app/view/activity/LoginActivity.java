package com.async.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.async.app.databinding.ActivityLoginBinding;
import com.async.app.model.User;
import com.async.app.util.SessionManager;
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

        setupObservers();
        setupListeners();
        
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getUser();
            binding.cardForm.setVisibility(View.GONE);
            binding.btnGotoSignup.setVisibility(View.GONE);
            binding.loginProgressBar.setVisibility(View.VISIBLE);
            viewModel.checkServerAuthentication(user.getUsername(), user.getPassword());
        }
    }

    private void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getAuthCheckSuccess().observe(this, authenticated -> {
            if (authenticated != null) {
                if (authenticated) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    SessionManager.getInstance(LoginActivity.this).logout();
                    Toast.makeText(LoginActivity.this, "Session expired, please log in again.", Toast.LENGTH_SHORT).show();
                    binding.cardForm.setVisibility(View.VISIBLE);
                    binding.btnGotoSignup.setVisibility(View.VISIBLE);
                    binding.loginProgressBar.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getUsernameError().observe(this, error -> {
            if (error != null) {
                binding.txtUsernameError.setVisibility(View.VISIBLE);
                binding.txtUsernameError.setText(error);
            } else {
                binding.txtUsernameError.setVisibility(View.GONE);
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

        viewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (binding.cardForm.getVisibility() == View.VISIBLE) {
                if (isLoading != null && isLoading) {
                    binding.loginProgressBar.setVisibility(View.VISIBLE);
                    binding.btnLogin.setEnabled(false);
                } else {
                    binding.loginProgressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                }
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(username, password);
        });

        binding.btnGotoSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
