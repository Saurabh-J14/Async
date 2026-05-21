package com.async.app.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.repository.MockDataRepository;

public class SignUpViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public LiveData<String> getNameError() {
        return nameError;
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void signUp(String fullName, String email, String password, String confirmPassword) {
        // Reset errors
        nameError.setValue(null);
        emailError.setValue(null);
        passwordError.setValue(null);
        confirmPasswordError.setValue(null);
        authError.setValue(null);

        boolean hasError = false;

        if (fullName == null || fullName.trim().isEmpty()) {
            nameError.setValue("Full Name is required");
            hasError = true;
        }

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

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            confirmPasswordError.setValue("Confirm Password is required");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordError.setValue("Passwords do not match");
            hasError = true;
        }

        if (hasError) return;

        isLoading.setValue(true);

        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean success = repository.register(fullName, email, password);
            isLoading.setValue(false);
            if (success) {
                signupSuccess.setValue(true);
            } else {
                authError.setValue("This email address is already registered");
            }
        }, 1500);
    }
}
