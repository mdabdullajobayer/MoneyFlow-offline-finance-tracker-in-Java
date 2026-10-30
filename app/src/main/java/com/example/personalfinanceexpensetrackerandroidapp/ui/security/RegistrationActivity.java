package com.example.personalfinanceexpensetrackerandroidapp.ui.security;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityRegistrationBinding;
import com.example.personalfinanceexpensetrackerandroidapp.util.CurrencyUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.SecurityUtils;

public class RegistrationActivity extends AppCompatActivity {
    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupBackCallback();
    }

    private void setupBackCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SecurityUtils.isUserRegistered(RegistrationActivity.this)) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Please complete setup to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupUI() {
        String[] currencies = new String[]{"$ (USD)", "€ (EUR)", "৳ (BDT)", "£ (GBP)", "¥ (JPY/CNY)", "₹ (INR)", "A$ (AUD)", "C$ (CAD)", "CHF", "kr", "R$", "₩", "₦"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies);
        binding.actvCurrency.setAdapter(currencyAdapter);

        binding.btnGetStarted.setOnClickListener(v -> {
            String enteredName = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
            if (enteredName.isEmpty()) {
                enteredName = "Alex Morgan";
            }

            String currencySelection = binding.actvCurrency.getText().toString().trim();
            String symbol = currencySelection.contains(" ") ? currencySelection.substring(0, currencySelection.indexOf(" ")) : currencySelection;
            if (symbol.isEmpty()) {
                symbol = "$";
            }

            String pin = binding.etPin.getText() != null ? binding.etPin.getText().toString().trim() : "";

            SecurityUtils.setUserName(this, enteredName);
            CurrencyUtils.setCurrencySymbol(this, symbol);
            SecurityUtils.setUserRegistered(this, true);

            if (!pin.isEmpty() && pin.length() >= 4) {
                SecurityUtils.savePin(this, pin);
                SecurityUtils.setAppLockEnabled(this, true);
                SecurityUtils.setUnlockedInSession(true);
            }

            Toast.makeText(this, "Welcome to MoneyFlow, " + enteredName + "!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
