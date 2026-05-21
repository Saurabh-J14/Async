package com.async.app.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.async.app.R;
import com.async.app.databinding.FragmentMyTaskBinding;
import com.async.app.view.adapters.TaskAdapter;
import com.async.app.viewmodel.MyTaskViewModel;

public class MyTaskFragment extends Fragment {

    private FragmentMyTaskBinding binding;
    private MyTaskViewModel viewModel;
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyTaskViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.loadTasks();
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter((taskId, isCompleted) -> viewModel.toggleTaskCompletion(taskId, isCompleted));
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTasks.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks == null || tasks.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvTasks.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvTasks.setVisibility(View.VISIBLE);
                adapter.setTasks(tasks);
            }
        });
    }

    private void setupListeners() {
        binding.fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Task");

        // Dynamic view creation for custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.activity_signup, null);
        // We'll design a simpler programmatic or customized view to prevent xml duplication
        // Create an elegant custom layout programmatic view
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Task Title");
        etTitle.setPadding(16, 24, 16, 24);
        layout.addView(etTitle);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Description");
        etDesc.setPadding(16, 24, 16, 24);
        layout.addView(etDesc);

        final EditText etCategory = new EditText(getContext());
        etCategory.setHint("Category (e.g. Design, Dev)");
        etCategory.setPadding(16, 24, 16, 24);
        layout.addView(etCategory);

        // Spinner for priority
        final Spinner spinnerPriority = new Spinner(getContext());
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(spinnerAdapter);
        spinnerPriority.setPadding(16, 24, 16, 24);
        layout.addView(spinnerPriority);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priority = spinnerPriority.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addTask(title, desc, priority, category, "Due Today");
                Toast.makeText(getContext(), "Task Added!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
