package com.async.app.repository;

import com.async.app.model.AppNotification;
import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockDataRepository {
    private static MockDataRepository instance;

    private final List<User> users = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();
    private final List<AppNotification> notifications = new ArrayList<>();
    private User currentUser = null;

    private MockDataRepository() {
        // Prepopulate users: Manager and Employees
        users.add(new User("Project Manager", "sgrj414@gmail.com", "password123", "MANAGER"));
        users.add(new User("Developer User", "user@async.com", "password123", "EMPLOYEE"));
        users.add(new User("John Doe", "employee1@async.com", "password123", "EMPLOYEE"));
        users.add(new User("Sarah Smith", "employee2@async.com", "password123", "EMPLOYEE"));

        // Prepopulate projects (including IT, HR, Accounts workspaces)
        projects.add(new Project("1", "Mobile App Development", "Building the next-gen task manager featuring seamless sync and advanced charts.", "Engineering", 75, 4));
        projects.add(new Project("2", "Marketing Rebrand", "Designing fresh typography guidelines, logo updates, and online landing pages.", "Design", 40, 2));
        projects.add(new Project("3", "Database Scalability", "Optimizing indexes and implementing sharding to handle 100k concurrent users.", "DevOps", 15, 3));
        
        projects.add(new Project("IT_DEPT", "IT Department", "IT Department Workspace: networks, systems, infrastructure.", "IT", 50, 2));
        projects.add(new Project("HR_DEPT", "HR Department", "HR Department Workspace: hiring, onboarding, payroll.", "HR", 30, 1));
        projects.add(new Project("ACCOUNTS_DEPT", "Accounts Department", "Accounts Department Workspace: taxation, audits, financial reports.", "Finance", 80, 3));

        // Prepopulate tasks mapping to appropriate employees and workspaces
        tasks.add(new Task("1", "Design Login Screen UI", "Create high-fidelity mockups using Material Design tokens.", "HIGH", "UI Design", "Due Today", false, Task.STATUS_TODO, "user@async.com", "1"));
        tasks.add(new Task("2", "Setup MVVM Architecture", "Implement the project setup, folders, models, and repositories.", "HIGH", "Architecture", "Due Today", true, Task.STATUS_DONE, "user@async.com", "1"));
        tasks.add(new Task("3", "Integrate View Binding", "Configure app gradle files and enable view binding variables.", "MEDIUM", "Engineering", "Due Tomorrow", true, Task.STATUS_DONE, "user@async.com", "1"));
        tasks.add(new Task("4", "Implement Bottom Navigation", "Setup bottom bar menu with Home, My Task, and Projects tabs.", "MEDIUM", "Engineering", "Due in 2 days", false, Task.STATUS_TODO, "user@async.com", "1"));
        tasks.add(new Task("5", "Write Automated Tests", "Create unit and instrumentation tests for core ViewModel states.", "LOW", "QA Testing", "Due in 5 days", false, Task.STATUS_TODO, "user@async.com", "1"));

        // IT department tasks
        tasks.add(new Task("IT_1", "Setup Office WiFi", "Deploy APs in the new office wing.", "HIGH", "IT Support", "Due Tomorrow", false, Task.STATUS_TODO, "user@async.com", "IT_DEPT"));
        tasks.add(new Task("IT_2", "Server Migration", "Migrate legacy database server to AWS EC2.", "HIGH", "Cloud", "Due Today", false, Task.STATUS_REVIEW, "user@async.com", "IT_DEPT"));

        // HR department tasks
        tasks.add(new Task("HR_1", "Onboard New Engineers", "Send offer letter and setup email accounts.", "MEDIUM", "HR Operations", "Due in 2 days", false, Task.STATUS_TODO, "employee1@async.com", "HR_DEPT"));

        // Accounts department tasks
        tasks.add(new Task("ACC_1", "Quarterly Audit", "Review Q1 balance sheet and verify receipts.", "HIGH", "Auditing", "Due in 5 days", false, Task.STATUS_TODO, "employee2@async.com", "ACCOUNTS_DEPT"));

        // Prepopulate notifications
        notifications.add(new AppNotification("Task Assigned", "You were assigned Design Login Screen UI", "user@async.com"));
        notifications.add(new AppNotification("Workspace Added", "You were added to IT Department workspace", "user@async.com"));
        notifications.add(new AppNotification("Task Assigned", "You were assigned Setup Office WiFi", "user@async.com"));
        notifications.add(new AppNotification("Task Review Required", "Task 'Server Migration' is pending manager approval.", "sgrj414@gmail.com"));
    }

    public static synchronized MockDataRepository getInstance() {
        if (instance == null) {
            instance = new MockDataRepository();
        }
        return instance;
    }

    public boolean login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    public boolean register(String fullName, String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return false; // Email already registered
            }
        }
        // Force new registration to be EMPLOYEE
        User newUser = new User(fullName, email, password, "EMPLOYEE");
        users.add(newUser);
        currentUser = newUser;
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    // Tasks management
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public boolean isTaskIdDuplicate(String id) {
        for (Task task : tasks) {
            if (task.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean addTask(Task task) {
        if (isTaskIdDuplicate(task.getId())) {
            return false;
        }
        tasks.add(task);
        // Automatically create in-app notification when assigned
        if (task.getAssignedToEmail() != null) {
            addNotification(new AppNotification(
                "New Task Assigned",
                "You have been assigned: " + task.getTitle(),
                task.getAssignedToEmail()
            ));
        }
        return true;
    }

    public void addTask(String title, String description, String priority, String category, String dueDate) {
        String nextId = UUID.randomUUID().toString();
        String assignedEmail = (currentUser != null) ? currentUser.getEmail() : "user@async.com";
        String currentDate = getTodayFormattedDate();
        Task task = new Task(nextId, title, description, priority.toUpperCase(), category, dueDate, false, Task.STATUS_TODO, assignedEmail, "1", currentDate, "Pending");
        addTask(task);
    }

    public void addTask(String title, String description, String priority, String category, String dueDate, String assignedToEmail, String workspaceId) {
        String nextId = UUID.randomUUID().toString();
        String currentDate = getTodayFormattedDate();
        Task task = new Task(nextId, title, description, priority.toUpperCase(), category, dueDate, false, Task.STATUS_TODO, assignedToEmail, workspaceId, currentDate, "Pending");
        addTask(task);
    }

    public void updateTaskCompletion(String taskId, boolean isCompleted) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                if (currentUser != null && "MANAGER".equalsIgnoreCase(currentUser.getRole())) {
                    task.setCompleted(isCompleted);
                    task.setStatus(isCompleted ? Task.STATUS_DONE : Task.STATUS_TODO);
                    task.setCompletedDate(isCompleted ? getTodayFormattedDate() : "Pending");
                } else {
                    // Employee completes task -> transitions to REVIEW, completed status stays false until manager approves
                    if (isCompleted) {
                        task.setStatus(Task.STATUS_REVIEW);
                        task.setCompleted(false);
                        task.setCompletedDate("Pending (Under Review)");
                        
                        // Notify manager
                        addNotification(new AppNotification(
                            "Task Pending Review",
                            "Task '" + task.getTitle() + "' completed by " + (currentUser != null ? currentUser.getFullName() : "employee") + " is ready for review.",
                            "sgrj414@gmail.com"
                        ));
                    } else {
                        task.setStatus(Task.STATUS_TODO);
                        task.setCompleted(false);
                        task.setCompletedDate("Pending");
                    }
                }
                break;
            }
        }
    }

    public void approveTask(String taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                task.setStatus(Task.STATUS_DONE);
                task.setCompleted(true);
                task.setCompletedDate(getTodayFormattedDate());
                
                // Notify the assigned employee
                if (task.getAssignedToEmail() != null) {
                    addNotification(new AppNotification(
                        "Task Approved",
                        "Your task '" + task.getTitle() + "' has been approved by the Manager.",
                        task.getAssignedToEmail()
                    ));
                }
                break;
            }
        }
    }

    // Projects (workspaces) management
    public List<Project> getProjects() {
        return new ArrayList<>(projects);
    }

    public boolean isProjectIdDuplicate(String id) {
        for (Project project : projects) {
            if (project.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean addProject(Project project) {
        if (isProjectIdDuplicate(project.getId())) {
            return false;
        }
        projects.add(project);
        return true;
    }

    // Notifications API
    public List<AppNotification> getNotificationsForUser(String email) {
        List<AppNotification> userNotifications = new ArrayList<>();
        for (AppNotification notification : notifications) {
            if (notification.getTargetUserEmail().equalsIgnoreCase(email)) {
                userNotifications.add(notification);
            }
        }
        return userNotifications;
    }

    public int getUnreadNotificationCount(String email) {
        int count = 0;
        for (AppNotification notification : notifications) {
            if (notification.getTargetUserEmail().equalsIgnoreCase(email) && !notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markNotificationsAsRead(String email) {
        for (AppNotification notification : notifications) {
            if (notification.getTargetUserEmail().equalsIgnoreCase(email)) {
                notification.setRead(true);
            }
        }
    }

    public void addNotification(AppNotification notification) {
        notifications.add(notification);
    }

    public int getPendingReviewTasksCount() {
        int count = 0;
        for (Task task : tasks) {
            if (Task.STATUS_REVIEW.equals(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public void updateUserProfileImage(String email, String base64Image) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                user.setProfileImage(base64Image);
                if (currentUser != null && currentUser.getEmail().equalsIgnoreCase(email)) {
                    currentUser.setProfileImage(base64Image);
                }
                break;
            }
        }
    }

    private String getTodayFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
        return sdf.format(new java.util.Date());
    }
}
