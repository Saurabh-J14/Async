package com.async.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.User;

public class LoginViewModel extends AndroidViewModel {

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> authCheckSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getUsernameError() {
        return usernameError;
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

    public LiveData<Boolean> getAuthCheckSuccess() {
        return authCheckSuccess;
    }

    public void login(String username, String password) {
        // Reset errors
        usernameError.setValue(null);
        passwordError.setValue(null);
        authError.setValue(null);

        boolean hasError = false;

        if (username == null || username.trim().isEmpty()) {
            usernameError.setValue("Username is required");
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

        com.async.app.network.model.LoginRequest loginRequest = new com.async.app.network.model.LoginRequest(username, password);

        com.async.app.network.ApiClient.getApiService().loginUser(loginRequest).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
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

                    User loggedInUser = parseUserFromResponse(json, username, password);

                    com.async.app.util.SessionManager.getInstance(getApplication()).saveUser(loggedInUser);

                    loginSuccess.setValue(true);
                } else {
                    try {
                        String errorMsg = "Invalid username or password";
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
                        authError.setValue("Invalid username or password");
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

    private User parseUserFromResponse(String json, String fallbackUsername, String fallbackPassword) {
        try {
            if (json != null && !json.trim().isEmpty()) {
                com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
                String name = obj.has("name") ? obj.get("name").getAsString() : "Async User";
                String email = obj.has("email") ? obj.get("email").getAsString() : fallbackUsername + "@async.com";
                String role = obj.has("role") ? obj.get("role").getAsString().toUpperCase() : "EMPLOYEE";
                String dept = obj.has("department") ? obj.get("department").getAsString() : "";

                User user = new User(name, email, fallbackPassword, role);
                user.setUsername(fallbackUsername);
                user.setDepartment(dept);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        User user = new User(fallbackUsername, fallbackUsername + "@async.com", fallbackPassword, "EMPLOYEE");
        user.setUsername(fallbackUsername);
        return user;
    }

    public void checkServerAuthentication(String username, String password) {
        isLoading.setValue(true);

        com.async.app.network.ApiClient.getApiService().isAuth(null).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    authCheckSuccess.setValue(true);
                } else {
                    authCheckSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                authCheckSuccess.setValue(false);
            }
        });
    }
}
