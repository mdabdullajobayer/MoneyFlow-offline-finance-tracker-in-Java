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
import com.example.personalfinanceexpensetrackerandroidapp.data.model.PaymentMethod;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityManagementBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.PaymentMethodAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;

public class PaymentMethodManagementActivity extends AppCompatActivity {
    private ActivityManagementBinding binding;
    private FinanceViewModel viewModel;
    private PaymentMethodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Payment Methods");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new PaymentMethodAdapter(new ArrayList<>(), this::showEditDialog, this::confirmDelete);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add Method");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getPaymentMethods().observe(this, methods -> {
            if (methods != null) {
                adapter.updateData(methods);
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_payment_method, null);
        EditText etName = dialogView.findViewById(R.id.etName);

        new AlertDialog.Builder(this)
                .setTitle("Add Payment Method")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.addPaymentMethod(name, null, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Payment method added", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to add payment method", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(PaymentMethod method) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_payment_method, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        etName.setText(method.getName());

        new AlertDialog.Builder(this)
                .setTitle("Edit Payment Method")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        PaymentMethod updated = new PaymentMethod(method.getId(), name, method.getIcon(), method.isActive());
                        viewModel.updatePaymentMethod(updated, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Payment method updated", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to update payment method", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(PaymentMethod method) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Payment Method")
                .setMessage("Are you sure you want to delete '" + method.getName() + "'? If referenced by transactions, it will be safely deactivated.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteOrDeactivatePaymentMethod(method.getId(), result -> runOnUiThread(() -> {
                        if (result == FinanceRepository.ResultType.DELETED) {
                            Toast.makeText(this, "Payment method deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Payment method deactivated (referenced by transactions)", Toast.LENGTH_LONG).show();
                        }
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
