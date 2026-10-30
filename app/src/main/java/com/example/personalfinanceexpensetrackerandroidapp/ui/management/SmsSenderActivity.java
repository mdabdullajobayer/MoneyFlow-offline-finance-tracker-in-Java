package com.example.personalfinanceexpensetrackerandroidapp.ui.management;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.SmsSender;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivitySmsSenderBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.SmsSenderAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class SmsSenderActivity extends AppCompatActivity {
    private ActivitySmsSenderBinding binding;
    private FinanceViewModel viewModel;
    private SmsSenderAdapter adapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmsSenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage SMS Senders");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new SmsSenderAdapter(
                new ArrayList<>(),
                this::onToggleActive,
                this::showEditDialog,
                this::confirmDelete,
                this::showAccountActionDialog
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add SMS Sender");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        binding.btnImportSenders.setOnClickListener(v -> checkAndImportSenders());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        loadAndShowDiscoveredSenders();
                    } else {
                        Toast.makeText(this, "SMS permission is required to import senders from inbox.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        viewModel.getSmsSenders().observe(this, senders -> {
            if (senders != null) {
                adapter.updateData(senders);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshSmsSenders();
    }

    private void checkAndImportSenders() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            loadAndShowDiscoveredSenders();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_SMS);
        }
    }

    private void loadAndShowDiscoveredSenders() {
        List<String> existingSenderNames = new ArrayList<>();
        if (adapter != null && adapter.getSenders() != null) {
            for (SmsSender s : adapter.getSenders()) {
                if (s.getSenderName() != null) {
                    existingSenderNames.add(s.getSenderName().trim().toLowerCase());
                }
            }
        }

        List<String> uniqueSenders = new ArrayList<>();
        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.parse("content://sms/inbox");
            long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L;
            Cursor cursor = resolver.query(uri, new String[]{"address", "date"}, "date >= ?", new String[]{String.valueOf(thirtyDaysAgo)}, "date DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String address = cursor.getString(0);
                    long timestamp = cursor.getLong(1);
                    if (timestamp < thirtyDaysAgo) continue;
                    if (address != null && !address.trim().isEmpty()) {
                        String trimmed = address.trim();
                        if (!existingSenderNames.contains(trimmed.toLowerCase()) && !uniqueSenders.contains(trimmed)) {
                            uniqueSenders.add(trimmed);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uniqueSenders.isEmpty()) {
            Toast.makeText(this, "No new SMS senders found in inbox from the last month.", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] senderArray = uniqueSenders.toArray(new CharSequence[0]);
        boolean[] checkedItems = new boolean[senderArray.length];

        new AlertDialog.Builder(this)
                .setTitle("Import Senders from Inbox")
                .setMultiChoiceItems(senderArray, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Import Selected", (dialog, which) -> {
                    List<String> toImport = new ArrayList<>();
                    for (int i = 0; i < senderArray.length; i++) {
                        if (checkedItems[i]) {
                            toImport.add(senderArray[i].toString());
                        }
                    }
                    if (!toImport.isEmpty()) {
                        viewModel.importSmsSenders(toImport, success -> runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "Selected SMS senders imported successfully", Toast.LENGTH_SHORT).show();
                                viewModel.refreshSmsSenders();
                            } else {
                                Toast.makeText(this, "Failed to import SMS senders", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    } else {
                        Toast.makeText(this, "No senders selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onToggleActive(SmsSender sender, boolean isActive) {
        sender.setActive(isActive);
        viewModel.updateSmsSender(sender, success -> runOnUiThread(() -> {
            if (success) {
                Toast.makeText(this, "Sender " + (isActive ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update sender status", Toast.LENGTH_SHORT).show();
                viewModel.refreshSmsSenders();
            }
        }));
    }

    private void showAccountActionDialog(SmsSender sender) {
        CharSequence[] options = {"Create New Account from Sender", "Link to Existing Account"};
        new AlertDialog.Builder(this)
                .setTitle("Account Actions: " + sender.getSenderName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showCreateAccountForSenderDialog(sender);
                    } else {
                        showLinkExistingAccountDialog(sender);
                    }
                })
                .show();
    }

    private void showCreateAccountForSenderDialog(SmsSender sender) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etBalance = dialogView.findViewById(R.id.etBalance);
        EditText etCurrency = dialogView.findViewById(R.id.etCurrency);

        etName.setText(sender.getSenderName());
        etType.setText("MOBILE_WALLET");
        etBalance.setText("0.0");
        etCurrency.setText("BDT");

        new AlertDialog.Builder(this)
                .setTitle("Create Account from SMS Sender")
                .setView(dialogView)
                .setPositiveButton("Create & Link", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    if (type.isEmpty()) type = "MOBILE_WALLET";
                    double balance = 0.0;
                    try {
                        balance = Double.parseDouble(etBalance.getText().toString().trim());
                    } catch (Exception ignored) {}
                    String currency = etCurrency.getText().toString().trim();
                    if (currency.isEmpty()) currency = "BDT";

                    if (!name.isEmpty()) {
                        viewModel.addAccountWithId(name, type, balance, currency, null, accountId -> runOnUiThread(() -> {
                            if (accountId != -1L) {
                                sender.setAccountId(accountId);
                                viewModel.updateSmsSender(sender, success -> runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(this, "Account created and linked successfully", Toast.LENGTH_SHORT).show();
                                        viewModel.refreshSmsSenders();
                                    } else {
                                        Toast.makeText(this, "Account created, but failed to link sender", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                            } else {
                                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLinkExistingAccountDialog(SmsSender sender) {
        FinanceRepository repo = new FinanceRepository(this);
        repo.getAccounts(accounts -> runOnUiThread(() -> {
            if (accounts == null || accounts.isEmpty()) {
                Toast.makeText(this, "No accounts available. Please create an account first.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Account> activeAccounts = new ArrayList<>();
            for (Account acc : accounts) {
                if (acc.isActive()) activeAccounts.add(acc);
            }

            if (activeAccounts.isEmpty()) {
                Toast.makeText(this, "No active accounts available.", Toast.LENGTH_SHORT).show();
                return;
            }

            CharSequence[] accountNames = new CharSequence[activeAccounts.size()];
            for (int i = 0; i < activeAccounts.size(); i++) {
                accountNames[i] = activeAccounts.get(i).getName() + " (" + activeAccounts.get(i).getType() + ")";
            }

            new AlertDialog.Builder(this)
                    .setTitle("Link " + sender.getSenderName() + " to Account")
                    .setItems(accountNames, (dialog, which) -> {
                        Account selectedAccount = activeAccounts.get(which);
                        sender.setAccountId(selectedAccount.getId());
                        viewModel.updateSmsSender(sender, success -> runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "Linked to account: " + selectedAccount.getName(), Toast.LENGTH_SHORT).show();
                                viewModel.refreshSmsSenders();
                            } else {
                                Toast.makeText(this, "Failed to link SMS sender", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }));
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_sms_sender, null);
        EditText etSenderName = dialogView.findViewById(R.id.etSenderName);
        EditText etKeywordFilter = dialogView.findViewById(R.id.etKeywordFilter);
        EditText etAccountId = dialogView.findViewById(R.id.etAccountId);
        CheckBox cbIsActive = dialogView.findViewById(R.id.cbIsActive);

        new AlertDialog.Builder(this)
                .setTitle("Add Whitelisted SMS Sender")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String senderName = etSenderName.getText().toString().trim();
                    String keywordFilter = etKeywordFilter.getText().toString().trim();
                    long accountId = 3;
                    try {
                        accountId = Long.parseLong(etAccountId.getText().toString().trim());
                    } catch (Exception ignored) {}
                    boolean isActive = cbIsActive.isChecked();

                    if (!senderName.isEmpty()) {
                        viewModel.addSmsSender(senderName, keywordFilter, accountId, isActive, success -> runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "SMS sender added", Toast.LENGTH_SHORT).show();
                                viewModel.refreshSmsSenders();
                            } else {
                                Toast.makeText(this, "Failed to add SMS sender", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(SmsSender sender) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_sms_sender, null);
        EditText etSenderName = dialogView.findViewById(R.id.etSenderName);
        EditText etKeywordFilter = dialogView.findViewById(R.id.etKeywordFilter);
        EditText etAccountId = dialogView.findViewById(R.id.etAccountId);
        CheckBox cbIsActive = dialogView.findViewById(R.id.cbIsActive);

        etSenderName.setText(sender.getSenderName());
        etKeywordFilter.setText(sender.getKeywordFilter());
        etAccountId.setText(String.valueOf(sender.getAccountId()));
        cbIsActive.setChecked(sender.isActive());

        new AlertDialog.Builder(this)
                .setTitle("Edit SMS Sender")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String senderName = etSenderName.getText().toString().trim();
                    String keywordFilter = etKeywordFilter.getText().toString().trim();
                    long accountId = sender.getAccountId();
                    try {
                        accountId = Long.parseLong(etAccountId.getText().toString().trim());
                    } catch (Exception ignored) {}
                    boolean isActive = cbIsActive.isChecked();

                    if (!senderName.isEmpty()) {
                        SmsSender updated = new SmsSender(sender.getId(), senderName, keywordFilter, accountId, sender.getAccountName(), isActive);
                        viewModel.updateSmsSender(updated, success -> runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "SMS sender updated", Toast.LENGTH_SHORT).show();
                                viewModel.refreshSmsSenders();
                            } else {
                                Toast.makeText(this, "Failed to update SMS sender", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(SmsSender sender) {
        new AlertDialog.Builder(this)
                .setTitle("Delete SMS Sender")
                .setMessage("Are you sure you want to remove '" + sender.getSenderName() + "' from whitelisted senders?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSmsSender(sender.getId(), result -> runOnUiThread(() -> {
                        Toast.makeText(this, "SMS sender removed", Toast.LENGTH_SHORT).show();
                        viewModel.refreshSmsSenders();
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
