package com.example.personalfinanceexpensetrackerandroidapp.ui.management;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.IncomeSource;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityManagementBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.IncomeSourceAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class IncomeSourceManagementActivity extends AppCompatActivity {
    private ActivityManagementBinding binding;
    private FinanceViewModel viewModel;
    private IncomeSourceAdapter adapter;
    private List<Owner> ownersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Income Sources");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new IncomeSourceAdapter(new ArrayList<>(), this::showEditDialog, this::confirmDelete);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add Source");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getIncomeSources().observe(this, sources -> {
            if (sources != null) {
                adapter.updateData(sources);
            }
        });

        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                ownersList = owners;
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_income_source, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerOwner = dialogView.findViewById(R.id.spinnerOwner);

        List<Owner> activeOwners = new ArrayList<>();
        for (Owner o : ownersList) {
            if (o.isActive()) activeOwners.add(o);
        }

        List<String> ownerNames = new ArrayList<>();
        ownerNames.add("Global");
        for (Owner o : activeOwners) {
            ownerNames.add(o.getName() + " (" + o.getType() + ")");
        }
        ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ownerNames);
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOwner.setAdapter(ownerAdapter);

        int defaultPos = 0;
        for (int i = 0; i < activeOwners.size(); i++) {
            if (activeOwners.get(i).isDefault()) {
                defaultPos = i + 1;
                break;
            }
        }
        if (defaultPos == 0 && !activeOwners.isEmpty()) {
            defaultPos = 1;
        }
        spinnerOwner.setSelection(defaultPos);

        new AlertDialog.Builder(this)
                .setTitle("Add Income Source")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();

                    int pos = spinnerOwner.getSelectedItemPosition();
                    long ownerId = 0L;
                    if (pos > 0 && pos - 1 < activeOwners.size()) {
                        ownerId = activeOwners.get(pos - 1).getId();
                    }

                    if (!name.isEmpty()) {
                        viewModel.addIncomeSource(name, ownerId, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Income source added", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to add income source", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(IncomeSource source) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_income_source, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerOwner = dialogView.findViewById(R.id.spinnerOwner);

        etName.setText(source.getName());

        List<Owner> activeOwners = new ArrayList<>();
        for (Owner o : ownersList) {
            if (o.isActive()) activeOwners.add(o);
        }

        List<String> ownerNames = new ArrayList<>();
        ownerNames.add("Global");
        for (Owner o : activeOwners) {
            ownerNames.add(o.getName() + " (" + o.getType() + ")");
        }
        ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ownerNames);
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOwner.setAdapter(ownerAdapter);

        int selectPos = 0;
        if (source.getOwnerId() > 0) {
            for (int i = 0; i < activeOwners.size(); i++) {
                if (activeOwners.get(i).getId() == source.getOwnerId()) {
                    selectPos = i + 1;
                    break;
                }
            }
        }
        spinnerOwner.setSelection(selectPos);

        new AlertDialog.Builder(this)
                .setTitle("Edit Income Source")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();

                    int pos = spinnerOwner.getSelectedItemPosition();
                    long ownerId = 0L;
                    if (pos > 0 && pos - 1 < activeOwners.size()) {
                        ownerId = activeOwners.get(pos - 1).getId();
                    }

                    if (!name.isEmpty()) {
                        IncomeSource updated = new IncomeSource(source.getId(), name, ownerId, source.isActive());
                        viewModel.updateIncomeSource(updated, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Income source updated", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to update income source", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(IncomeSource source) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Income Source")
                .setMessage("Are you sure you want to delete '" + source.getName() + "'? If referenced by transactions, it will be safely deactivated.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteOrDeactivateIncomeSource(source.getId(), result -> runOnUiThread(() -> {
                        if (result == FinanceRepository.ResultType.DELETED) {
                            Toast.makeText(this, "Income source deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Income source deactivated (referenced by transactions)", Toast.LENGTH_LONG).show();
                        }
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
