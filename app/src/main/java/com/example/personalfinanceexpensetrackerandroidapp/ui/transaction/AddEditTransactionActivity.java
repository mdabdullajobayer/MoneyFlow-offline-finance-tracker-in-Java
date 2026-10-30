package com.example.personalfinanceexpensetrackerandroidapp.ui.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.*;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityAddEditTransactionBinding;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEditTransactionActivity extends AppCompatActivity {
    private ActivityAddEditTransactionBinding binding;
    private FinanceViewModel viewModel;

    private List<Owner> ownersList = new ArrayList<>();
    private List<Account> accountsList = new ArrayList<>();
    private List<Category> categoriesList = new ArrayList<>();
    private List<IncomeSource> incomeSourcesList = new ArrayList<>();
    private List<PaymentMethod> paymentMethodsList = new ArrayList<>();
    private List<TransactionType> transactionTypesList = new ArrayList<>();

    private long selectedOwnerId = -1L;
    private long selectedAccountId = -1L;
    private Long selectedDestinationAccountId = null;
    private long selectedTransactionTypeId = -1L;
    private Long selectedCategoryId = null;
    private Long selectedIncomeSourceId = null;
    private long selectedPaymentMethodId = -1L;

    private long editingTransactionId = -1L;
    private Transaction editingTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Transaction");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        editingTransactionId = getIntent().getLongExtra("transaction_id", -1L);
        if (editingTransactionId != -1L) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Transaction");
            }
            binding.btnSave.setText("Update Transaction");
        }

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        observeViewModelData();

        if (editingTransactionId != -1L) {
            viewModel.getTransactions().observe(this, txs -> {
                if (txs != null && editingTransaction == null) {
                    for (Transaction t : txs) {
                        if (t.getId() == editingTransactionId) {
                            editingTransaction = t;
                            populateTransactionFields(t);
                            break;
                        }
                    }
                }
            });
            viewModel.refreshTransactions(null);
        }

        binding.btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void populateTransactionFields(Transaction t) {
        selectedOwnerId = t.getOwnerId();
        selectedAccountId = t.getAccountId();
        selectedDestinationAccountId = t.getDestinationAccountId();
        selectedTransactionTypeId = t.getTransactionTypeId();
        selectedCategoryId = t.getCategoryId();
        selectedIncomeSourceId = t.getIncomeSourceId();
        selectedPaymentMethodId = t.getPaymentMethodId();

        binding.etAmount.setText(String.format(Locale.getDefault(), "%.2f", t.getAmount() / 100.0));
        if (t.getPayeePayer() != null) binding.etPayeePayer.setText(t.getPayeePayer());
        if (t.getNote() != null) binding.etNote.setText(t.getNote());

        updateOwnerSelection();
        updateAccountSelection();
        updateTransactionTypeSelection();
        updatePaymentMethodSelection();
        updateIncomeSourceSelection();
    }

    private void updateOwnerSelection() {
        if (editingTransactionId != -1L && !ownersList.isEmpty()) {
            for (Owner o : ownersList) {
                if (o.getId() == selectedOwnerId) {
                    binding.actvOwner.setText(o.getName(), false);
                    filterCategoriesForOwnerAndType();
                    break;
                }
            }
        }
    }

    private void updateAccountSelection() {
        if (editingTransactionId != -1L && !accountsList.isEmpty()) {
            for (Account a : accountsList) {
                if (a.getId() == selectedAccountId) {
                    binding.actvAccount.setText(a.getName() + " (" + a.getCurrency() + " " + a.getBalance() + ")", false);
                    break;
                }
            }
            if (selectedDestinationAccountId != null) {
                for (Account a : accountsList) {
                    if (a.getId() == selectedDestinationAccountId) {
                        binding.actvDestinationAccount.setText(a.getName() + " (" + a.getCurrency() + " " + a.getBalance() + ")", false);
                        break;
                    }
                }
            }
        }
    }

    private void updateTransactionTypeSelection() {
        if (editingTransactionId != -1L && !transactionTypesList.isEmpty()) {
            for (TransactionType type : transactionTypesList) {
                if (type.getId() == selectedTransactionTypeId) {
                    binding.actvTransactionType.setText(type.getName(), false);
                    updateFieldVisibilityForType(type.getName());
                    filterCategoriesForOwnerAndType();
                    break;
                }
            }
        }
    }

    private void updatePaymentMethodSelection() {
        if (editingTransactionId != -1L && !paymentMethodsList.isEmpty()) {
            for (PaymentMethod m : paymentMethodsList) {
                if (m.getId() == selectedPaymentMethodId) {
                    binding.actvPaymentMethod.setText(m.getName(), false);
                    break;
                }
            }
        }
    }

    private void updateIncomeSourceSelection() {
        if (editingTransactionId != -1L && selectedIncomeSourceId != null && !incomeSourcesList.isEmpty()) {
            for (IncomeSource s : incomeSourcesList) {
                if (s.getId() == selectedIncomeSourceId) {
                    binding.actvIncomeSource.setText(s.getName(), false);
                    break;
                }
            }
        }
    }

    private void observeViewModelData() {
        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                ownersList = new ArrayList<>();
                for (Owner o : owners) {
                    if (o.isActive()) ownersList.add(o);
                }
                List<String> names = new ArrayList<>();
                for (Owner o : ownersList) names.add(o.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                binding.actvOwner.setAdapter(adapter);
                binding.actvOwner.setOnItemClickListener((parent, view, position, id) -> {
                    selectedOwnerId = ownersList.get(position).getId();
                    filterCategoriesForOwnerAndType();
                });
                if (editingTransactionId != -1L) {
                    updateOwnerSelection();
                } else if (!ownersList.isEmpty() && selectedOwnerId == -1L) {
                    Owner defOwner = null;
                    for (Owner o : ownersList) {
                        if (o.isDefault()) {
                            defOwner = o;
                            break;
                        }
                    }
                    if (defOwner == null) defOwner = ownersList.get(0);
                    selectedOwnerId = defOwner.getId();
                    binding.actvOwner.setText(defOwner.getName(), false);
                    filterCategoriesForOwnerAndType();
                }
            }
        });

        viewModel.getAccounts().observe(this, accounts -> {
            if (accounts != null) {
                accountsList = new ArrayList<>();
                for (Account a : accounts) {
                    if (a.isActive()) accountsList.add(a);
                }
                List<String> names = new ArrayList<>();
                for (Account a : accountsList) {
                    names.add(a.getName() + " (" + a.getCurrency() + " " + a.getBalance() + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                binding.actvAccount.setAdapter(adapter);
                binding.actvAccount.setOnItemClickListener((parent, view, position, id) -> {
                    selectedAccountId = accountsList.get(position).getId();
                });
                binding.actvDestinationAccount.setAdapter(adapter);
                binding.actvDestinationAccount.setOnItemClickListener((parent, view, position, id) -> {
                    selectedDestinationAccountId = accountsList.get(position).getId();
                });
                if (editingTransactionId != -1L) {
                    updateAccountSelection();
                } else if (!accountsList.isEmpty() && selectedAccountId == -1L) {
                    selectedAccountId = accountsList.get(0).getId();
                    binding.actvAccount.setText(accountsList.get(0).getName() + " (" + accountsList.get(0).getCurrency() + " " + accountsList.get(0).getBalance() + ")", false);
                }
            }
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoriesList = new ArrayList<>();
                for (Category c : categories) {
                    if (c.isActive()) categoriesList.add(c);
                }
                filterCategoriesForOwnerAndType();
                if (editingTransactionId != -1L && selectedCategoryId != null) {
                    for (Category c : categoriesList) {
                        if (c.getId() == selectedCategoryId) {
                            binding.actvCategory.setText(c.getName(), false);
                            break;
                        }
                    }
                }
            }
        });

        viewModel.getIncomeSources().observe(this, sources -> {
            if (sources != null) {
                incomeSourcesList = new ArrayList<>();
                for (IncomeSource s : sources) {
                    if (s.isActive()) incomeSourcesList.add(s);
                }
                List<String> names = new ArrayList<>();
                for (IncomeSource s : incomeSourcesList) names.add(s.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                binding.actvIncomeSource.setAdapter(adapter);
                binding.actvIncomeSource.setOnItemClickListener((parent, view, position, id) -> {
                    selectedIncomeSourceId = incomeSourcesList.get(position).getId();
                });
                if (editingTransactionId != -1L) {
                    updateIncomeSourceSelection();
                }
            }
        });

        viewModel.getPaymentMethods().observe(this, methods -> {
            if (methods != null) {
                paymentMethodsList = new ArrayList<>();
                for (PaymentMethod m : methods) {
                    if (m.isActive()) paymentMethodsList.add(m);
                }
                List<String> names = new ArrayList<>();
                for (PaymentMethod m : paymentMethodsList) names.add(m.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                binding.actvPaymentMethod.setAdapter(adapter);
                binding.actvPaymentMethod.setOnItemClickListener((parent, view, position, id) -> {
                    selectedPaymentMethodId = paymentMethodsList.get(position).getId();
                });
                if (editingTransactionId != -1L) {
                    updatePaymentMethodSelection();
                } else if (!paymentMethodsList.isEmpty() && selectedPaymentMethodId == -1L) {
                    selectedPaymentMethodId = paymentMethodsList.get(0).getId();
                    binding.actvPaymentMethod.setText(paymentMethodsList.get(0).getName(), false);
                }
            }
        });

        viewModel.getTransactionTypes().observe(this, types -> {
            if (types != null) {
                transactionTypesList = new ArrayList<>();
                for (TransactionType t : types) {
                    if (t.isActive()) transactionTypesList.add(t);
                }
                List<String> names = new ArrayList<>();
                for (TransactionType t : transactionTypesList) names.add(t.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                binding.actvTransactionType.setAdapter(adapter);
                binding.actvTransactionType.setOnItemClickListener((parent, view, position, id) -> {
                    TransactionType typeObj = transactionTypesList.get(position);
                    selectedTransactionTypeId = typeObj.getId();
                    updateFieldVisibilityForType(typeObj.getName());
                    filterCategoriesForOwnerAndType();
                });
                if (editingTransactionId != -1L) {
                    updateTransactionTypeSelection();
                } else if (!transactionTypesList.isEmpty() && selectedTransactionTypeId == -1L) {
                    TransactionType outType = null;
                    for (TransactionType t : transactionTypesList) {
                        if ("OUT".equals(t.getName())) {
                            outType = t;
                            break;
                        }
                    }
                    if (outType == null) outType = transactionTypesList.get(0);
                    selectedTransactionTypeId = outType.getId();
                    binding.actvTransactionType.setText(outType.getName(), false);
                    updateFieldVisibilityForType(outType.getName());
                }
            }
        });
    }

    private void updateFieldVisibilityForType(String typeName) {
        if ("IN".equals(typeName)) {
            binding.tilDestinationAccount.setVisibility(View.GONE);
            binding.tilCategory.setVisibility(View.VISIBLE);
            binding.tilIncomeSource.setVisibility(View.VISIBLE);
            selectedDestinationAccountId = null;
        } else if ("OUT".equals(typeName)) {
            binding.tilDestinationAccount.setVisibility(View.GONE);
            binding.tilCategory.setVisibility(View.VISIBLE);
            binding.tilIncomeSource.setVisibility(View.GONE);
            selectedDestinationAccountId = null;
            selectedIncomeSourceId = null;
        } else if ("TRANSFER".equals(typeName)) {
            binding.tilDestinationAccount.setVisibility(View.VISIBLE);
            binding.tilCategory.setVisibility(View.GONE);
            binding.tilIncomeSource.setVisibility(View.GONE);
            selectedCategoryId = null;
            selectedIncomeSourceId = null;
        }
    }

    private void filterCategoriesForOwnerAndType() {
        TransactionType currentTypeObj = null;
        for (TransactionType t : transactionTypesList) {
            if (t.getId() == selectedTransactionTypeId) {
                currentTypeObj = t;
                break;
            }
        }
        String typeName = currentTypeObj != null ? currentTypeObj.getName() : "OUT";

        List<Category> filtered = new ArrayList<>();
        for (Category cat : categoriesList) {
            boolean matchOwner = (selectedOwnerId == -1L || cat.getOwnerId() == selectedOwnerId);
            boolean matchType = ("TRANSFER".equals(typeName) || cat.getType().equals(typeName));
            if (matchOwner && matchType) {
                filtered.add(cat);
            }
        }

        List<String> names = new ArrayList<>();
        for (Category c : filtered) names.add(c.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        binding.actvCategory.setAdapter(adapter);
        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (position < filtered.size()) {
                selectedCategoryId = filtered.get(position).getId();
            }
        });
        if (!filtered.isEmpty()) {
            selectedCategoryId = filtered.get(0).getId();
            binding.actvCategory.setText(filtered.get(0).getName(), false);
        } else {
            selectedCategoryId = null;
            binding.actvCategory.setText("", false);
        }
    }

    private void saveTransaction() {
        String amountStr = binding.etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        Double majorAmount = null;
        try {
            majorAmount = Double.parseDouble(amountStr);
        } catch (Exception ignored) {}

        if (majorAmount == null || majorAmount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        long minorAmount = (long) (majorAmount * 100);

        TransactionType currentTypeObj = null;
        for (TransactionType t : transactionTypesList) {
            if (t.getId() == selectedTransactionTypeId) {
                currentTypeObj = t;
                break;
            }
        }
        String typeName = currentTypeObj != null ? currentTypeObj.getName() : "OUT";

        if (selectedAccountId == -1L) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("TRANSFER".equals(typeName) && (selectedDestinationAccountId == null || selectedDestinationAccountId == -1L)) {
            Toast.makeText(this, "Please select a destination account for transfer", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("TRANSFER".equals(typeName) && selectedAccountId == selectedDestinationAccountId) {
            Toast.makeText(this, "Source and destination accounts cannot be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String note = binding.etNote.getText().toString().trim();
        String payeePayer = binding.etPayeePayer.getText().toString().trim();

        Transaction transaction = new Transaction(
                editingTransactionId != -1L ? editingTransactionId : 0L,
                selectedOwnerId != -1L ? selectedOwnerId : 1L,
                selectedAccountId,
                "TRANSFER".equals(typeName) ? selectedDestinationAccountId : null,
                selectedTransactionTypeId,
                !"TRANSFER".equals(typeName) ? selectedCategoryId : null,
                "IN".equals(typeName) ? selectedIncomeSourceId : null,
                selectedPaymentMethodId != -1L ? selectedPaymentMethodId : 1L,
                minorAmount,
                now,
                !note.isEmpty() ? note : null,
                !payeePayer.isEmpty() ? payeePayer : null,
                false,
                null,
                true,
                null, null, null, null, null, null, null
        );

        if (editingTransactionId != -1L) {
            viewModel.updateTransaction(transaction, success -> runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            viewModel.addTransaction(transaction, success -> runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }
}
