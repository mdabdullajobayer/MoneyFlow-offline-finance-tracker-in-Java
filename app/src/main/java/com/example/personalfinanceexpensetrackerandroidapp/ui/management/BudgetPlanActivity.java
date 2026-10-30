package com.example.personalfinanceexpensetrackerandroidapp.ui.management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.MainActivity;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Category;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityBudgetPlanBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.CategoryAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.ui.reports.ReportsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.security.SettingsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.transaction.AddEditTransactionActivity;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class BudgetPlanActivity extends AppCompatActivity {
    private ActivityBudgetPlanBinding binding;
    private FinanceViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityBudgetPlanBinding.inflate(getLayoutInflater());
            if (binding == null) {
                finish();
                return;
            }
            setContentView(binding.getRoot());

            if (binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Budget & Financial Plan");
                }
                binding.toolbar.setNavigationOnClickListener(v -> finish());
            }

            if (binding.rvBudgets != null) {
                binding.rvBudgets.setLayoutManager(new LinearLayoutManager(this));
                categoryAdapter = new CategoryAdapter(new ArrayList<>(),
                        cat -> {},
                        cat -> {}
                );
                binding.rvBudgets.setAdapter(categoryAdapter);
            }

            viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

            setupBottomNavigation();
            observeData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting Budget Plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void observeData() {
        if (viewModel == null) return;

        try {
            viewModel.getCategories().observe(this, categories -> {
                if (categories != null && categoryAdapter != null) {
                    List<Category> activeCategories = new ArrayList<>();
                    for (Category c : categories) {
                        if (c.isActive()) {
                            activeCategories.add(c);
                        }
                    }
                    categoryAdapter.updateData(activeCategories);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBottomNavigation() {
        if (binding == null) return;

        if (binding.navHome != null) {
            binding.navHome.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (binding.navReport != null) {
            binding.navReport.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, ReportsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (binding.navPlan != null) {
            binding.navPlan.setOnClickListener(v -> {
                // Already in Plan screen
            });
        }

        if (binding.navSettings != null) {
            binding.navSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (binding.fabAddTransaction != null) {
            binding.fabAddTransaction.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(this, AddEditTransactionActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
