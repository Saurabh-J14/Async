package com.async.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.async.app.databinding.FragmentProjectsBinding;
import com.async.app.view.adapters.ProjectAdapter;
import com.async.app.viewmodel.ProjectsViewModel;

public class ProjectsFragment extends Fragment {

    private FragmentProjectsBinding binding;
    private ProjectsViewModel viewModel;
    private ProjectAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);

        setupRecyclerView();
        setupObservers();

        viewModel.loadProjects();
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter();
        binding.rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProjects.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getProjectList().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                adapter.setProjects(projects);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
