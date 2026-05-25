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
import com.async.app.util.SessionManager;
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

    private SessionManager sessionManager;
    private User currentUser;
    private final java.util.List<AppNotification> localNotifications = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        currentUser = sessionManager.getUser();

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
            int pendingCount = 0;
            if (pendingCount > 0) {
                new AlertDialog.Builder(this)
                    .setTitle("Pending Reviews Alert")
                    .setMessage("Welcome back, Manager! You have " + pendingCount + " task(s) awaiting your review and approval.")
                    .setPositiveButton("Go to Manage Tab", (dialog, which) -> {
                        binding.navView.setSelectedItemId(R.id.navigation_projects);
                    })
                    .setNegativeButton("Dismiss", null)
                    .show();
            }
        }
    }

    private void setupThirdFragment() {
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            thirdFragment = new ManageFragment();
            binding.navView.getMenu().findItem(R.id.navigation_projects).setTitle("Manage");
        } else {
            thirdFragment = new ProjectsFragment();
        }
    }

    private void setupNavigation() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        fragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, thirdFragment, "projects").hide(thirdFragment)
                .add(R.id.nav_host_fragment, tasksFragment, "tasks").hide(tasksFragment)
                .add(R.id.nav_host_fragment, homeFragment, "home")
                .commit();

        binding.navView.setSelectedItemId(R.id.navigation_home);

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
        binding.btnNotificationBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotificationsDialog();
            }
        });

        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        if (binding.txtNotificationBadge == null || currentUser == null) return;

        int unreadCount = 0;
        for (AppNotification notification : localNotifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        if (unreadCount > 0) {
            binding.txtNotificationBadge.setVisibility(View.VISIBLE);
            binding.txtNotificationBadge.setText(String.valueOf(unreadCount));
        } else {
            binding.txtNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void showNotificationsDialog() {
        if (currentUser == null) return;
        
        List<AppNotification> list = localNotifications;
        
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
            for (AppNotification notification : localNotifications) {
                notification.setRead(true);
            }
            updateNotificationBadge();
        });
        
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

}
