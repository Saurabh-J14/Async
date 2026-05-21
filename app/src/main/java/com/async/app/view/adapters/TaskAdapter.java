package com.async.app.view.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.R;
import com.async.app.databinding.ItemTaskBinding;
import com.async.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskStatusChangeListener listener;

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(String taskId, boolean isCompleted);
    }

    public TaskAdapter(OnTaskStatusChangeListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        public TaskViewHolder(@NonNull ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task) {
            binding.txtTaskTitle.setText(task.getTitle());
            binding.txtTaskDescription.setText(task.getDescription());
            binding.txtTaskCategory.setText(task.getCategory());
            binding.txtTaskDueDate.setText(task.getDueDate());
            
            // Set priority styling
            String priority = task.getPriority().toUpperCase();
            binding.txtTaskPriority.setText(priority);
            
            if (priority.equals("HIGH")) {
                binding.txtTaskPriority.setBackgroundResource(R.drawable.bg_badge_high);
                binding.txtTaskPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high_txt));
            } else if (priority.equals("MEDIUM")) {
                binding.txtTaskPriority.setBackgroundResource(R.drawable.bg_badge_medium);
                binding.txtTaskPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium_txt));
            } else {
                binding.txtTaskPriority.setBackgroundResource(R.drawable.bg_badge_low);
                binding.txtTaskPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low_txt));
            }

            // Set checkbox state (temporarily clear listener to prevent loop)
            binding.cbTaskStatus.setOnCheckedChangeListener(null);
            binding.cbTaskStatus.setChecked(task.isCompleted());
            
            // Strike-through title if completed
            if (task.isCompleted()) {
                binding.txtTaskTitle.setPaintFlags(binding.txtTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.txtTaskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            } else {
                binding.txtTaskTitle.setPaintFlags(binding.txtTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.txtTaskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }

            // Bind dates (Calendar times)
            String assigned = task.getAssignedDate() != null ? task.getAssignedDate() : "May 20, 2026";
            String completed = task.getCompletedDate() != null ? task.getCompletedDate() : "Pending";
            binding.txtTaskCalendarDates.setText("Assigned: " + assigned + " | Completed: " + completed);

            // React to user clicking the checkbox
            binding.cbTaskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStatusChanged(task.getId(), isChecked);
                }
            });
        }
    }
}
