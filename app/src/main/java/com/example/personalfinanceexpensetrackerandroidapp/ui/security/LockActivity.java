package com.example.personalfinanceexpensetrackerandroidapp.ui.security;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.example.personalfinanceexpensetrackerandroidapp.databinding.ActivityLockBinding;
import com.example.personalfinanceexpensetrackerandroidapp.util.SecurityUtils;
import java.util.concurrent.Executor;

public class LockActivity extends AppCompatActivity {
    private ActivityLockBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityLockBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setupUI();

            if (SecurityUtils.isBiometricEnabled(this)) {
                showBiometricPrompt();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing lock screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupUI() {
        binding.btnUnlockPin.setOnClickListener(v -> {
            String enteredPin = binding.etPin.getText() != null ? binding.etPin.getText().toString().trim() : "";
            String savedPin = SecurityUtils.getSavedPin(this);

            if (savedPin.isEmpty()) {
                if (enteredPin.length() >= 4) {
                    SecurityUtils.savePin(this, enteredPin);
                    SecurityUtils.setUnlockedInSession(true);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Please enter at least 4 digits for your new PIN", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (enteredPin.equals(savedPin)) {
                    SecurityUtils.setUnlockedInSession(true);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (SecurityUtils.isBiometricEnabled(this)) {
            binding.btnBiometric.setVisibility(View.VISIBLE);
            binding.btnBiometric.setOnClickListener(v -> showBiometricPrompt());
        } else {
            binding.btnBiometric.setVisibility(View.GONE);
        }
    }

    private void showBiometricPrompt() {
        try {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SecurityUtils.setUnlockedInSession(true);
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("MoneyFlow Biometric Authentication")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Use PIN")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
