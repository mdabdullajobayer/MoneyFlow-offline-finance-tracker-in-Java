package com.example.personalfinanceexpensetrackerandroidapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.example.personalfinanceexpensetrackerandroidapp.util.NotificationHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityMainBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.TransactionAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.BudgetPlanActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.reports.ReportsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.security.LockActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.security.RegistrationActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.security.SettingsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.transaction.AddEditTransactionActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.transaction.TransactionHistoryActivity;
import com.example.personalfinanceexpensetrackerandroidapp.util.CurrencyUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.SecurityUtils;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter recentTxAdapter;

    private List<Account> accountsList = new ArrayList<>();
    private List<Owner> ownersList = new ArrayList<>();
    private List<Transaction> allTransactions = new ArrayList<>();

    private int selectedYear;
    private int selectedMonth; // 0-indexed: 0 = Jan, 11 = Dec
    private static final String[] MONTH_NAMES = new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private final ActivityResultLauncher<Intent> lockLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    finish();
                }
            }
    );

    private final ActivityResultLauncher<Intent> registrationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (!SecurityUtils.isUserRegistered(this)) {
                    finish();
                } else if (SecurityUtils.isAppLockEnabled(this) && !SecurityUtils.isUnlockedInSession()) {
                    lockLauncher.launch(new Intent(this, LockActivity.class));
                } else {
                    if (binding != null) {
                        binding.tvUserName.setText(SecurityUtils.getUserName(this));
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                // Permission handled
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            SecurityUtils.getInstallTimestamp(this);
            AppCompatDelegate.setDefaultNightMode(SecurityUtils.getThemeMode(this));

            NotificationHelper.createNotificationChannel(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }

            if (!SecurityUtils.isUserRegistered(this)) {
                registrationLauncher.launch(new Intent(this, RegistrationActivity.class));
            } else if (SecurityUtils.isAppLockEnabled(this) && !SecurityUtils.isUnlockedInSession()) {
                lockLauncher.launch(new Intent(this, LockActivity.class));
            }

            Calendar cal = Calendar.getInstance();
            selectedYear = cal.get(Calendar.YEAR);
            selectedMonth = cal.get(Calendar.MONTH);

            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            if (binding != null && binding.tvUserName != null) {
                binding.tvUserName.setText(SecurityUtils.getUserName(this));
            }

            viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

            setupUI();
            observeData();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && binding.tvUserName != null) {
            binding.tvUserName.setText(SecurityUtils.getUserName(this));
        }
        if (viewModel != null) {
            viewModel.refreshAll();
        }
    }

    private void setupUI() {
        recentTxAdapter = new TransactionAdapter(
                new ArrayList<>(),
                tx -> {
                    Intent intent = new Intent(this, AddEditTransactionActivity.class);
                    intent.putExtra("transaction_id", tx.getId());
                    startActivity(intent);
                },
                tx -> {
                    Intent intent = new Intent(this, TransactionHistoryActivity.class);
                    startActivity(intent);
                }
        );

        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentTransactions.setAdapter(recentTxAdapter);

        View.OnClickListener monthSelectorListener = v -> showMonthYearPickerDialog();

        if (binding.btnMonthSelector != null) {
            binding.btnMonthSelector.setOnClickListener(monthSelectorListener);
        }
        if (binding.tvCurrentMonthYear != null) {
            binding.tvCurrentMonthYear.setOnClickListener(monthSelectorListener);
        }
        if (binding.btnDateSelector != null) {
            binding.btnDateSelector.setOnClickListener(monthSelectorListener);
        }

        binding.btnReports.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ReportsActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.btnViewAllTransactions.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open transaction history", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open transaction history", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditTransactionActivity.class));
        });

        // Bottom Navigation
        binding.navHome.setOnClickListener(v -> {
            // Already home
        });

        binding.navReport.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            overridePendingTransition(0, 0);
        });

        binding.navPlan.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, BudgetPlanActivity.class));
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open Budget Plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void showMonthYearPickerDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);

            AutoCompleteTextView actvMonth = dialogView.findViewById(R.id.actvMonth);
            AutoCompleteTextView actvYear = dialogView.findViewById(R.id.actvYear);
            NumberPicker pickerMonth = dialogView.findViewById(R.id.pickerMonth);
            NumberPicker pickerYear = dialogView.findViewById(R.id.pickerYear);

            List<String> monthsList = Arrays.asList(MONTH_NAMES);
            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, monthsList);
            if (actvMonth != null) {
                actvMonth.setAdapter(monthAdapter);
                actvMonth.setText(MONTH_NAMES[selectedMonth], false);
            }

            List<String> yearsList = new ArrayList<>();
            for (int y = 2020; y <= 2030; y++) {
                yearsList.add(String.valueOf(y));
            }
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, yearsList);
            if (actvYear != null) {
                actvYear.setAdapter(yearAdapter);
                actvYear.setText(String.valueOf(selectedYear), false);
            }

            if (pickerMonth != null) {
                pickerMonth.setMinValue(0);
                pickerMonth.setMaxValue(11);
                pickerMonth.setDisplayedValues(MONTH_NAMES);
                pickerMonth.setValue(selectedMonth);
            }

            if (pickerYear != null) {
                pickerYear.setMinValue(2020);
                pickerYear.setMaxValue(2030);
                pickerYear.setValue(selectedYear);
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select Month & Year")
                    .setView(dialogView)
                    .setPositiveButton("OK", (dialog, which) -> {
                        int chosenMonth = selectedMonth;
                        int chosenYear = selectedYear;

                        if (actvMonth != null && actvMonth.getText() != null) {
                            String mStr = actvMonth.getText().toString().trim();
                            int idx = monthsList.indexOf(mStr);
                            if (idx != -1) {
                                chosenMonth = idx;
                            } else if (pickerMonth != null) {
                                chosenMonth = pickerMonth.getValue();
                            }
                        } else if (pickerMonth != null) {
                            chosenMonth = pickerMonth.getValue();
                        }

                        if (actvYear != null && actvYear.getText() != null) {
                            String yStr = actvYear.getText().toString().trim();
                            try {
                                chosenYear = Integer.parseInt(yStr);
                            } catch (NumberFormatException e) {
                                if (pickerYear != null) {
                                    chosenYear = pickerYear.getValue();
                                }
                            }
                        } else if (pickerYear != null) {
                            chosenYear = pickerYear.getValue();
                        }

                        selectedMonth = chosenMonth;
                        selectedYear = chosenYear;
                        updateDashboard();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void observeData() {
        viewModel.getAccounts().observe(this, accounts -> {
            if (accounts != null) {
                accountsList = accounts;
                updateDashboard();
            }
        });

        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                ownersList = owners;
                updateDashboard();
            }
        });

        viewModel.getTransactions().observe(this, txs -> {
            if (txs != null) {
                allTransactions = txs;
                updateDashboard();
            }
        });

        viewModel.getTotalAccountBalanceLiveData().observe(this, totalMinorUnits -> {
            if (totalMinorUnits != null) {
                updateTotalBalanceDisplay(totalMinorUnits);
            }
        });
    }

    private void updateTotalBalanceDisplay(long totalMinorUnits) {
        String formatted = CurrencyUtils.formatAmount(this, totalMinorUnits);
        if (binding != null && binding.tvTotalBalance != null) {
            binding.tvTotalBalance.setText(formatted);
        }
        View currentBalanceView = findViewById(getResources().getIdentifier("tvCurrentBalance", "id", getPackageName()));
        if (currentBalanceView instanceof android.widget.TextView) {
            ((android.widget.TextView) currentBalanceView).setText(formatted);
        }
    }

    private void updateDashboard() {
        try {
            String monthName = MONTH_NAMES[selectedMonth];
            String titleText = monthName + " " + selectedYear;
            String buttonText = titleText + " ▼";

            if (binding.btnMonthSelector != null) {
                binding.btnMonthSelector.setText(buttonText);
            }
            if (binding.tvCurrentMonthYear != null) {
                binding.tvCurrentMonthYear.setText(titleText);
            }
            if (binding.btnDateSelector != null) {
                binding.btnDateSelector.setText(buttonText);
            }

            long totalBalanceMinorUnits = 0;
            for (Account acc : accountsList) {
                if (acc != null && acc.isActive()) {
                    totalBalanceMinorUnits += acc.getBalanceMinorUnits();
                }
            }
            updateTotalBalanceDisplay(totalBalanceMinorUnits);

            String selectedYearMonth = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth + 1);
            List<Transaction> monthTxs = new ArrayList<>();
            double selectedMonthIncome = 0.0;
            double selectedMonthExpense = 0.0;

            for (Transaction tx : allTransactions) {
                if (tx != null && tx.isActive() && tx.getDate() != null && tx.getDate().startsWith(selectedYearMonth)) {
                    monthTxs.add(tx);
                    double amt = tx.getAmount() / 100.0;
                    String typeName = tx.getTransactionTypeName();
                    if ("IN".equals(typeName)) {
                        selectedMonthIncome += amt;
                    } else if ("OUT".equals(typeName)) {
                        selectedMonthExpense += amt;
                    }
                }
            }

            double selectedMonthSavings = selectedMonthIncome - selectedMonthExpense;

            binding.tvMonthlyIncome.setText(CurrencyUtils.format(this, selectedMonthIncome));
            binding.tvMonthlyExpense.setText(CurrencyUtils.format(this, selectedMonthExpense));

            if (binding.tvMonthlySavings != null) {
                binding.tvMonthlySavings.setText(CurrencyUtils.format(this, selectedMonthSavings));
            }
            if (binding.tvSelectedMonthIncome != null) {
                binding.tvSelectedMonthIncome.setText(CurrencyUtils.format(this, selectedMonthIncome));
            }
            if (binding.tvSelectedMonthExpense != null) {
                binding.tvSelectedMonthExpense.setText(CurrencyUtils.format(this, selectedMonthExpense));
            }
            if (binding.tvSelectedMonthSavings != null) {
                binding.tvSelectedMonthSavings.setText(CurrencyUtils.format(this, selectedMonthSavings));
            }

            if (binding.barChart != null) {
                setupBarChart(selectedMonthIncome, selectedMonthExpense);
            }

            int takeCount = Math.min(5, monthTxs.size());
            recentTxAdapter.updateData(monthTxs.subList(0, takeCount));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBarChart(double income, double expense) {
        if (binding.barChart == null) return;

        List<BarEntry> entries = Arrays.asList(
                new BarEntry(0f, (float) income),
                new BarEntry(1f, (float) expense)
        );

        BarDataSet dataSet = new BarDataSet(entries, "Amount ($)");
        dataSet.setColors(Arrays.asList(Color.parseColor("#2E7D32"), Color.parseColor("#C62828")));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        binding.barChart.setData(barData);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Expense"}));
        binding.barChart.getXAxis().setGranularity(1f);
        binding.barChart.getXAxis().setGranularityEnabled(true);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.animateY(500);
        binding.barChart.invalidate();
    }
}
