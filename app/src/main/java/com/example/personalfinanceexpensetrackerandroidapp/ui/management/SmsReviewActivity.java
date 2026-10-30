package com.example.personalfinanceexpensetrackerandroidapp.ui.management;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Category;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.SmsLog;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivitySmsReviewBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.SmsLogAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class SmsReviewActivity extends AppCompatActivity {
    private ActivitySmsReviewBinding binding;
    private FinanceViewModel viewModel;
    private SmsLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmsReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pending SMS Transactions");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new SmsLogAdapter(
                new ArrayList<>(),
                this::approveLog,
                this::editAndApproveLog,
                this::rejectLog
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        viewModel.getSmsLogs().observe(this, logs -> {
            if (logs != null) {
                adapter.updateData(logs);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshSmsLogs();
    }

    private void approveLog(SmsLog log) {
        new FinanceRepository(this).getOwners(owners -> {
            new FinanceRepository(this).getCategories(categories -> runOnUiThread(() -> {
                List<Owner> activeOwners = new ArrayList<>();
                if (owners != null) {
                    for (Owner o : owners) {
                        if (o.isActive()) activeOwners.add(o);
                    }
                }
                if (activeOwners.isEmpty()) {
                    activeOwners.add(new Owner(1L, "Personal", "PERSONAL", true, true));
                }

                String[] ownerNames = new String[activeOwners.size()];
                for (int i = 0; i < activeOwners.size(); i++) {
                    ownerNames[i] = activeOwners.get(i).getName();
                }

                Spinner spinnerOwner = new Spinner(this);
                ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ownerNames);
                ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOwner.setAdapter(ownerAdapter);

                List<Category> tempCategories = new ArrayList<>();
                if (categories != null) {
                    for (Category c : categories) {
                        if ("TRANSFER".equalsIgnoreCase(log.getTransactionType()) || c.getType().equalsIgnoreCase(log.getTransactionType())) {
                            tempCategories.add(c);
                        }
                    }
                }
                if (tempCategories.isEmpty() && categories != null) {
                    tempCategories = categories;
                }
                if (tempCategories.isEmpty()) {
                    tempCategories.add(new Category(1L, "General", "OUT", "default", 1L, true));
                }
                final List<Category> finalFilteredCategories = tempCategories;

                String[] categoryNames = new String[finalFilteredCategories.size()];
                for (int i = 0; i < finalFilteredCategories.size(); i++) {
                    categoryNames[i] = finalFilteredCategories.get(i).getName();
                }

                Spinner spinnerCategory = new Spinner(this);
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(catAdapter);

                android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setPadding(40, 40, 40, 40);

                TextView tvOwnerLabel = new TextView(this);
                tvOwnerLabel.setText("Owner:");
                tvOwnerLabel.setPadding(0, 10, 0, 10);
                layout.addView(tvOwnerLabel);
                layout.addView(spinnerOwner);

                TextView tvCatLabel = new TextView(this);
                tvCatLabel.setText("Category:");
                tvCatLabel.setPadding(0, 20, 0, 10);
                layout.addView(tvCatLabel);
                layout.addView(spinnerCategory);

                new AlertDialog.Builder(this)
                        .setTitle("Approve SMS Transaction")
                        .setView(layout)
                        .setPositiveButton("Approve", (dialog, which) -> {
                            int ownerPos = spinnerOwner.getSelectedItemPosition();
                            long chosenOwnerId = activeOwners.get(Math.max(0, Math.min(ownerPos, activeOwners.size() - 1))).getId();

                            int catPos = spinnerCategory.getSelectedItemPosition();
                            Long chosenCatId = null;
                            if (!finalFilteredCategories.isEmpty() && catPos >= 0 && catPos < finalFilteredCategories.size()) {
                                chosenCatId = finalFilteredCategories.get(catPos).getId();
                            }

                            executeApproval(log, log.getParsedAmount(), log.getTransactionType(), log.getReference(), chosenCatId, chosenOwnerId);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }));
        });
    }

    private void editAndApproveLog(SmsLog log) {
        new FinanceRepository(this).getOwners(owners -> {
            new FinanceRepository(this).getCategories(categories -> runOnUiThread(() -> {
                EditText etAmount = new EditText(this);
                etAmount.setHint("Amount");
                etAmount.setText(String.valueOf(log.getParsedAmount()));

                EditText etRef = new EditText(this);
                etRef.setHint("Reference");
                etRef.setText(log.getReference());

                List<Owner> activeOwners = new ArrayList<>();
                if (owners != null) {
                    for (Owner o : owners) {
                        if (o.isActive()) activeOwners.add(o);
                    }
                }
                if (activeOwners.isEmpty()) {
                    activeOwners.add(new Owner(1L, "Personal", "PERSONAL", true, true));
                }

                String[] ownerNames = new String[activeOwners.size()];
                for (int i = 0; i < activeOwners.size(); i++) {
                    ownerNames[i] = activeOwners.get(i).getName();
                }

                Spinner spinnerOwner = new Spinner(this);
                ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ownerNames);
                ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOwner.setAdapter(ownerAdapter);

                List<Category> tempCategories = new ArrayList<>();
                if (categories != null) {
                    for (Category c : categories) {
                        if ("TRANSFER".equalsIgnoreCase(log.getTransactionType()) || c.getType().equalsIgnoreCase(log.getTransactionType())) {
                            tempCategories.add(c);
                        }
                    }
                    if (tempCategories.isEmpty()) {
                        tempCategories = categories;
                    }
                }
                if (tempCategories.isEmpty()) {
                    tempCategories.add(new Category(1L, "General", "OUT", "default", 1L, true));
                }
                final List<Category> finalFilteredCategories = tempCategories;
                String[] categoryNames = new String[finalFilteredCategories.size()];
                for (int i = 0; i < finalFilteredCategories.size(); i++) {
                    categoryNames[i] = finalFilteredCategories.get(i).getName();
                }

                Spinner spinnerCategory = new Spinner(this);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(spinnerAdapter);

                android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setPadding(40, 40, 40, 40);
                layout.addView(etAmount);
                layout.addView(etRef);

                TextView tvOwnerLabel = new TextView(this);
                tvOwnerLabel.setText("Owner:");
                tvOwnerLabel.setPadding(0, 20, 0, 10);
                layout.addView(tvOwnerLabel);
                layout.addView(spinnerOwner);

                TextView tvCatLabel = new TextView(this);
                tvCatLabel.setText("Category:");
                tvCatLabel.setPadding(0, 20, 0, 10);
                layout.addView(tvCatLabel);
                layout.addView(spinnerCategory);

                new AlertDialog.Builder(this)
                        .setTitle("Edit & Approve SMS")
                        .setView(layout)
                        .setPositiveButton("Approve", (dialog, which) -> {
                            double amt = log.getParsedAmount();
                            try {
                                amt = Double.parseDouble(etAmount.getText().toString().trim());
                            } catch (Exception ignored) {}
                            String ref = etRef.getText().toString().trim();
                            if (ref.isEmpty()) ref = log.getReference();

                            int ownerPos = spinnerOwner.getSelectedItemPosition();
                            long chosenOwnerId = activeOwners.get(Math.max(0, Math.min(ownerPos, activeOwners.size() - 1))).getId();

                            Long catId = null;
                            int selectedPos = spinnerCategory.getSelectedItemPosition();
                            if (!finalFilteredCategories.isEmpty() && selectedPos >= 0 && selectedPos < finalFilteredCategories.size()) {
                                catId = finalFilteredCategories.get(selectedPos).getId();
                            } else if (!finalFilteredCategories.isEmpty()) {
                                catId = finalFilteredCategories.get(0).getId();
                            }

                            executeApproval(log, amt, log.getTransactionType(), ref, catId, chosenOwnerId);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }));
        });
    }

    private void executeApproval(SmsLog log, double amountDouble, String transactionType, String reference, Long categoryId, long ownerId) {
        long amountCents = Math.round(amountDouble * 100);
        long transactionTypeId;
        Long resolvedCategoryId = categoryId;
        Long destinationAccountId = null;

        if ("IN".equalsIgnoreCase(transactionType)) {
            transactionTypeId = 1L;
            if (resolvedCategoryId == null) resolvedCategoryId = 8L; // Salary
        } else if ("TRANSFER".equalsIgnoreCase(transactionType)) {
            transactionTypeId = 3L;
            resolvedCategoryId = null;
            destinationAccountId = log.getAccountId() == 1L ? 2L : 1L;
        } else {
            transactionTypeId = 2L;
            if (resolvedCategoryId == null) resolvedCategoryId = 1L; // Food & Dining
        }
        long paymentMethodId = 4L; // MFS

        Transaction transaction = new Transaction(
                0,
                ownerId,
                log.getAccountId(),
                destinationAccountId,
                transactionTypeId,
                resolvedCategoryId,
                null,
                paymentMethodId,
                amountCents,
                log.getReceivedDate(),
                "Parsed from SMS (" + log.getSender() + "): " + reference,
                log.getSender(),
                false,
                null,
                true,
                null, null, null, null, null, null, null
        );

        viewModel.addTransaction(transaction, success -> runOnUiThread(() -> {
            if (success) {
                // Update SMS log status to APPROVED
                viewModel.updateSmsLogStatus(log.getId(), "APPROVED", null, updateSuccess -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "SMS transaction approved and added", Toast.LENGTH_SHORT).show();
                        viewModel.refreshSmsLogs();
                        viewModel.refreshTransactions(null);
                        viewModel.refreshAccounts();
                    });
                });
            } else {
                Toast.makeText(this, "Failed to create transaction from SMS", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void rejectLog(SmsLog log) {
        viewModel.updateSmsLogStatus(log.getId(), "REJECTED", null, success -> runOnUiThread(() -> {
            if (success) {
                Toast.makeText(this, "SMS log rejected", Toast.LENGTH_SHORT).show();
                viewModel.refreshSmsLogs();
            } else {
                Toast.makeText(this, "Failed to reject SMS log", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
