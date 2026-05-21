package com.async.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.async.app.model.Project;
import com.async.app.repository.MockDataRepository;

import java.util.List;

public class ProjectsViewModel extends ViewModel {
    private final MockDataRepository repository = MockDataRepository.getInstance();

    private final MutableLiveData<List<Project>> projectList = new MutableLiveData<>();

    public LiveData<List<Project>> getProjectList() {
        return projectList;
    }

    public void loadProjects() {
        projectList.setValue(repository.getProjects());
    }
}
