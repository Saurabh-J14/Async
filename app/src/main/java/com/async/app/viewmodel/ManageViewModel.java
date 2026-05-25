package com.async.app.viewmodel;

import android.app.Application;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.network.ApiClient;
import com.async.app.util.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageViewModel extends AndroidViewModel {

    private final MutableLiveData<List<User>> employeeList = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectList = new MutableLiveData<>();
    private final MutableLiveData<String> operationStatus = new MutableLiveData<>();

    public ManageViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<User>> getEmployeeList() {
        return employeeList;
    }

    public LiveData<List<Task>> getTaskList() {
        return taskList;
    }

    public LiveData<List<Project>> getProjectList() {
        return projectList;
    }

    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    public void loadData() {
        ApiClient.getApiService().getEmployees().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> allUsers = response.body();
                    List<User> employees = new ArrayList<>();
                    for (User user : allUsers) {
                        if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
                            employees.add(user);
                        }
                    }
                    employeeList.setValue(employees);
                } else {
                    operationStatus.setValue("Failed to load employees: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                android.util.Log.e("ManageViewModel", "Error fetching employees: " + t.getMessage());
                operationStatus.setValue("Error fetching employees: " + t.getMessage());
            }
        });

        ApiClient.getApiService().getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.setValue(response.body());
                } else {
                    operationStatus.setValue("Failed to load tasks: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                android.util.Log.e("ManageViewModel", "Error fetching tasks: " + t.getMessage());
                operationStatus.setValue("Error fetching tasks: " + t.getMessage());
            }
        });

        ApiClient.getApiService().getProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(@NonNull Call<List<Project>> call, @NonNull Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.setValue(response.body());
                } else {
                    operationStatus.setValue("Failed to load projects: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
                android.util.Log.e("ManageViewModel", "Error fetching projects: " + t.getMessage());
                operationStatus.setValue("Error fetching projects: " + t.getMessage());
            }
        });
    }

    public void approveTask(String taskId) {
        // Approve task in local memory
        List<Task> currentTasks = taskList.getValue();
        if (currentTasks != null) {
            for (Task task : currentTasks) {
                if (task.getId().equals(taskId)) {
                    task.setStatus(Task.STATUS_DONE);
                    task.setCompleted(true);
                    break;
                }
            }
            taskList.setValue(currentTasks);
        }
    }

    public void assignTask(String title, String description, String priority, String category, String employeeEmail, String workspaceId, Runnable onSuccess) {
        try {
            String name = employeeEmail.split("@")[0];
            com.async.app.network.model.AssignedUser assignedUser = new com.async.app.network.model.AssignedUser(name, "employee", employeeEmail);

            String currentDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(new java.util.Date());

            com.async.app.network.model.CreateTaskRequest createTaskRequest = new com.async.app.network.model.CreateTaskRequest(
                title,
                description,
                0,
                0,
                "TODO",
                priority.toUpperCase(),
                currentDateStr,
                currentDateStr,
                false,
                currentDateStr,
                currentDateStr,
                assignedUser
            );

            ApiClient.getApiService().createTask(createTaskRequest).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                    if (response.isSuccessful()) {
                        android.util.Log.d("AsyncAPI", "Task created on server successfully!");
                        loadData(); // Refresh lists
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        android.util.Log.e("AsyncAPI", "Failed to create task on server: " + response.code());
                        operationStatus.setValue("Failed to assign task: Server error " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                    android.util.Log.e("AsyncAPI", "Network error creating task: " + t.getMessage());
                    operationStatus.setValue("Failed to assign task: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            operationStatus.setValue("Failed to assign task: " + e.getMessage());
        }
    }

    public void deleteUser(String userIdStr) {
        User user = SessionManager.getInstance(getApplication()).getUser();
        if (user == null) {
            operationStatus.setValue("Session expired. Please log in again.");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (Exception e) {
            operationStatus.setValue("Invalid User ID.");
            return;
        }

        String credentials = user.getUsername() + ":" + user.getPassword();
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        ApiClient.getApiService().deleteUser(authHeader, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationStatus.setValue("User deleted successfully");
                    loadData(); // Refresh list
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
                    operationStatus.setValue("Failed to delete user: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                operationStatus.setValue("Failed to delete user: " + t.getMessage());
            }
        });
    }

    public void updateEmployeeProfile(String userId, String name, String username, String password, String role, String department) {
        User loggedInUser = SessionManager.getInstance(getApplication()).getUser();
        if (loggedInUser == null) {
            operationStatus.setValue("Session expired. Please log in again.");
            return;
        }

        String credentials = loggedInUser.getUsername() + ":" + loggedInUser.getPassword();
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        okhttp3.RequestBody nameBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), name);
        okhttp3.RequestBody usernameBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), username);
        okhttp3.RequestBody passwordBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), password);
        okhttp3.RequestBody roleBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), role.toLowerCase());
        okhttp3.RequestBody departmentBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), department);

        ApiClient.getApiService().updateEmployeeProfile(authHeader, userId, nameBody, usernameBody, passwordBody, roleBody, departmentBody, null)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            operationStatus.setValue("Employee profile updated successfully!");
                            loadData(); // Refresh employee list
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
                            operationStatus.setValue("Failed to update employee: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        operationStatus.setValue("Failed to update employee: " + t.getMessage());
                    }
                });
    }
}
