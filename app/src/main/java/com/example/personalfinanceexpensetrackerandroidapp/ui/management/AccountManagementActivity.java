package com.example.personalfinanceexpensetrackerandroidapp.ui.management;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityManagementBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.AccountAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;

public class AccountManagementActivity extends AppCompatActivity {
    private ActivityManagementBinding binding;
    private FinanceViewModel viewModel;
    private AccountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Accounts");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new AccountAdapter(new ArrayList<>(), this::showEditDialog, this::confirmDelete);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add Account");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getAccounts().observe(this, accounts -> {
            if (accounts != null) {
                adapter.updateData(accounts);
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etBalance = dialogView.findViewById(R.id.etBalance);
        EditText etCurrency = dialogView.findViewById(R.id.etCurrency);
        etCurrency.setText("BDT");

        new AlertDialog.Builder(this)
                .setTitle("Add New Account")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    if (type.isEmpty()) type = "CASH";
                    double balance = 0.0;
                    try {
                        balance = Double.parseDouble(etBalance.getText().toString().trim());
                    } catch (Exception ignored) {}
                    String currency = etCurrency.getText().toString().trim();
                    if (currency.isEmpty()) currency = "BDT";

                    if (!name.isEmpty()) {
                        viewModel.addAccount(name, type, balance, currency, null, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Account added", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to add account", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Account account) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etBalance = dialogView.findViewById(R.id.etBalance);
        EditText etCurrency = dialogView.findViewById(R.id.etCurrency);

        etName.setText(account.getName());
        etType.setText(account.getType());
        etBalance.setText(String.valueOf(account.getBalance()));
        etCurrency.setText(account.getCurrency());

        new AlertDialog.Builder(this)
                .setTitle("Edit Account")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    double balance = account.getBalance();
                    try {
                        balance = Double.parseDouble(etBalance.getText().toString().trim());
                    } catch (Exception ignored) {}
                    String currency = etCurrency.getText().toString().trim();

                    if (!name.isEmpty()) {
                        Account updated = new Account(account.getId(), name, type, balance, currency, account.getIcon(), account.isActive());
                        viewModel.updateAccount(updated, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Account updated", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Account account) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete '" + account.getName() + "'? If referenced by transactions, it will be safely deactivated.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteOrDeactivateAccount(account.getId(), result -> runOnUiThread(() -> {
                        if (result == FinanceRepository.ResultType.DELETED) {
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Account deactivated (referenced by transactions)", Toast.LENGTH_LONG).show();
                        }
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
