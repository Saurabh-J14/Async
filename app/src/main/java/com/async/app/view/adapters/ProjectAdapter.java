package com.async.app.view.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.databinding.ItemProjectBinding;
import com.async.app.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onEditProject(Project project);
        void onDeleteProject(Project project);
    }

    private final List<Project> projects = new ArrayList<>();
    private OnProjectActionListener actionListener;

    public void setProjects(List<Project> newProjects) {
        projects.clear();
        projects.addAll(newProjects);
        notifyDataSetChanged();
    }

    public void setOnProjectActionListener(OnProjectActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projects.get(position), actionListener);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemProjectBinding binding;

        public ProjectViewHolder(@NonNull ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Project project, OnProjectActionListener listener) {
            binding.txtProjectName.setText(project.getName());
            binding.txtProjectDescription.setText(project.getDescription());
            binding.txtProjectCategory.setText(project.getCategory());
            
            // Set Progress
            binding.pbProjectProgress.setProgress(project.getProgress());
            binding.txtProjectProgressValue.setText(project.getProgress() + "%");
            
            // Set Members string
            binding.txtProjectMembers.setText(project.getMembersCount() + " team members active");

            binding.btnEditProject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditProject(project);
                }
            });

            binding.btnDeleteProject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProject(project);
                }
            });
        }
    }
}
