package com.example.personalfinanceexpensetrackerandroidapp.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class ExportUtils {
    private static final String TAG = "ExportUtils";

    public static boolean exportToCsv(Context context, List<Transaction> transactions, Uri uri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {

            if (writer == null) return false;

            // CSV Header
            writer.append("ID,Date,Owner,Account,Type,Category,IncomeSource,PaymentMethod,Amount,PayeePayer,Note,Recurring\n");

            for (Transaction tx : transactions) {
                writer.append(String.valueOf(tx.getId())).append(",");
                writer.append(escapeCsv(tx.getDate())).append(",");
                writer.append(escapeCsv(tx.getOwnerName())).append(",");
                writer.append(escapeCsv(tx.getAccountName())).append(",");
                writer.append(escapeCsv(tx.getTransactionTypeName())).append(",");
                writer.append(escapeCsv(tx.getCategoryName())).append(",");
                writer.append(escapeCsv(tx.getIncomeSourceName())).append(",");
                writer.append(escapeCsv(tx.getPaymentMethodName())).append(",");
                writer.append(String.format(Locale.US, "%.2f", tx.getAmount() / 100.0)).append(",");
                writer.append(escapeCsv(tx.getPayeePayer())).append(",");
                writer.append(escapeCsv(tx.getNote())).append(",");
                writer.append(tx.isRecurring() ? "Yes" : "No");
                writer.append("\n");
            }
            writer.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error exporting CSV", e);
            return false;
        }
    }

    public static boolean exportToExcel(Context context, List<Transaction> transactions, Uri uri) {
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {

            if (outputStream == null) return false;

            Sheet sheet = workbook.createSheet("Transactions");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Date", "Owner", "Account", "Type", "Category", "Income Source", "Payment Method", "Amount", "Payee/Payer", "Note", "Recurring"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Transaction tx : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tx.getId());
                row.createCell(1).setCellValue(tx.getDate() != null ? tx.getDate() : "");
                row.createCell(2).setCellValue(tx.getOwnerName() != null ? tx.getOwnerName() : "");
                row.createCell(3).setCellValue(tx.getAccountName() != null ? tx.getAccountName() : "");
                row.createCell(4).setCellValue(tx.getTransactionTypeName() != null ? tx.getTransactionTypeName() : "");
                row.createCell(5).setCellValue(tx.getCategoryName() != null ? tx.getCategoryName() : "");
                row.createCell(6).setCellValue(tx.getIncomeSourceName() != null ? tx.getIncomeSourceName() : "");
                row.createCell(7).setCellValue(tx.getPaymentMethodName() != null ? tx.getPaymentMethodName() : "");
                row.createCell(8).setCellValue(tx.getAmount() / 100.0);
                row.createCell(9).setCellValue(tx.getPayeePayer() != null ? tx.getPayeePayer() : "");
                row.createCell(10).setCellValue(tx.getNote() != null ? tx.getNote() : "");
                row.createCell(11).setCellValue(tx.isRecurring() ? "Yes" : "No");
            }

            workbook.write(outputStream);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error exporting Excel", e);
            return false;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
