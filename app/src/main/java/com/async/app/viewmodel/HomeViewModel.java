package com.async.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.repository.MockDataRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> taskSummary = new MutableLiveData<>();
    private final MutableLiveData<String> projectSummary = new MutableLiveData<>();
    private final MutableLiveData<Integer> tasksProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> projectsProgress = new MutableLiveData<>(0);
    private final MutableLiveData<List<Task>> tasksList = new MutableLiveData<>();

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
        currentUser.setValue(repository.getCurrentUser());

        List<Task> tasks = repository.getTasks();
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
        }

        List<Project> projects = repository.getProjects();
        projectSummary.setValue(projects.size() + " Active");
        
        int totalProjectProgress = 0;
        for (Project project : projects) {
            totalProjectProgress += project.getProgress();
        }
        if (!projects.isEmpty()) {
            projectsProgress.setValue(totalProjectProgress / projects.size());
        }
    }
}
