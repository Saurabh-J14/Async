package com.async.app.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.async.app.R;
import com.async.app.databinding.FragmentHomeBinding;
import com.async.app.model.Task;
import com.async.app.viewmodel.HomeViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import com.async.app.view.adapters.ColleagueAdapter;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ColleagueAdapter colleagueAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupColleaguesRecyclerView();
        setupCharts();
        setupObservers();

        binding.cardHomeAvatar.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.async.app.view.activity.ProfileActivity.class);
            startActivity(intent);
        });

        binding.cardLeaderboard.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.async.app.view.activity.LeaderboardActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            viewModel.loadDashboardData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            viewModel.loadDashboardData();
        }
    }

    private void setupObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.txtGreeting.setText("Welcome back, " + user.getFullName() + "! 👋");
                binding.txtUserEmail.setText(user.getEmail());

                // Bind Profile Avatar Image
                if (user.getProfileImage() != null) {
                    try {
                        byte[] decodedBytes = android.util.Base64.decode(user.getProfileImage(), android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) {
                            binding.imgHomeAvatar.setImageBitmap(bitmap);
                        } else {
                            binding.imgHomeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                        }
                    } catch (Exception e) {
                        binding.imgHomeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                } else {
                    binding.imgHomeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }
        });

        viewModel.getTaskSummary().observe(getViewLifecycleOwner(), summary -> 
                binding.txtTaskCount.setText(summary));

        viewModel.getTasksProgress().observe(getViewLifecycleOwner(), progress -> 
                binding.progressTasks.setProgress(progress));

        viewModel.getProjectSummary().observe(getViewLifecycleOwner(), summary -> 
                binding.txtProjectCount.setText(summary));

        viewModel.getProjectsProgress().observe(getViewLifecycleOwner(), progress -> 
                binding.progressProjects.setProgress(progress));

        viewModel.getTasksList().observe(getViewLifecycleOwner(), this::updateCharts);

        viewModel.getDepartmentColleagues().observe(getViewLifecycleOwner(), colleagues -> {
            if (colleagues != null) {
                colleagueAdapter.setColleagues(colleagues);
            }
        });
    }

    private void setupCharts() {
        // Configure Pie Chart
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setExtraOffsets(5f, 10f, 5f, 5f);
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.WHITE);
        binding.pieChart.setTransparentCircleRadius(61f);
        binding.pieChart.setEntryLabelColor(Color.BLACK);
        binding.pieChart.setEntryLabelTextSize(12f);
        
        // Configure Bar Chart
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.getLegend().setEnabled(true);
    }

    private void updateCharts(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return;

        updatePieChart(tasks);
        updateBarChart(tasks);
    }

    private void updatePieChart(List<Task> tasks) {
        int completed = 0;
        int pending = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            } else {
                pending++;
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (completed > 0) {
            entries.add(new PieEntry(completed, "Completed"));
        }
        if (pending > 0) {
            entries.add(new PieEntry(pending, "In Progress"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Task Status");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.success_green));
        colors.add(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));

        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
        binding.pieChart.animateY(1000);
    }

    private void updateBarChart(List<Task> tasks) {
        int high = 0;
        int medium = 0;
        int low = 0;
        for (Task task : tasks) {
            String p = task.getPriority();
            if (p != null) {
                if (p.equalsIgnoreCase("HIGH")) high++;
                else if (p.equalsIgnoreCase("MEDIUM")) medium++;
                else if (p.equalsIgnoreCase("LOW")) low++;
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, high));
        entries.add(new BarEntry(1f, medium));
        entries.add(new BarEntry(2f, low));

        BarDataSet dataSet = new BarDataSet(entries, "Priority Level");
        
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.priority_high_txt));
        colors.add(ContextCompat.getColor(requireContext(), R.color.priority_medium_txt));
        colors.add(ContextCompat.getColor(requireContext(), R.color.priority_low_txt));
        dataSet.setColors(colors);

        BarData data = new BarData(dataSet);
        data.setValueTextSize(12f);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);

        // Customize X Axis to show LOW, MEDIUM, HIGH Labels
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) return "High";
                if (value == 1f) return "Medium";
                if (value == 2f) return "Low";
                return "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Customize Y Axis to only show integers
        YAxis yAxisLeft = binding.barChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setDrawGridLines(true);

        binding.barChart.invalidate();
        binding.barChart.animateY(1000);
    }

    private void setupColleaguesRecyclerView() {
        colleagueAdapter = new ColleagueAdapter();
        binding.rvColleagues.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvColleagues.setAdapter(colleagueAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
