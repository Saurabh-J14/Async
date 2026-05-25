package com.async.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.async.app.databinding.FragmentManageBinding;
import com.async.app.databinding.DialogEditEmployeeBinding;
import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.viewmodel.ManageViewModel;
import com.async.app.view.adapters.EmployeeAdapter;
import com.async.app.util.OnEmployeeActionListener;

import java.util.ArrayList;
import java.util.List;

public class ManageFragment extends Fragment {

    private FragmentManageBinding binding;
    private ManageViewModel viewModel;
    private EmployeeAdapter adapter;

    private final List<Project> projectsCache = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBinding.inflate(inflater, container, false);
        binding.rvManageEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        projectsCache.clear();

        viewModel = new ViewModelProvider(this).get(ManageViewModel.class);

        adapter = new EmployeeAdapter(new OnEmployeeActionListener() {
            @Override
            public void onDeleteEmployee(User employee) {
                showDeleteEmployeeDialog(employee);
            }

            @Override
            public void onEditEmployee(User employee) {
                showEditEmployeeDialog(employee);
            }

            @Override
            public void onApproveTask(Task task) {
                viewModel.approveTask(task.getId());
                Toast.makeText(getContext(), "Approved: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public String getWorkspaceName(String workspaceId) {
                return ManageFragment.this.getWorkspaceName(workspaceId);
            }
        });
        binding.rvManageEmployees.setAdapter(adapter);

        if (binding.fabAssignTask != null) {
            binding.fabAssignTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAssignTaskDialog();
                }
            });
        }

        viewModel.getEmployeeList().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                adapter.setEmployees(users);
            }
        });

        viewModel.getTaskList().observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                adapter.setTasks(tasks);
            }
        });

        viewModel.getProjectList().observe(getViewLifecycleOwner(), new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                projectsCache.clear();
                if (projects != null) {
                    projectsCache.addAll(projects);
                }
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String status) {
                if (status != null && !status.isEmpty()) {
                    Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            viewModel.loadData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            viewModel.loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getWorkspaceName(String id) {
        for (Project proj : projectsCache) {
            if (proj.getId().equalsIgnoreCase(id)) {
                return proj.getName();
            }
        }
        return "Workspace " + id;
    }



    private void showAssignTaskDialog() {
        List<User> employeeSource = viewModel.getEmployeeList().getValue();
        List<Project> projectSource = viewModel.getProjectList().getValue();

        if (employeeSource == null || employeeSource.isEmpty() || projectSource == null || projectSource.isEmpty()) {
            Toast.makeText(getContext(), "Loading employee and project data. Please try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Assign New Task to Team");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Task Title");
        layout.addView(etTitle);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Task Description");
        layout.addView(etDesc);

        final EditText etCategory = new EditText(getContext());
        etCategory.setHint("Category (e.g. IT, Design, Engineering)");
        layout.addView(etCategory);

        final TextView tvPriority = new TextView(getContext());
        tvPriority.setText("Priority:");
        tvPriority.setPadding(0, 16, 0, 8);
        layout.addView(tvPriority);
        
        final Spinner spinnerPriority = new Spinner(getContext());
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        layout.addView(spinnerPriority);

        final TextView tvAssignee = new TextView(getContext());
        tvAssignee.setText("Assign To Employee:");
        tvAssignee.setPadding(0, 16, 0, 8);
        layout.addView(tvAssignee);

        final Spinner spinnerAssignee = new Spinner(getContext());
        final List<User> employees = new ArrayList<>();
        List<String> employeeNames = new ArrayList<>();
        for (User u : employeeSource) {
            if (!"MANAGER".equalsIgnoreCase(u.getRole())) {
                employees.add(u);
                employeeNames.add(u.getFullName() + " (" + u.getEmail() + ")");
            }
        }
        
        if (employees.isEmpty()) {
            Toast.makeText(getContext(), "No employees available to assign tasks.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, employeeNames);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignee.setAdapter(employeeAdapter);
        layout.addView(spinnerAssignee);

        final TextView tvWorkspace = new TextView(getContext());
        tvWorkspace.setText("Select Workspace:");
        tvWorkspace.setPadding(0, 16, 0, 8);
        layout.addView(tvWorkspace);

        final Spinner spinnerWorkspace = new Spinner(getContext());
        final List<Project> workspaces = new ArrayList<>(projectSource);
        List<String> workspaceNames = new ArrayList<>();
        for (Project p : workspaces) {
            workspaceNames.add(p.getName());
        }
        ArrayAdapter<String> workspaceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, workspaceNames);
        workspaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkspace.setAdapter(workspaceAdapter);
        layout.addView(spinnerWorkspace);

        builder.setView(layout);

        builder.setPositiveButton("Assign", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priority = spinnerPriority.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                User selectedEmployee = employees.get(spinnerAssignee.getSelectedItemPosition());
                Project selectedWorkspace = workspaces.get(spinnerWorkspace.getSelectedItemPosition());

                viewModel.assignTask(
                    title,
                    desc,
                    priority,
                    category,
                    selectedEmployee.getEmail(),
                    selectedWorkspace.getId(),
                    () -> Toast.makeText(getContext(), "Task assigned successfully!", Toast.LENGTH_SHORT).show()
                );
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteEmployeeDialog(final User employee) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Employee")
            .setMessage("Are you sure you want to delete employee " + employee.getFullName() + "? This action cannot be undone.")
            .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    viewModel.deleteUser(employee.getId());
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditEmployeeDialog(final User employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogEditEmployeeBinding dialogBinding = DialogEditEmployeeBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());

        // Populate existing details
        dialogBinding.etEditEmployeeName.setText(employee.getFullName());
        dialogBinding.etEditEmployeeUsername.setText(employee.getUsername());
        dialogBinding.etEditEmployeePassword.setText(""); // Keep empty initially
        dialogBinding.etEditEmployeeDepartment.setText(employee.getDepartment());

        // Set up the roles spinner
        String[] roles = {"EMPLOYEE", "MANAGER"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerEditEmployeeRole.setAdapter(roleAdapter);

        // Pre-select employee's current role
        if (employee.getRole() != null) {
            if ("MANAGER".equalsIgnoreCase(employee.getRole())) {
                dialogBinding.spinnerEditEmployeeRole.setSelection(1);
            } else {
                dialogBinding.spinnerEditEmployeeRole.setSelection(0);
            }
        }

        builder.setPositiveButton("Save", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String name = dialogBinding.etEditEmployeeName.getText().toString().trim();
                String username = dialogBinding.etEditEmployeeUsername.getText().toString().trim();
                String password = dialogBinding.etEditEmployeePassword.getText().toString().trim();
                String department = dialogBinding.etEditEmployeeDepartment.getText().toString().trim();
                String role = dialogBinding.spinnerEditEmployeeRole.getSelectedItem().toString();

                if (name.isEmpty() || username.isEmpty() || department.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If password is empty, we keep the original password
                String finalPassword = password.isEmpty() ? employee.getPassword() : password;

                viewModel.updateEmployeeProfile(employee.getId(), name, username, finalPassword, role, department);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
