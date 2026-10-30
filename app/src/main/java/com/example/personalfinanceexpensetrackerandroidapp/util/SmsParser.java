package com.example.personalfinanceexpensetrackerandroidapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    public static class ParsedResult {
        public double amount;
        public String transactionType; // "IN", "OUT", or "TRANSFER"
        public String reference;
        public boolean success;
        public String sourceAccount;
        public String destinationAccount;
        public String categorySuggestion;
    }

    public static ParsedResult parse(String body) {
        ParsedResult result = new ParsedResult();
        result.success = false;
        result.amount = 0.0;
        result.transactionType = "OUT";
        result.reference = "";
        result.sourceAccount = null;
        result.destinationAccount = null;
        result.categorySuggestion = "Food & Dining";

        if (body == null || body.isEmpty()) return result;

        String lowerBody = body.toLowerCase();

        // Determine transaction type: IN, OUT, or TRANSFER
        if (lowerBody.contains("transfer") || lowerBody.contains("fund transfer") || lowerBody.contains("account deposit") || lowerBody.contains("self transfer") || (lowerBody.contains("from") && lowerBody.contains("to") && (lowerBody.contains("bank") || lowerBody.contains("bkash") || lowerBody.contains("account") || lowerBody.contains("wallet")))) {
            result.transactionType = "TRANSFER";
            result.categorySuggestion = "Transfer";
        } else if (lowerBody.contains("loan") && (lowerBody.contains("repayment") || lowerBody.contains("repay") || lowerBody.contains("repaid") || lowerBody.contains("installment") || lowerBody.contains("paid") || lowerBody.contains("deducted") || lowerBody.contains("emi") || lowerBody.contains("payment"))) {
            result.transactionType = "OUT";
            result.categorySuggestion = "Loan Repayment";
        } else if (lowerBody.contains("loan") && (lowerBody.contains("received") || lowerBody.contains("disbursed") || lowerBody.contains("credited") || lowerBody.contains("taking") || lowerBody.contains("approved"))) {
            result.transactionType = "IN";
            result.categorySuggestion = "Loan Taking";
        } else if (lowerBody.contains("received") || lowerBody.contains("cash in") || lowerBody.contains("credited") || lowerBody.contains("cr") || lowerBody.contains("salary") || lowerBody.contains("deposit") || lowerBody.contains("credit")) {
            if (lowerBody.contains("salary")) {
                result.transactionType = "IN";
                result.categorySuggestion = "Salary";
            } else {
                result.transactionType = "IN";
                result.categorySuggestion = "Salary Income";
            }
        } else if (lowerBody.contains("sent") || lowerBody.contains("payment") || lowerBody.contains("cash out") || lowerBody.contains("debited") || lowerBody.contains("dr") || lowerBody.contains("paid") || lowerBody.contains("charge") || lowerBody.contains("debit")) {
            result.transactionType = "OUT";
            result.categorySuggestion = "Food & Dining";
        } else {
            result.transactionType = "OUT";
            result.categorySuggestion = "Food & Dining";
        }

        // Extract source and destination accounts for transfers
        if ("TRANSFER".equals(result.transactionType)) {
            Pattern transferFromToPattern = Pattern.compile("from\\s+([A-Za-z0-9\\s]+?)\\s+to\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s+(?:ref|trxid|via|successful)|$)", Pattern.CASE_INSENSITIVE);
            Matcher transferMatcher = transferFromToPattern.matcher(body);
            if (transferMatcher.find()) {
                if (transferMatcher.group(1) != null) result.sourceAccount = transferMatcher.group(1).trim();
                if (transferMatcher.group(2) != null) result.destinationAccount = transferMatcher.group(2).trim();
            } else {
                Pattern transferToFromPattern = Pattern.compile("to\\s+([A-Za-z0-9\\s]+?)\\s+from\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s+(?:ref|trxid|via|successful)|$)", Pattern.CASE_INSENSITIVE);
                Matcher transferToFromMatcher = transferToFromPattern.matcher(body);
                if (transferToFromMatcher.find()) {
                    if (transferToFromMatcher.group(1) != null) result.destinationAccount = transferToFromMatcher.group(1).trim();
                    if (transferToFromMatcher.group(2) != null) result.sourceAccount = transferToFromMatcher.group(2).trim();
                } else {
                    Pattern transferToPattern = Pattern.compile("to\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s+(?:ref|trxid|via|successful)|$)", Pattern.CASE_INSENSITIVE);
                    Matcher transferToMatcher = transferToPattern.matcher(body);
                    if (transferToMatcher.find()) {
                        if (transferToMatcher.group(1) != null) result.destinationAccount = transferToMatcher.group(1).trim();
                    }
                }
            }
        }

        // Extract amount: match Tk / BDT / Rs / $ / ৳ / USD / EUR followed by number
        Pattern amountPattern = Pattern.compile("(?:tk|bdt|rs|\\$|৳|usd|eur|gbp)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(body);
        if (amountMatcher.find()) {
            String amtStr = amountMatcher.group(1).replace(",", "");
            try {
                result.amount = Double.parseDouble(amtStr);
                result.success = true;
            } catch (Exception ignored) {}
        } else {
            Pattern fallbackPattern = Pattern.compile("(?:amount|sum|tk|bdt|rs|\\$|৳|received|paid|deposit|loan|credit|debit)\\b[:\\s]*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE);
            Matcher fallbackMatcher = fallbackPattern.matcher(body);
            if (fallbackMatcher.find()) {
                String amtStr = fallbackMatcher.group(1).replace(",", "");
                try {
                    result.amount = Double.parseDouble(amtStr);
                    result.success = true;
                } catch (Exception ignored) {}
            } else {
                Pattern genericPattern = Pattern.compile("([\\d,]+\\.\\d{2})", Pattern.CASE_INSENSITIVE);
                Matcher genericMatcher = genericPattern.matcher(body);
                if (genericMatcher.find()) {
                    String amtStr = genericMatcher.group(1).replace(",", "");
                    try {
                        result.amount = Double.parseDouble(amtStr);
                        result.success = true;
                    } catch (Exception ignored) {}
                }
            }
        }

        // Extract Reference / TrxID
        Pattern refPattern = Pattern.compile("(?:trxid|trx\\s*id|ref|reference|id)[:\\s]*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher refMatcher = refPattern.matcher(body);
        if (refMatcher.find()) {
            result.reference = refMatcher.group(1);
        } else {
            result.reference = "SMS-" + (System.currentTimeMillis() % 100000);
        }

        return result;
    }
}
