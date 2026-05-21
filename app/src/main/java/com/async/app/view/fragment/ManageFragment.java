package com.async.app.view.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.async.app.R;
import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.repository.MockDataRepository;
import com.async.app.viewmodel.ManageViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageFragment extends Fragment {

    private ManageViewModel viewModel;
    private RecyclerView rvEmployees;
    private EmployeeAdapter adapter;

    // Cache projects to look up names
    private final List<Project> projectsCache = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        rvEmployees = view.findViewById(R.id.rv_manage_employees);
        rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        projectsCache.clear();
        projectsCache.addAll(MockDataRepository.getInstance().getProjects());

        viewModel = new ViewModelProvider(this).get(ManageViewModel.class);

        adapter = new EmployeeAdapter();
        rvEmployees.setAdapter(adapter);

        FloatingActionButton fabAssignTask = view.findViewById(R.id.fab_assign_task);
        if (fabAssignTask != null) {
            fabAssignTask.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadData();
    }

    private String getWorkspaceName(String id) {
        for (Project proj : projectsCache) {
            if (proj.getId().equalsIgnoreCase(id)) {
                return proj.getName();
            }
        }
        return "Workspace " + id;
    }

    private class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

        private List<User> employees = new ArrayList<>();
        private List<Task> allTasks = new ArrayList<>();
        private final Set<String> expandedEmails = new HashSet<>();

        public void setEmployees(List<User> employees) {
            this.employees = employees;
            notifyDataSetChanged();
        }

        public void setTasks(List<Task> tasks) {
            this.allTasks = tasks;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_employee, parent, false);
            return new EmployeeViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
            final User employee = employees.get(position);
            holder.bind(employee, allTasks, expandedEmails.contains(employee.getEmail()));
        }

        @Override
        public int getItemCount() {
            return employees.size();
        }

        class EmployeeViewHolder extends RecyclerView.ViewHolder {
            ImageView imgAvatar;
            TextView txtName, txtEmail, txtWorkload, txtReviewCount;
            View separator;
            LinearLayout layoutTasksContainer, containerTasks, layoutHeader;

            public EmployeeViewHolder(@NonNull View itemView) {
                super(itemView);
                imgAvatar = itemView.findViewById(R.id.img_employee_avatar);
                txtName = itemView.findViewById(R.id.txt_employee_name);
                txtEmail = itemView.findViewById(R.id.txt_employee_email);
                txtWorkload = itemView.findViewById(R.id.txt_workload_summary);
                txtReviewCount = itemView.findViewById(R.id.txt_review_count);
                separator = itemView.findViewById(R.id.separator_line);
                layoutTasksContainer = itemView.findViewById(R.id.layout_tasks_container);
                containerTasks = itemView.findViewById(R.id.container_tasks);
                layoutHeader = itemView.findViewById(R.id.layout_employee_header);
            }

            public void bind(final User employee, List<Task> tasks, final boolean isExpanded) {
                txtName.setText(employee.getFullName());
                txtEmail.setText(employee.getEmail());

                if (employee.getProfileImage() != null) {
                    try {
                        byte[] decodedBytes = Base64.decode(employee.getProfileImage(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) {
                            imgAvatar.setImageBitmap(bitmap);
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                        }
                    } catch (Exception e) {
                        imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }

                final List<Task> employeeTasks = new ArrayList<>();
                int reviewTasksCount = 0;
                for (Task t : tasks) {
                    if (employee.getEmail().equalsIgnoreCase(t.getAssignedToEmail())) {
                        employeeTasks.add(t);
                        if (Task.STATUS_REVIEW.equalsIgnoreCase(t.getStatus())) {
                            reviewTasksCount++;
                        }
                    }
                }

                txtWorkload.setText(employeeTasks.size() + " Tasks");

                if (reviewTasksCount > 0) {
                    txtReviewCount.setVisibility(View.VISIBLE);
                    txtReviewCount.setText(reviewTasksCount + " REVIEW");
                } else {
                    txtReviewCount.setVisibility(View.GONE);
                }

                layoutHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExpanded) {
                            expandedEmails.remove(employee.getEmail());
                        } else {
                            expandedEmails.add(employee.getEmail());
                        }
                        notifyItemChanged(getAdapterPosition());
                    }
                });

                containerTasks.removeAllViews();
                if (isExpanded) {
                    separator.setVisibility(View.VISIBLE);
                    layoutTasksContainer.setVisibility(View.VISIBLE);

                    if (employeeTasks.isEmpty()) {
                        TextView emptyText = new TextView(itemView.getContext());
                        emptyText.setText("No tasks assigned to this employee.");
                        emptyText.setTextColor(itemView.getResources().getColor(R.color.text_secondary));
                        emptyText.setTextSize(13);
                        emptyText.setPadding(0, 8, 0, 8);
                        containerTasks.addView(emptyText);
                    } else {
                        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                        for (final Task task : employeeTasks) {
                            View row = inflater.inflate(R.layout.item_manage_task, containerTasks, false);
                            
                            TextView title = row.findViewById(R.id.txt_manage_task_title);
                            TextView priority = row.findViewById(R.id.txt_manage_task_priority);
                            TextView workspace = row.findViewById(R.id.txt_manage_task_workspace);
                            TextView status = row.findViewById(R.id.txt_manage_task_status);
                            Button approveBtn = row.findViewById(R.id.btn_manage_task_approve);

                            title.setText(task.getTitle());
                            priority.setText(task.getPriority().toUpperCase());
                            workspace.setText(getWorkspaceName(task.getWorkspaceId()));

                            TextView dates = row.findViewById(R.id.txt_manage_task_dates);
                            String assigned = task.getAssignedDate() != null ? task.getAssignedDate() : "May 20, 2026";
                            String completed = task.getCompletedDate() != null ? task.getCompletedDate() : "Pending";
                            dates.setText("Assigned: " + assigned + " | Completed: " + completed);

                            if ("HIGH".equalsIgnoreCase(task.getPriority())) {
                                priority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_high_bg));
                                priority.setTextColor(itemView.getResources().getColor(R.color.priority_high_txt));
                            } else if ("MEDIUM".equalsIgnoreCase(task.getPriority())) {
                                priority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_medium_bg));
                                priority.setTextColor(itemView.getResources().getColor(R.color.priority_medium_txt));
                            } else {
                                priority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_low_bg));
                                priority.setTextColor(itemView.getResources().getColor(R.color.priority_low_txt));
                            }

                            String taskStatus = task.getStatus();
                            status.setText(taskStatus);
                            if (Task.STATUS_REVIEW.equalsIgnoreCase(taskStatus)) {
                                status.setBackgroundColor(itemView.getResources().getColor(R.color.error_red));
                                status.setTextColor(itemView.getResources().getColor(R.color.white));
                                approveBtn.setVisibility(View.VISIBLE);
                                approveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.approveTask(task.getId());
                                        Toast.makeText(itemView.getContext(), "Approved: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (Task.STATUS_DONE.equalsIgnoreCase(taskStatus)) {
                                status.setBackgroundColor(itemView.getResources().getColor(R.color.success_green));
                                status.setTextColor(itemView.getResources().getColor(R.color.white));
                                approveBtn.setVisibility(View.GONE);
                            } else {
                                // TODO
                                status.setBackgroundColor(itemView.getResources().getColor(R.color.border_color));
                                status.setTextColor(itemView.getResources().getColor(R.color.text_primary));
                                approveBtn.setVisibility(View.GONE);
                            }

                            containerTasks.addView(row);
                        }
                    }
                } else {
                    separator.setVisibility(View.GONE);
                    layoutTasksContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showAssignTaskDialog() {
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

        // Spinner for priority
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

        // Spinner for assignee
        final TextView tvAssignee = new TextView(getContext());
        tvAssignee.setText("Assign To Employee:");
        tvAssignee.setPadding(0, 16, 0, 8);
        layout.addView(tvAssignee);

        final Spinner spinnerAssignee = new Spinner(getContext());
        final List<User> employees = new ArrayList<>();
        List<String> employeeNames = new ArrayList<>();
        for (User u : MockDataRepository.getInstance().getUsers()) {
            if (!"MANAGER".equalsIgnoreCase(u.getRole())) {
                employees.add(u);
                employeeNames.add(u.getFullName() + " (" + u.getEmail() + ")");
            }
        }
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, employeeNames);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignee.setAdapter(employeeAdapter);
        layout.addView(spinnerAssignee);

        // Spinner for workspace
        final TextView tvWorkspace = new TextView(getContext());
        tvWorkspace.setText("Select Workspace:");
        tvWorkspace.setPadding(0, 16, 0, 8);
        layout.addView(tvWorkspace);

        final Spinner spinnerWorkspace = new Spinner(getContext());
        final List<Project> workspaces = MockDataRepository.getInstance().getProjects();
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

            if (title.isEmpty() || desc.isEmpty() || category.isEmpty() || employees.isEmpty() || workspaces.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields and ensure employees exist.", Toast.LENGTH_SHORT).show();
            } else {
                User selectedEmployee = employees.get(spinnerAssignee.getSelectedItemPosition());
                Project selectedWorkspace = workspaces.get(spinnerWorkspace.getSelectedItemPosition());

                MockDataRepository.getInstance().addTask(
                    title,
                    desc,
                    priority,
                    category,
                    "Due in 3 days",
                    selectedEmployee.getEmail(),
                    selectedWorkspace.getId()
                );
                
                Toast.makeText(getContext(), "Task assigned successfully!", Toast.LENGTH_SHORT).show();
                viewModel.loadData(); // reload datasets
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
