package com.async.app.util;

import com.async.app.model.Task;
import com.async.app.model.User;

public interface OnEmployeeActionListener {
    void onDeleteEmployee(User employee);
    void onEditEmployee(User employee);
    void onApproveTask(Task task);
    String getWorkspaceName(String workspaceId);
}
