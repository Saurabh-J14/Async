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

    private final List<Project> projects = new ArrayList<>();

    public void setProjects(List<Project> newProjects) {
        projects.clear();
        projects.addAll(newProjects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projects.get(position));
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

        public void bind(Project project) {
            binding.txtProjectName.setText(project.getName());
            binding.txtProjectDescription.setText(project.getDescription());
            binding.txtProjectCategory.setText(project.getCategory());
            
            // Set Progress
            binding.pbProjectProgress.setProgress(project.getProgress());
            binding.txtProjectProgressValue.setText(project.getProgress() + "%");
            
            // Set Members string
            binding.txtProjectMembers.setText(project.getMembersCount() + " team members active");
        }
    }
}
