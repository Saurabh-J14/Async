package com.async.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.async.app.R;
import com.async.app.databinding.FragmentProjectsBinding;
import com.async.app.model.Project;
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
        setupFiltersAndActions();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            viewModel.loadProjects();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            viewModel.loadProjects();
        }
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter();
        binding.rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProjects.setAdapter(adapter);

        adapter.setOnProjectActionListener(new ProjectAdapter.OnProjectActionListener() {
            @Override
            public void onEditProject(Project project) {
                showEditProjectDialog(project);
            }

            @Override
            public void onDeleteProject(Project project) {
                showDeleteProjectDialog(project);
            }
        });
    }

    private void setupObservers() {
        viewModel.getProjectList().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                adapter.setProjects(projects);
            }
        });

        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteProjectDialog(Project project) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete the project \"" + project.getName() + "\"? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteProject(project.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditProjectDialog(Project project) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText etName = new EditText(getContext());
        etName.setHint("Project Name");
        etName.setText(project.getName());
        layout.addView(etName);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Project Description");
        etDesc.setText(project.getDescription());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
        etDesc.setLayoutParams(params);
        layout.addView(etDesc);

        final EditText etDept = new EditText(getContext());
        etDept.setHint("Department");
        etDept.setText(project.getCategory() != null ? project.getCategory() : "");
        etDept.setLayoutParams(params);
        layout.addView(etDept);

        new AlertDialog.Builder(requireContext())
            .setTitle("Update Project")
            .setView(layout)
            .setPositiveButton("Update", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                String dept = etDept.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.updateProject(project.getId(), name, desc, dept);
            })
            .setNegativeButton("Cancel", null)
            .show();
     }

    private void setupFiltersAndActions() {
        binding.btnFilterAll.setOnClickListener(v -> {
            binding.btnFilterAll.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.btnFilterAll.setTextColor(getResources().getColor(R.color.white));
            binding.btnFilterMine.setBackgroundResource(R.drawable.bg_segmented_inactive);
            binding.btnFilterMine.setTextColor(getResources().getColor(R.color.text_secondary));
            viewModel.loadProjects();
        });

        binding.btnFilterMine.setOnClickListener(v -> {
            binding.btnFilterMine.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.btnFilterMine.setTextColor(getResources().getColor(R.color.white));
            binding.btnFilterAll.setBackgroundResource(R.drawable.bg_segmented_inactive);
            binding.btnFilterAll.setTextColor(getResources().getColor(R.color.text_secondary));
            viewModel.loadUserProjects();
        });

        if (binding.fabAddProject != null) {
            binding.fabAddProject.setOnClickListener(v -> showCreateProjectDialog());
        }
    }

    private void showCreateProjectDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText etName = new EditText(getContext());
        etName.setHint("Project Name");
        etName.setBackgroundResource(R.drawable.bg_edittext);
        etName.setPadding(padding, padding, padding, padding);
        layout.addView(etName);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Project Description");
        etDesc.setBackgroundResource(R.drawable.bg_edittext);
        etDesc.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
        etDesc.setLayoutParams(params);
        layout.addView(etDesc);

        new AlertDialog.Builder(requireContext())
            .setTitle("Create New Project")
            .setView(layout)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Project name is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.createProject(name, desc);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
