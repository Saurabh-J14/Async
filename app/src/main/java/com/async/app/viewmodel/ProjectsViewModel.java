package com.async.app.viewmodel;

import android.app.Application;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.Project;
import com.async.app.model.User;
import com.async.app.network.ApiClient;
import com.async.app.util.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Project>> projectList = new MutableLiveData<>();
    private final MutableLiveData<String> operationStatus = new MutableLiveData<>();

    public ProjectsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Project>> getProjectList() {
        return projectList;
    }

    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    public void loadProjects() {
        ApiClient.getApiService().getProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(@NonNull Call<List<Project>> call, @NonNull Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.setValue(response.body());
                } else {
                    android.util.Log.e("ProjectsViewModel", "Failed to load projects: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
                android.util.Log.e("ProjectsViewModel", "Error fetching projects: " + t.getMessage());
            }
        });
    }

    public void deleteProject(String projectIdStr) {
        User user = SessionManager.getInstance(getApplication()).getUser();
        if (user == null) {
            operationStatus.setValue("Session expired. Please log in again.");
            return;
        }

        int projectId;
        try {
            projectId = Integer.parseInt(projectIdStr);
        } catch (Exception e) {
            operationStatus.setValue("Invalid Project ID.");
            return;
        }

        String credentials = user.getUsername() + ":" + user.getPassword();
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        ApiClient.getApiService().deleteProject(authHeader, projectId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationStatus.setValue("Project deleted successfully");
                    loadProjects(); // Refresh list
                } else {
                    String errorMsg = "Deletion failed";
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            if (err.contains("\"detail\"")) {
                                JsonObject obj = new JsonParser().parse(err).getAsJsonObject();
                                errorMsg = obj.get("detail").getAsString();
                            } else {
                                errorMsg = err;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    operationStatus.setValue("Failed to delete project: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                operationStatus.setValue("Failed to delete project: " + t.getMessage());
            }
        });
    }

    public void updateProject(String projectIdStr, String name, String description, String department) {
        User user = SessionManager.getInstance(getApplication()).getUser();
        if (user == null) {
            operationStatus.setValue("Session expired. Please log in again.");
            return;
        }

        int projectId;
        try {
            projectId = Integer.parseInt(projectIdStr);
        } catch (Exception e) {
            operationStatus.setValue("Invalid Project ID.");
            return;
        }

        String credentials = user.getUsername() + ":" + user.getPassword();
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        com.async.app.network.model.UpdateProjectRequest updateRequest = new com.async.app.network.model.UpdateProjectRequest(name, description, department);

        ApiClient.getApiService().updateProject(authHeader, projectId, updateRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationStatus.setValue("Project updated successfully");
                    loadProjects(); // Refresh list
                } else {
                    String errorMsg = "Update failed";
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            if (err.contains("\"detail\"")) {
                                JsonObject obj = new JsonParser().parse(err).getAsJsonObject();
                                errorMsg = obj.get("detail").getAsString();
                            } else {
                                errorMsg = err;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    operationStatus.setValue("Failed to update project: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                operationStatus.setValue("Failed to update project: " + t.getMessage());
            }
        });
    }

    public void createProject(String name, String description) {
        com.async.app.network.model.CreateProjectRequest request = new com.async.app.network.model.CreateProjectRequest(name, description);
        ApiClient.getApiService().createProject(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationStatus.setValue("Project created successfully");
                    loadProjects(); // Refresh all projects list
                } else {
                    String errorMsg = "Creation failed";
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            if (err.contains("\"detail\"")) {
                                JsonObject obj = new JsonParser().parse(err).getAsJsonObject();
                                errorMsg = obj.get("detail").getAsString();
                            } else {
                                errorMsg = err;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    operationStatus.setValue("Failed to create project: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                operationStatus.setValue("Failed to create project: " + t.getMessage());
            }
        });
    }

    public void loadUserProjects() {
        ApiClient.getApiService().getUserProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(@NonNull Call<List<Project>> call, @NonNull Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.setValue(response.body());
                } else {
                    android.util.Log.e("ProjectsViewModel", "Failed to load user projects: " + response.code());
                    operationStatus.setValue("Failed to load user projects: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
                android.util.Log.e("ProjectsViewModel", "Error fetching user projects: " + t.getMessage());
                operationStatus.setValue("Error fetching user projects: " + t.getMessage());
            }
        });
    }
}
