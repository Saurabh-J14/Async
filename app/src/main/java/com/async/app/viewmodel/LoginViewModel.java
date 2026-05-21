package com.async.app.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.repository.MockDataRepository;

public class LoginViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String email, String password) {
        // Reset errors
        emailError.setValue(null);
        passwordError.setValue(null);
        authError.setValue(null);

        boolean hasError = false;

        if (email == null || email.trim().isEmpty()) {
            emailError.setValue("Email address is required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Please enter a valid email address");
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Password is required");
            hasError = true;
        } else if (password.length() < 6) {
            passwordError.setValue("Password must be at least 6 characters");
            hasError = true;
        }

        if (hasError) return;

        isLoading.setValue(true);

        // Simulate network delay of 1.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean success = repository.login(email, password);
            isLoading.setValue(false);
            if (success) {
                loginSuccess.setValue(true);
            } else {
                authError.setValue("Invalid email or password");
            }
        }, 1500);
    }
}
