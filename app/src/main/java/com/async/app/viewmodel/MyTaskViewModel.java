package com.async.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.model.Task;
import com.async.app.repository.MockDataRepository;

import java.util.List;

public class MyTaskViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();

    public LiveData<List<Task>> getTaskList() {
        return taskList;
    }

    public void loadTasks() {
        taskList.setValue(repository.getTasks());
    }

    public void toggleTaskCompletion(String taskId, boolean isCompleted) {
        repository.updateTaskCompletion(taskId, isCompleted);
        // Reload tasks list
        loadTasks();
    }

    public void addTask(String title, String description, String priority, String category, String dueDate) {
        repository.addTask(title, description, priority, category, dueDate);
        // Reload tasks list
        loadTasks();
    }
}
