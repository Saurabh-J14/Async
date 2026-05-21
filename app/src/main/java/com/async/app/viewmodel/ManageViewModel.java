package com.async.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.repository.MockDataRepository;

import java.util.ArrayList;
import java.util.List;

public class ManageViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<List<User>> employeeList = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();

    public LiveData<List<User>> getEmployeeList() {
        return employeeList;
    }

    public LiveData<List<Task>> getTaskList() {
        return taskList;
    }

    public void loadData() {
        // Load employees
        List<User> allUsers = repository.getUsers();
        List<User> employees = new ArrayList<>();
        for (User user : allUsers) {
            if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
                employees.add(user);
            }
        }
        employeeList.setValue(employees);

        // Load tasks
        taskList.setValue(repository.getTasks());
    }

    public void approveTask(String taskId) {
        repository.approveTask(taskId);
        loadData(); // refresh UI lists
    }
}
