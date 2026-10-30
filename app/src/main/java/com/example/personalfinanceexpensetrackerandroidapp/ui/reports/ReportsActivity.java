package com.example.personalfinanceexpensetrackerandroidapp.ui.reports;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinanceexpensetrackerandroidapp.MainActivity;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Category;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityReportsBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.BudgetPlanActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.security.SettingsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.transaction.AddEditTransactionActivity;
import com.example.personalfinanceexpensetrackerandroidapp.util.BackupUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.CurrencyUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.ExportUtils;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportsActivity extends AppCompatActivity {
    private ActivityReportsBinding binding;
    private FinanceViewModel viewModel;

    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();
    private List<Owner> ownersList = new ArrayList<>();
    private List<Account> accountsList = new ArrayList<>();
    private List<Category> categoriesList = new ArrayList<>();

    private String selectedPeriod = "Current Month";
    private Long selectedOwnerId = null;
    private Long selectedAccountId = null;
    private Long selectedCategoryId = null;

    private ActivityResultLauncher<String> createCsvLauncher;
    private ActivityResultLauncher<String> createExcelLauncher;
    private ActivityResultLauncher<String> createBackupLauncher;
    private ActivityResultLauncher<String[]> openRestoreLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Financial Reports & Analytics");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        setupLaunchers();
        setupDropdowns();
        setupExportAndBackupButtons();
        setupBottomNavigation();

        viewModel.getTransactions().observe(this, txs -> {
            if (txs != null) {
                allTransactions = txs;
                updateReports();
            }
        });

        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                ownersList = owners;
                List<String> ownerNames = new ArrayList<>();
                ownerNames.add("All Owners");
                for (Owner o : owners) {
                    ownerNames.add(o.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ownerNames);
                binding.actvReportOwner.setAdapter(adapter);
            }
        });

        viewModel.getAccounts().observe(this, accounts -> {
            if (accounts != null) {
                accountsList = accounts;
                List<String> accountNames = new ArrayList<>();
                accountNames.add("All Accounts");
                for (Account a : accounts) {
                    accountNames.add(a.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountNames);
                binding.actvReportAccount.setAdapter(adapter);
            }
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoriesList = categories;
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("All Categories");
                for (Category c : categories) {
                    categoryNames.add(c.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
                binding.actvReportCategory.setAdapter(adapter);
            }
        });
    }

    private void setupBottomNavigation() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navReport.setOnClickListener(v -> {
            // Already report
        });

        binding.navPlan.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, BudgetPlanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open Budget Plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditTransactionActivity.class));
        });
    }

    private void setupLaunchers() {
        createCsvLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
            if (uri != null) {
                boolean success = ExportUtils.exportToCsv(this, filteredTransactions, uri);
                if (success) {
                    Toast.makeText(this, "CSV Export Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "CSV Export Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createExcelLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), uri -> {
            if (uri != null) {
                boolean success = ExportUtils.exportToExcel(this, filteredTransactions, uri);
                if (success) {
                    Toast.makeText(this, "Excel Export Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Excel Export Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createBackupLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri != null) {
                boolean success = BackupUtils.exportBackupJson(this, uri);
                if (success) {
                    Toast.makeText(this, "Database Backup Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Database Backup Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        openRestoreLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Restore")
                        .setMessage("Restoring from backup will overwrite all current data and app settings. Proceed?")
                        .setPositiveButton("Restore", (dialog, which) -> {
                            boolean success = BackupUtils.restoreBackupJson(this, uri);
                            if (success) {
                                Toast.makeText(this, "Database Restore Successful", Toast.LENGTH_SHORT).show();
                                viewModel.refreshAll();
                            } else {
                                Toast.makeText(this, "Database Restore Failed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void setupExportAndBackupButtons() {
        binding.btnExportCsv.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            createCsvLauncher.launch("moneyflow_transactions_" + timeStamp + ".csv");
        });

        binding.btnExportExcel.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            createExcelLauncher.launch("moneyflow_transactions_" + timeStamp + ".xlsx");
        });

        binding.btnBackup.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            createBackupLauncher.launch("moneyflow_backup_" + timeStamp + ".json");
        });

        binding.btnRestore.setOnClickListener(v -> {
            openRestoreLauncher.launch(new String[]{"application/json", "*/*"});
        });
    }

    private void setupDropdowns() {
        List<String> periods = Arrays.asList("Current Month", "Last Month", "All Time");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
        binding.actvReportPeriod.setAdapter(periodAdapter);
        binding.actvReportPeriod.setOnItemClickListener((parent, view, position, id) -> {
            selectedPeriod = periods.get(position);
            updateReports();
        });

        binding.actvReportOwner.setOnItemClickListener((parent, view, position, id) -> {
            selectedOwnerId = (position == 0) ? null : ownersList.get(position - 1).getId();
            updateReports();
        });

        binding.actvReportAccount.setOnItemClickListener((parent, view, position, id) -> {
            selectedAccountId = (position == 0) ? null : accountsList.get(position - 1).getId();
            updateReports();
        });

        binding.actvReportCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = (position == 0) ? null : categoriesList.get(position - 1).getId();
            updateReports();
        });
    }

    private void updateReports() {
        Calendar cal = Calendar.getInstance();
        String currentYearMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.getTime());

        cal.add(Calendar.MONTH, -1);
        String lastYearMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.getTime());

        Long ownerIdFilter = selectedOwnerId;
        Long accountIdFilter = selectedAccountId;
        Long categoryIdFilter = selectedCategoryId;

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            if (!tx.isActive()) continue;
            if (ownerIdFilter != null && tx.getOwnerId() != ownerIdFilter) continue;
            if (accountIdFilter != null && tx.getAccountId() != accountIdFilter && (tx.getDestinationAccountId() == null || !accountIdFilter.equals(tx.getDestinationAccountId()))) continue;
            if (categoryIdFilter != null && (tx.getCategoryId() == null || !categoryIdFilter.equals(tx.getCategoryId()))) continue;

            if ("Current Month".equals(selectedPeriod)) {
                if (tx.getDate() != null && tx.getDate().startsWith(currentYearMonth)) {
                    filtered.add(tx);
                }
            } else if ("Last Month".equals(selectedPeriod)) {
                if (tx.getDate() != null && tx.getDate().startsWith(lastYearMonth)) {
                    filtered.add(tx);
                }
            } else {
                filtered.add(tx);
            }
        }
        filteredTransactions = filtered;

        double totalIncome = 0.0;
        double totalExpense = 0.0;
        Map<String, Double> categoryExpenses = new HashMap<>();

        for (Transaction tx : filtered) {
            double amount = tx.getAmount() / 100.0;
            String typeName = tx.getTransactionTypeName();
            if ("IN".equals(typeName)) {
                totalIncome += amount;
            } else if ("OUT".equals(typeName)) {
                totalExpense += amount;
                String catName = tx.getCategoryName();
                if (catName == null) catName = "Uncategorized";
                categoryExpenses.put(catName, categoryExpenses.getOrDefault(catName, 0.0) + amount);
            }
        }

        double netSavings = totalIncome - totalExpense;

        binding.tvReportIncome.setText(CurrencyUtils.format(this, totalIncome));
        binding.tvReportExpense.setText(CurrencyUtils.format(this, totalExpense));
        binding.tvReportSavings.setText(CurrencyUtils.format(this, netSavings));

        setupPieChart(categoryExpenses);
        setupBarChart(totalIncome, totalExpense);
    }

    private void setupPieChart(Map<String, Double> categoryExpenses) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        List<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        binding.pieChart.setData(new PieData(dataSet));
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Expenses");
        binding.pieChart.setCenterTextSize(14f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();
    }

    private void setupBarChart(double income, double expense) {
        List<BarEntry> entries = Arrays.asList(
                new BarEntry(0f, (float) income),
                new BarEntry(1f, (float) expense)
        );

        BarDataSet dataSet = new BarDataSet(entries, "Amount ($)");
        dataSet.setColors(Arrays.asList(Color.parseColor("#2E7D32"), Color.parseColor("#C62828")));
        dataSet.setValueTextSize(12f);

        binding.barChart.setData(new BarData(dataSet));
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Expense"}));
        binding.barChart.getXAxis().setGranularity(1f);
        binding.barChart.getXAxis().setGranularityEnabled(true);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.animateY(1000);
        binding.barChart.invalidate();
    }
}
