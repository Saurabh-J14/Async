package com.async.app.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.async.app.R;
import com.async.app.databinding.ActivityMainBinding;
import com.async.app.model.AppNotification;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.repository.MockDataRepository;
import com.async.app.util.NotificationHelper;
import com.async.app.view.fragment.HomeFragment;
import com.async.app.view.fragment.ManageFragment;
import com.async.app.view.fragment.MyTaskFragment;
import com.async.app.view.fragment.ProjectsFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment tasksFragment = new MyTaskFragment();
    private Fragment thirdFragment;
    private Fragment activeFragment = homeFragment;

    private MockDataRepository repository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = MockDataRepository.getInstance();
        currentUser = repository.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Session expired, please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupThirdFragment();
        setupNavigation();
        setupToolbarNotifications();
        checkAndRequestNotificationPermission();
        showManagerAlertIfNeeded();
    }

    private static final int REQUEST_CODE_NOTIFICATIONS = 201;

    private void checkAndRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
            }
        }
    }

    private void showManagerAlertIfNeeded() {
        if (currentUser != null && "MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            int pendingCount = repository.getPendingReviewTasksCount();
            if (pendingCount > 0) {
                new AlertDialog.Builder(this)
                    .setTitle("Pending Reviews Alert")
                    .setMessage("Welcome back, Manager! You have " + pendingCount + " task(s) awaiting your review and approval.")
                    .setPositiveButton("Go to Manage Tab", (dialog, which) -> {
                        // Switch to the third tab programmatically
                        binding.navView.setSelectedItemId(R.id.navigation_projects);
                    })
                    .setNegativeButton("Dismiss", null)
                    .show();
            }
        }
    }

    private void setupThirdFragment() {
        // If Manager logs in, dynamically load ManageFragment instead of ProjectsFragment
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            thirdFragment = new ManageFragment();
            // Change BottomNav item title dynamically to "Manage"
            binding.navView.getMenu().findItem(R.id.navigation_projects).setTitle("Manage");
        } else {
            thirdFragment = new ProjectsFragment();
        }
    }

    private void setupNavigation() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Add all fragments to manager and hide non-active ones to preserve states
        fragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, thirdFragment, "projects").hide(thirdFragment)
                .add(R.id.nav_host_fragment, tasksFragment, "tasks").hide(tasksFragment)
                .add(R.id.nav_host_fragment, homeFragment, "home")
                .commit();

        // Default item is Home in BottomNav
        binding.navView.setSelectedItemId(R.id.navigation_home);

        // BottomNavigationView selection listener
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment nextFragment = null;

            if (itemId == R.id.navigation_home) {
                nextFragment = homeFragment;
            } else if (itemId == R.id.navigation_tasks) {
                nextFragment = tasksFragment;
            } else if (itemId == R.id.navigation_projects) {
                nextFragment = thirdFragment;
            }

            if (nextFragment != null && nextFragment != activeFragment) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(activeFragment).show(nextFragment).commit();
                activeFragment = nextFragment;
                return true;
            }
            return false;
        });
    }

    private void setupToolbarNotifications() {
        // Find notification elements from toolbar
        RelativeLayout btnBell = findViewById(R.id.btn_notification_bell);
        
        btnBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotificationsDialog();
            }
        });

        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        TextView txtBadge = findViewById(R.id.txt_notification_badge);
        if (txtBadge == null || currentUser == null) return;

        int unreadCount = repository.getUnreadNotificationCount(currentUser.getEmail());
        if (unreadCount > 0) {
            txtBadge.setVisibility(View.VISIBLE);
            txtBadge.setText(String.valueOf(unreadCount));
        } else {
            txtBadge.setVisibility(View.GONE);
        }
    }

    private void showNotificationsDialog() {
        if (currentUser == null) return;
        
        String email = currentUser.getEmail();
        List<AppNotification> list = repository.getNotificationsForUser(email);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notifications");
        
        if (list.isEmpty()) {
            builder.setMessage("You have no notifications.");
        } else {
            String[] items = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                AppNotification notif = list.get(i);
                items[i] = notif.getTitle() + "\n" + notif.getMessage();
            }
            builder.setItems(items, null);
        }
        
        builder.setPositiveButton("Mark as Read & Close", (dialog, which) -> {
            repository.markNotificationsAsRead(email);
            updateNotificationBadge();
        });
        
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check/Update badge count when app re-enters foreground
        updateNotificationBadge();
        
        // Reload manager fragment if it's active
        if (activeFragment instanceof ManageFragment) {
            ((ManageFragment) activeFragment).onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Trigger background task assignment simulation when app is closed/minimized
        simulateBackgroundAssignment();
    }

    private void simulateBackgroundAssignment() {
        if (currentUser == null || !"EMPLOYEE".equalsIgnoreCase(currentUser.getRole())) {
            return;
        }

        final String userEmail = currentUser.getEmail();
        
        // Spawn background thread to simulate external server assigning task after 5 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    
                    String taskTitle = "Optimize Database Indexes";
                    String taskDesc = "Analyze slow database queries and deploy composite indexes in production.";
                    
                    // Add new task under IT_DEPT workspace
                    Task bgTask = new Task(
                        "BG_" + System.currentTimeMillis(),
                        taskTitle,
                        taskDesc,
                        "HIGH",
                        "Database",
                        "Due in 3 days",
                        false,
                        Task.STATUS_TODO,
                        userEmail,
                        "IT_DEPT"
                    );
                    
                    boolean success = repository.addTask(bgTask);
                    if (success) {
                        // Fire a real system status bar notification
                        NotificationHelper.showNotification(
                            getApplicationContext(),
                            "New Task Assigned",
                            "You have been assigned: " + taskTitle
                        );
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
