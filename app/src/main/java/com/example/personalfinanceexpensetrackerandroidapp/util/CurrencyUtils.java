package com.example.personalfinanceexpensetrackerandroidapp.util;

import android.content.Context;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;
import java.util.Locale;

public class CurrencyUtils {
    public static String getCurrencySymbol(Context context) {
        try {
            FinanceRepository repo = new FinanceRepository(context);
            String symbol = repo.getSetting("currency_symbol", "$");
            return symbol != null && !symbol.isEmpty() ? symbol : "$";
        } catch (Exception e) {
            return "$";
        }
    }

    public static void setCurrencySymbol(Context context, String symbol) {
        try {
            FinanceRepository repo = new FinanceRepository(context);
            repo.saveSetting("currency_symbol", symbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String format(Context context, double amount) {
        String symbol = getCurrencySymbol(context);
        return symbol + String.format(Locale.getDefault(), "%,.2f", amount);
    }

    public static String formatCents(Context context, long cents) {
        return format(context, cents / 100.0);
    }

    public static String formatAmount(Context context, long minorUnits) {
        return format(context, minorUnits / 100.0);
    }

    public static String formatAmount(Context context, double amount) {
        return format(context, amount);
    }

    public static String formatAmount(long minorUnits) {
        return String.format(Locale.getDefault(), "%,.2f", minorUnits / 100.0);
    }

    public static String formatAmount(double amount) {
        return String.format(Locale.getDefault(), "%,.2f", amount);
    }
}
