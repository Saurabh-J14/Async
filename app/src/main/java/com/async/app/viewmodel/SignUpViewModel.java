package com.async.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.User;

public class SignUpViewModel extends AndroidViewModel {

    public SignUpViewModel(@NonNull Application application) {
        super(application);
    }

    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();
    private final MutableLiveData<String> departmentError = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public LiveData<String> getNameError() {
        return nameError;
    }

    public LiveData<String> getUsernameError() {
        return usernameError;
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

    public LiveData<String> getDepartmentError() {
        return departmentError;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void signUp(String fullName, String username, String email, String password, String confirmPassword, String role, String department) {
        nameError.setValue(null);
        usernameError.setValue(null);
        emailError.setValue(null);
        passwordError.setValue(null);
        confirmPasswordError.setValue(null);
        departmentError.setValue(null);
        authError.setValue(null);

        boolean hasError = false;

        if (fullName == null || fullName.trim().isEmpty()) {
            nameError.setValue("Full Name is required");
            hasError = true;
        }

        if (username == null || username.trim().isEmpty()) {
            usernameError.setValue("Username is required");
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

        if (department == null || department.trim().isEmpty()) {
            departmentError.setValue("Department is required");
            hasError = true;
        }

        if (hasError) return;

        isLoading.setValue(true);

        com.async.app.network.model.RegisterRequest registerRequest = new com.async.app.network.model.RegisterRequest(
                fullName,
                username,
                password,
                email,
                role.toLowerCase(),
                department
        );

        com.async.app.network.ApiClient.getApiService().registerUser(registerRequest).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    String json = "";
                    try {
                        if (response.body() != null) {
                            json = response.body().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Parse and save token
                    try {
                        if (json != null && !json.trim().isEmpty()) {
                            com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
                            String token = null;
                            if (obj.has("access_token")) {
                                token = obj.get("access_token").getAsString();
                            } else if (obj.has("token")) {
                                token = obj.get("token").getAsString();
                            }
                            if (token != null) {
                                com.async.app.util.SessionManager.getInstance(getApplication()).saveToken(token);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    User newUser = new User(fullName, email, password, role.toUpperCase());
                    newUser.setUsername(username);
                    newUser.setDepartment(department);
                    com.async.app.util.SessionManager.getInstance(getApplication()).saveUser(newUser);

                    signupSuccess.setValue(true);
                } else {
                    try {
                        String errorMsg = "Registration failed";
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
                        authError.setValue(errorMsg);
                    } catch (Exception e) {
                        authError.setValue("Registration failed");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                authError.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
