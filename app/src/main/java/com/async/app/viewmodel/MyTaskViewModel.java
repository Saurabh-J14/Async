package com.async.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.network.ApiClient;
import com.async.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTaskViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    private final MutableLiveData<String> operationStatus = new MutableLiveData<>();

    public MyTaskViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Task>> getTaskList() {
        return taskList;
    }

    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    public void loadTasks() {
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
                android.util.Log.e("MyTaskViewModel", "Error fetching tasks: " + t.getMessage());
                operationStatus.setValue("Error fetching tasks: " + t.getMessage());
            }
        });
    }

    public void toggleTaskCompletion(String taskId, boolean isCompleted) {
        // Toggle state in memory
        List<Task> currentTasks = taskList.getValue();
        if (currentTasks != null) {
            for (Task task : currentTasks) {
                if (task.getId().equals(taskId)) {
                    task.setCompleted(isCompleted);
                    task.setStatus(isCompleted ? Task.STATUS_DONE : Task.STATUS_TODO);
                    break;
                }
            }
            taskList.setValue(currentTasks);
        }
    }

    public void addTask(String title, String description, String priority, String category, String dueDate) {
        try {
            User activeUser = SessionManager.getInstance(getApplication()).getUser();
            String userName = (activeUser != null) ? activeUser.getUsername() : "employee123";

            com.async.app.network.model.AssignedUser assignedUser = new com.async.app.network.model.AssignedUser(userName, "employee", userName);

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
                        operationStatus.setValue("Task created successfully!");
                        loadTasks(); // refresh task list from API
                    } else {
                        android.util.Log.e("AsyncAPI", "Failed to create task on server: " + response.code());
                        operationStatus.setValue("Failed to create task: Server error " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                    android.util.Log.e("AsyncAPI", "Network error creating task: " + t.getMessage());
                    operationStatus.setValue("Failed to create task: Network error " + t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            operationStatus.setValue("Failed to create task: " + e.getMessage());
        }
    }
}
