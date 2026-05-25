package com.async.app.view.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.async.app.R;
import com.async.app.databinding.ItemManageEmployeeBinding;
import com.async.app.databinding.ItemManageTaskBinding;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.util.OnEmployeeActionListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private List<User> employees = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private final Set<String> expandedEmails = new HashSet<>();
    private final OnEmployeeActionListener actionListener;

    public EmployeeAdapter(OnEmployeeActionListener actionListener) {
        this.actionListener = actionListener;
    }

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
        ItemManageEmployeeBinding itemBinding = ItemManageEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EmployeeViewHolder(itemBinding);
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

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final ItemManageEmployeeBinding itemBinding;

        public EmployeeViewHolder(@NonNull ItemManageEmployeeBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        public void bind(final User employee, List<Task> tasks, final boolean isExpanded) {
            itemBinding.txtEmployeeName.setText(employee.getFullName());
            itemBinding.txtEmployeeEmail.setText(employee.getEmail());

            itemBinding.btnDeleteEmployee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onDeleteEmployee(employee);
                    }
                }
            });

            itemBinding.btnEditEmployee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onEditEmployee(employee);
                    }
                }
            });

            if (employee.getProfileImage() != null) {
                try {
                    byte[] decodedBytes = Base64.decode(employee.getProfileImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        itemBinding.imgEmployeeAvatar.setImageBitmap(bitmap);
                    } else {
                        itemBinding.imgEmployeeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                } catch (Exception e) {
                    itemBinding.imgEmployeeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                itemBinding.imgEmployeeAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
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

            itemBinding.txtWorkloadSummary.setText(employeeTasks.size() + " Tasks");

            if (reviewTasksCount > 0) {
                itemBinding.txtReviewCount.setVisibility(View.VISIBLE);
                itemBinding.txtReviewCount.setText(reviewTasksCount + " REVIEW");
            } else {
                itemBinding.txtReviewCount.setVisibility(View.GONE);
            }

            itemBinding.layoutEmployeeHeader.setOnClickListener(new View.OnClickListener() {
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

            itemBinding.containerTasks.removeAllViews();
            if (isExpanded) {
                itemBinding.separatorLine.setVisibility(View.VISIBLE);
                itemBinding.layoutTasksContainer.setVisibility(View.VISIBLE);

                if (employeeTasks.isEmpty()) {
                    TextView emptyText = new TextView(itemView.getContext());
                    emptyText.setText("No tasks assigned to this employee.");
                    emptyText.setTextColor(itemView.getResources().getColor(R.color.text_secondary));
                    emptyText.setTextSize(13);
                    emptyText.setPadding(0, 8, 0, 8);
                    itemBinding.containerTasks.addView(emptyText);
                } else {
                    LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                    for (final Task task : employeeTasks) {
                        ItemManageTaskBinding rowBinding = ItemManageTaskBinding.inflate(inflater, itemBinding.containerTasks, false);

                        rowBinding.txtManageTaskTitle.setText(task.getTitle());
                        rowBinding.txtManageTaskPriority.setText(task.getPriority().toUpperCase());
                        rowBinding.txtManageTaskWorkspace.setText(actionListener != null ? actionListener.getWorkspaceName(task.getWorkspaceId()) : "");

                        String assigned = task.getAssignedDate() != null ? task.getAssignedDate() : "May 20, 2026";
                        String completed = task.getCompletedDate() != null ? task.getCompletedDate() : "Pending";
                        rowBinding.txtManageTaskDates.setText("Assigned: " + assigned + " | Completed: " + completed);

                        if ("HIGH".equalsIgnoreCase(task.getPriority())) {
                            rowBinding.txtManageTaskPriority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_high_bg));
                            rowBinding.txtManageTaskPriority.setTextColor(itemView.getResources().getColor(R.color.priority_high_txt));
                        } else if ("MEDIUM".equalsIgnoreCase(task.getPriority())) {
                            rowBinding.txtManageTaskPriority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_medium_bg));
                            rowBinding.txtManageTaskPriority.setTextColor(itemView.getResources().getColor(R.color.priority_medium_txt));
                        } else {
                            rowBinding.txtManageTaskPriority.setBackgroundColor(itemView.getResources().getColor(R.color.priority_low_bg));
                            rowBinding.txtManageTaskPriority.setTextColor(itemView.getResources().getColor(R.color.priority_low_txt));
                        }

                        String taskStatus = task.getStatus();
                        rowBinding.txtManageTaskStatus.setText(taskStatus);
                        if (Task.STATUS_REVIEW.equalsIgnoreCase(taskStatus)) {
                            rowBinding.txtManageTaskStatus.setBackgroundColor(itemView.getResources().getColor(R.color.error_red));
                            rowBinding.txtManageTaskStatus.setTextColor(itemView.getResources().getColor(R.color.white));
                            rowBinding.btnManageTaskApprove.setVisibility(View.VISIBLE);
                            rowBinding.btnManageTaskApprove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (actionListener != null) {
                                        actionListener.onApproveTask(task);
                                    }
                                }
                            });
                        } else if (Task.STATUS_DONE.equalsIgnoreCase(taskStatus)) {
                            rowBinding.txtManageTaskStatus.setBackgroundColor(itemView.getResources().getColor(R.color.success_green));
                            rowBinding.txtManageTaskStatus.setTextColor(itemView.getResources().getColor(R.color.white));
                            rowBinding.btnManageTaskApprove.setVisibility(View.GONE);
                        } else {
                            rowBinding.txtManageTaskStatus.setBackgroundColor(itemView.getResources().getColor(R.color.border_color));
                            rowBinding.txtManageTaskStatus.setTextColor(itemView.getResources().getColor(R.color.text_primary));
                            rowBinding.btnManageTaskApprove.setVisibility(View.GONE);
                        }

                        itemBinding.containerTasks.addView(rowBinding.getRoot());
                    }
                }
            } else {
                itemBinding.separatorLine.setVisibility(View.GONE);
                itemBinding.layoutTasksContainer.setVisibility(View.GONE);
            }
        }
    }
}
