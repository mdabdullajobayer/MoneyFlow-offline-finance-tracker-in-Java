package com.example.personalfinanceexpensetrackerandroidapp.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.TransactionType;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityTransactionHistoryBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.TransactionAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private ActivityTransactionHistoryBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter adapter;

    private FinanceRepository.TransactionFilter currentFilter = new FinanceRepository.TransactionFilter();

    private List<Owner> ownersList = new ArrayList<>();
    private List<Account> accountsList = new ArrayList<>();
    private List<TransactionType> transactionTypesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
            if (binding != null && binding.getRoot() != null) {
                setContentView(binding.getRoot());
            } else {
                Toast.makeText(this, "Error inflating layout", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Transaction History");
                }
                binding.toolbar.setNavigationOnClickListener(v -> finish());

                binding.toolbar.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.action_filter) {
                        showFilterDialog();
                        return true;
                    }
                    return false;
                });
            }

            viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
            if (viewModel == null) {
                Toast.makeText(this, "Error initializing ViewModel", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            adapter = new TransactionAdapter(
                    new ArrayList<>(),
                    tx -> {
                        if (tx != null) {
                            Intent intent = new Intent(this, AddEditTransactionActivity.class);
                            intent.putExtra("transaction_id", tx.getId());
                            startActivity(intent);
                        }
                    },
                    this::confirmDelete
            );

            if (binding.recyclerView != null) {
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                binding.recyclerView.setAdapter(adapter);
            }

            if (binding.fabAdd != null) {
                binding.fabAdd.setOnClickListener(v -> {
                    startActivity(new Intent(this, AddEditTransactionActivity.class));
                });
            }

            if (binding.etSearch != null) {
                binding.etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        String query = s != null ? s.toString().trim() : "";
                        if (currentFilter == null) {
                            currentFilter = new FinanceRepository.TransactionFilter();
                        }
                        currentFilter = currentFilter.setSearchQuery(query);
                        if (viewModel != null) {
                            viewModel.refreshTransactions(currentFilter);
                        }
                    }
                });
            }

            viewModel.getTransactions().observe(this, transactions -> {
                if (transactions != null && adapter != null) {
                    adapter.updateData(transactions);
                }
            });

            viewModel.getOwners().observe(this, owners -> {
                ownersList = owners != null ? owners : new ArrayList<>();
            });
            viewModel.getAccounts().observe(this, accounts -> {
                accountsList = accounts != null ? accounts : new ArrayList<>();
            });
            viewModel.getTransactionTypes().observe(this, types -> {
                transactionTypesList = types != null ? types : new ArrayList<>();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error starting transaction history: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (viewModel != null) {
                if (currentFilter == null) {
                    currentFilter = new FinanceRepository.TransactionFilter();
                }
                viewModel.refreshTransactions(currentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFilterDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_filter, null);
            if (dialogView == null) return;

            AutoCompleteTextView actvOwner = dialogView.findViewById(R.id.actvFilterOwner);
            AutoCompleteTextView actvAccount = dialogView.findViewById(R.id.actvFilterAccount);
            AutoCompleteTextView actvType = dialogView.findViewById(R.id.actvFilterType);

            List<String> ownerNames = new ArrayList<>();
            ownerNames.add("All Owners");
            if (ownersList != null) {
                for (Owner o : ownersList) {
                    if (o != null && o.getName() != null) ownerNames.add(o.getName());
                }
            }
            ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ownerNames);
            if (actvOwner != null) actvOwner.setAdapter(ownerAdapter);

            List<String> accountNames = new ArrayList<>();
            accountNames.add("All Accounts");
            if (accountsList != null) {
                for (Account a : accountsList) {
                    if (a != null && a.getName() != null) accountNames.add(a.getName());
                }
            }
            ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountNames);
            if (actvAccount != null) actvAccount.setAdapter(accountAdapter);

            List<String> typeNames = new ArrayList<>();
            typeNames.add("All Types");
            if (transactionTypesList != null) {
                for (TransactionType t : transactionTypesList) {
                    if (t != null && t.getName() != null) typeNames.add(t.getName());
                }
            }
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, typeNames);
            if (actvType != null) actvType.setAdapter(typeAdapter);

            if (currentFilter != null && currentFilter.getOwnerId() != null && actvOwner != null) {
                Owner o = null;
                if (ownersList != null) {
                    for (Owner owner : ownersList) {
                        if (owner != null && owner.getId() == currentFilter.getOwnerId()) {
                            o = owner;
                            break;
                        }
                    }
                }
                if (o != null && o.getName() != null) {
                    actvOwner.setText(o.getName(), false);
                } else {
                    actvOwner.setText("All Owners", false);
                }
            } else if (actvOwner != null) {
                actvOwner.setText("All Owners", false);
            }

            if (currentFilter != null && currentFilter.getAccountId() != null && actvAccount != null) {
                Account a = null;
                if (accountsList != null) {
                    for (Account acc : accountsList) {
                        if (acc != null && acc.getId() == currentFilter.getAccountId()) {
                            a = acc;
                            break;
                        }
                    }
                }
                if (a != null && a.getName() != null) {
                    actvAccount.setText(a.getName(), false);
                } else {
                    actvAccount.setText("All Accounts", false);
                }
            } else if (actvAccount != null) {
                actvAccount.setText("All Accounts", false);
            }

            if (currentFilter != null && currentFilter.getTransactionTypeId() != null && actvType != null) {
                TransactionType t = null;
                if (transactionTypesList != null) {
                    for (TransactionType type : transactionTypesList) {
                        if (type != null && type.getId() == currentFilter.getTransactionTypeId()) {
                            t = type;
                            break;
                        }
                    }
                }
                if (t != null && t.getName() != null) {
                    actvType.setText(t.getName(), false);
                } else {
                    actvType.setText("All Types", false);
                }
            } else if (actvType != null) {
                actvType.setText("All Types", false);
            }

            final Long[] selectedOwnerId = {currentFilter != null ? currentFilter.getOwnerId() : null};
            final Long[] selectedAccountId = {currentFilter != null ? currentFilter.getAccountId() : null};
            final Long[] selectedTypeId = {currentFilter != null ? currentFilter.getTransactionTypeId() : null};

            if (actvOwner != null) {
                actvOwner.setOnItemClickListener((parent, view, position, id) -> {
                    if (position == 0 || ownersList == null || (position - 1) >= ownersList.size() || (position - 1) < 0) {
                        selectedOwnerId[0] = null;
                    } else {
                        Owner owner = ownersList.get(position - 1);
                        selectedOwnerId[0] = owner != null ? owner.getId() : null;
                    }
                });
            }
            if (actvAccount != null) {
                actvAccount.setOnItemClickListener((parent, view, position, id) -> {
                    if (position == 0 || accountsList == null || (position - 1) >= accountsList.size() || (position - 1) < 0) {
                        selectedAccountId[0] = null;
                    } else {
                        Account acc = accountsList.get(position - 1);
                        selectedAccountId[0] = acc != null ? acc.getId() : null;
                    }
                });
            }
            if (actvType != null) {
                actvType.setOnItemClickListener((parent, view, position, id) -> {
                    if (position == 0 || transactionTypesList == null || (position - 1) >= transactionTypesList.size() || (position - 1) < 0) {
                        selectedTypeId[0] = null;
                    } else {
                        TransactionType type = transactionTypesList.get(position - 1);
                        selectedTypeId[0] = type != null ? type.getId() : null;
                    }
                });
            }

            new AlertDialog.Builder(this)
                    .setTitle("Filter Transactions")
                    .setView(dialogView)
                    .setPositiveButton("Apply", (dialog, which) -> {
                        if (currentFilter == null) {
                            currentFilter = new FinanceRepository.TransactionFilter();
                        }
                        currentFilter = currentFilter.setFilters(selectedOwnerId[0], selectedAccountId[0], null, selectedTypeId[0]);
                        if (viewModel != null) {
                            viewModel.refreshTransactions(currentFilter);
                        }
                    })
                    .setNeutralButton("Clear Filters", (dialog, which) -> {
                        currentFilter = new FinanceRepository.TransactionFilter(null, null, null, null, null, null, currentFilter != null ? currentFilter.getSearchQuery() : null);
                        if (viewModel != null) {
                            viewModel.refreshTransactions(currentFilter);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing filter dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void confirmDelete(Transaction tx) {
        if (tx == null) return;
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction? Account balances will be automatically adjusted.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (viewModel != null) {
                            viewModel.softDeleteTransaction(tx.getId(), success -> runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(this, "Transaction deleted & balances adjusted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
