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
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityManagementBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.OwnerAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;

public class OwnerManagementActivity extends AppCompatActivity {
    private ActivityManagementBinding binding;
    private FinanceViewModel viewModel;
    private OwnerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Owners");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new OwnerAdapter(new ArrayList<>(), this::showEditDialog, this::confirmDelete);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add Owner");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                adapter.updateData(owners);
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_owner, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);

        new AlertDialog.Builder(this)
                .setTitle("Add New Owner")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    if (type.isEmpty()) type = "PERSONAL";

                    if (!name.isEmpty()) {
                        viewModel.addOwner(name, type, false, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Owner added successfully", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to add owner", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Owner owner) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_owner, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        etName.setText(owner.getName());
        etType.setText(owner.getType());

        new AlertDialog.Builder(this)
                .setTitle("Edit Owner")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();

                    if (!name.isEmpty()) {
                        Owner updated = new Owner(owner.getId(), name, type, owner.isDefault(), owner.isActive());
                        viewModel.updateOwner(updated, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Owner updated", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to update owner", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Owner owner) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Owner")
                .setMessage("Are you sure you want to delete '" + owner.getName() + "'? If referenced by transactions, it will be safely deactivated.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteOrDeactivateOwner(owner.getId(), result -> runOnUiThread(() -> {
                        if (result == FinanceRepository.ResultType.DELETED) {
                            Toast.makeText(this, "Owner deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Owner deactivated (referenced by transactions)", Toast.LENGTH_LONG).show();
                        }
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
