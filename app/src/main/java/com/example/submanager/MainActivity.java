package com.example.submanager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.submanager.ui.fragment.AddExpenseFragment;
import com.example.submanager.ui.fragment.DashboardFragment;
import com.example.submanager.ui.fragment.ExpensesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.navigation_expenses) {
                fragment = new ExpensesFragment();
            } else if (itemId == R.id.navigation_add) {
                fragment = new AddExpenseFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }
}