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
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Category;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityManagementBinding;
import com.example.personalfinanceexpensetrackerandroidapp.ui.adapter.CategoryAdapter;
import com.example.personalfinanceexpensetrackerandroidapp.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {
    private ActivityManagementBinding binding;
    private FinanceViewModel viewModel;
    private CategoryAdapter adapter;
    private List<Owner> ownersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Categories");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        adapter = new CategoryAdapter(new ArrayList<>(), this::showEditDialog, this::confirmDelete);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setText("Add Category");
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                adapter.updateData(categories);
            }
        });

        viewModel.getOwners().observe(this, owners -> {
            if (owners != null) {
                ownersList = owners;
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        Spinner spinnerOwner = dialogView.findViewById(R.id.spinnerOwner);
        etType.setText("OUT");

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
                .setTitle("Add New Category")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    if (type.isEmpty()) type = "OUT";

                    int pos = spinnerOwner.getSelectedItemPosition();
                    long ownerId = 0L;
                    if (pos > 0 && pos - 1 < activeOwners.size()) {
                        ownerId = activeOwners.get(pos - 1).getId();
                    }

                    if (!name.isEmpty()) {
                        viewModel.addCategory(name, type, null, ownerId, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Category category) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etType = dialogView.findViewById(R.id.etType);
        Spinner spinnerOwner = dialogView.findViewById(R.id.spinnerOwner);

        etName.setText(category.getName());
        etType.setText(category.getType());

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
        if (category.getOwnerId() > 0) {
            for (int i = 0; i < activeOwners.size(); i++) {
                if (activeOwners.get(i).getId() == category.getOwnerId()) {
                    selectPos = i + 1;
                    break;
                }
            }
        }
        spinnerOwner.setSelection(selectPos);

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = etType.getText().toString().trim();

                    int pos = spinnerOwner.getSelectedItemPosition();
                    long ownerId = 0L;
                    if (pos > 0 && pos - 1 < activeOwners.size()) {
                        ownerId = activeOwners.get(pos - 1).getId();
                    }

                    if (!name.isEmpty()) {
                        Category updated = new Category(category.getId(), name, type, category.getIcon(), ownerId, category.isActive());
                        viewModel.updateCategory(updated, success -> runOnUiThread(() -> {
                            if (success) Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show();
                        }));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'? If referenced by transactions, it will be safely deactivated.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    viewModel.deleteOrDeactivateCategory(category.getId(), result -> runOnUiThread(() -> {
                        if (result == FinanceRepository.ResultType.DELETED) {
                            Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Category deactivated (referenced by transactions)", Toast.LENGTH_LONG).show();
                        }
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
