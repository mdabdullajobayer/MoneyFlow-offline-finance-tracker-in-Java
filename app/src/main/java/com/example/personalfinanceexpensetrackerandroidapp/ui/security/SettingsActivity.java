package com.example.personalfinanceexpensetrackerandroidapp.ui.security;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.personalfinanceexpensetrackerandroidapp.MainActivity;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivitySettingsBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.AccountManagementActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.BudgetPlanActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.CategoryManagementActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.IncomeSourceManagementActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.OwnerManagementActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.PaymentMethodManagementActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.SmsReviewActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.SmsSenderActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.reports.ReportsActivity;
import com.example.personalfinanceexpensetrackerandroidapp.ui.transaction.AddEditTransactionActivity;
import com.example.personalfinanceexpensetrackerandroidapp.util.CurrencyUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.SecurityUtils;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        loadSettings();
        setupListeners();
        setupBottomNavigation();
    }

    private void loadSettings() {
        binding.switchAppLock.setChecked(SecurityUtils.isAppLockEnabled(this));
        binding.switchBiometric.setChecked(SecurityUtils.isBiometricEnabled(this));

        int themeMode = SecurityUtils.getThemeMode(this);
        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.rbDark.setChecked(true);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.rbLight.setChecked(true);
        } else {
            binding.rbSystem.setChecked(true);
        }

        String[] currencies = new String[]{"$ (USD)", "€ (EUR)", "৳ (BDT)", "£ (GBP)", "¥ (JPY/CNY)", "₹ (INR)", "A$ (AUD)", "C$ (CAD)", "CHF", "kr", "R$", "₩", "₦"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies);
        binding.actvCurrency.setAdapter(currencyAdapter);

        String currentSymbol = CurrencyUtils.getCurrencySymbol(this);
        binding.actvCurrency.setText(currentSymbol, false);
    }

    private void setupListeners() {
        binding.switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SecurityUtils.setAppLockEnabled(this, isChecked);
            if (isChecked && SecurityUtils.getSavedPin(this).isEmpty()) {
                showSetPinDialog();
            }
        });

        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SecurityUtils.setBiometricEnabled(this, isChecked);
        });

        binding.btnChangePin.setOnClickListener(v -> showSetPinDialog());

        binding.rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == binding.rbDark.getId()) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else if (checkedId == binding.rbLight.getId()) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            }
            SecurityUtils.setThemeMode(this, mode);
        });

        String[] currencies = new String[]{"$ (USD)", "€ (EUR)", "৳ (BDT)", "£ (GBP)", "¥ (JPY/CNY)", "₹ (INR)", "A$ (AUD)", "C$ (CAD)", "CHF", "kr", "R$", "₩", "₦"};
        binding.actvCurrency.setOnItemClickListener((parent, view, position, id) -> {
            String selected = currencies[position];
            String symbol = selected.contains(" ") ? selected.substring(0, selected.indexOf(" ")) : selected;
            binding.actvCurrency.setText(symbol, false);
            CurrencyUtils.setCurrencySymbol(this, symbol);
            Toast.makeText(this, "Currency updated to " + symbol, Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveCurrency.setOnClickListener(v -> {
            String input = binding.actvCurrency.getText().toString().trim();
            if (!input.isEmpty()) {
                String symbol = input.contains(" ") ? input.substring(0, input.indexOf(" ")) : input;
                CurrencyUtils.setCurrencySymbol(this, symbol);
                Toast.makeText(this, "Currency updated to " + symbol, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a valid currency symbol", Toast.LENGTH_SHORT).show();
            }
        });

        // Management & Automation Listeners
        binding.btnManageAccounts.setOnClickListener(v -> startActivity(new Intent(this, AccountManagementActivity.class)));
        binding.btnManageCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryManagementActivity.class)));
        binding.btnManageOwners.setOnClickListener(v -> startActivity(new Intent(this, OwnerManagementActivity.class)));
        binding.btnManageIncomeSources.setOnClickListener(v -> startActivity(new Intent(this, IncomeSourceManagementActivity.class)));
        binding.btnManagePaymentMethods.setOnClickListener(v -> startActivity(new Intent(this, PaymentMethodManagementActivity.class)));
        binding.btnManageSmsRules.setOnClickListener(v -> startActivity(new Intent(this, SmsSenderActivity.class)));
        binding.btnPendingSmsReview.setOnClickListener(v -> startActivity(new Intent(this, SmsReviewActivity.class)));

        // Developer & About Info Listeners
        binding.btnGitHub.setOnClickListener(v -> openUrl("https://github.com/mdabdullajobayer"));
        binding.btnLinkedIn.setOnClickListener(v -> openUrl("https://linkedin.com/in/mdabdullahjobayer"));
        binding.layoutEmail.setOnClickListener(v -> sendEmail("mdabdullajovayer@gmail.com"));
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String email) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open email client", Toast.LENGTH_SHORT).show();
        }
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
            Intent intent = new Intent(this, ReportsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
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
            // Already settings
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditTransactionActivity.class));
        });
    }

    private void showSetPinDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter 4-6 digit PIN");
        
        float density = getResources().getDisplayMetrics().density;
        int paddingPx = (int) (16 * density);
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Set Security PIN")
                .setMessage("Please enter a new PIN for MoneyFlow lock")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String pin = input.getText().toString().trim();
                    if (pin.length() >= 4) {
                        SecurityUtils.savePin(this, pin);
                        SecurityUtils.setAppLockEnabled(this, true);
                        binding.switchAppLock.setChecked(true);
                        Toast.makeText(this, "PIN saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
