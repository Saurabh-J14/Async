package com.async.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.network.ApiClient;
import com.async.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> taskSummary = new MutableLiveData<>("0 Active");
    private final MutableLiveData<String> projectSummary = new MutableLiveData<>("0 Active");
    private final MutableLiveData<Integer> tasksProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> projectsProgress = new MutableLiveData<>(0);
    private final MutableLiveData<List<Task>> tasksList = new MutableLiveData<>();
    private final MutableLiveData<List<User>> departmentColleagues = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<User>> getDepartmentColleagues() {
        return departmentColleagues;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<Task>> getTasksList() {
        return tasksList;
    }

    public LiveData<String> getTaskSummary() {
        return taskSummary;
    }

    public LiveData<String> getProjectSummary() {
        return projectSummary;
    }

    public LiveData<Integer> getTasksProgress() {
        return tasksProgress;
    }

    public LiveData<Integer> getProjectsProgress() {
        return projectsProgress;
    }

    public void loadDashboardData() {
        currentUser.setValue(SessionManager.getInstance(getApplication()).getUser());

        ApiClient.getApiService().getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body();
                    tasksList.setValue(tasks);
                    int activeTasksCount = 0;
                    int completedTasksCount = 0;
                    for (Task task : tasks) {
                        if (task.isCompleted()) {
                            completedTasksCount++;
                        } else {
                            activeTasksCount++;
                        }
                    }
                    taskSummary.setValue(activeTasksCount + " Active");
                    if (!tasks.isEmpty()) {
                        tasksProgress.setValue((completedTasksCount * 100) / tasks.size());
                    } else {
                        tasksProgress.setValue(0);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                android.util.Log.e("HomeViewModel", "Error fetching tasks: " + t.getMessage());
            }
        });

        ApiClient.getApiService().getProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Project> projects = response.body();
                    projectSummary.setValue(projects.size() + " Active");
                    
                    int totalProjectProgress = 0;
                    for (Project project : projects) {
                        totalProjectProgress += project.getProgress();
                    }
                    if (!projects.isEmpty()) {
                        projectsProgress.setValue(totalProjectProgress / projects.size());
                    } else {
                        projectsProgress.setValue(0);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                android.util.Log.e("HomeViewModel", "Error fetching projects: " + t.getMessage());
            }
        });
        loadDepartmentColleagues();
    }

    public void loadDepartmentColleagues() {
        ApiClient.getApiService().getDepartmentColleagues(null).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departmentColleagues.setValue(response.body());
                } else {
                    android.util.Log.e("HomeViewModel", "Failed to fetch department colleagues: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                android.util.Log.e("HomeViewModel", "Error fetching department colleagues: " + t.getMessage());
            }
        });
    }
}
