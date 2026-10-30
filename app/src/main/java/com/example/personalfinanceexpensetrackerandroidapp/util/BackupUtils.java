package com.example.personalfinanceexpensetrackerandroidapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.personalfinanceexpensetrackerandroidapp.data.db.MoneyFlowDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

public class BackupUtils {
    private static final String TAG = "BackupUtils";

    private static final String[] TABLES = {
            MoneyFlowDbHelper.TABLE_OWNERS,
            MoneyFlowDbHelper.TABLE_ACCOUNTS,
            MoneyFlowDbHelper.TABLE_TRANSACTION_TYPES,
            MoneyFlowDbHelper.TABLE_CATEGORIES,
            MoneyFlowDbHelper.TABLE_INCOME_SOURCES,
            MoneyFlowDbHelper.TABLE_PAYMENT_METHODS,
            MoneyFlowDbHelper.TABLE_TRANSACTIONS,
            MoneyFlowDbHelper.TABLE_SMS_SENDERS,
            MoneyFlowDbHelper.TABLE_SMS_LOGS,
            MoneyFlowDbHelper.TABLE_BUDGETS,
            MoneyFlowDbHelper.TABLE_APP_SETTINGS
    };

    public static boolean exportBackupJson(Context context, Uri uri) {
        MoneyFlowDbHelper dbHelper = new MoneyFlowDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            JSONObject root = new JSONObject();
            root.put("version", 1);
            root.put("timestamp", System.currentTimeMillis());

            JSONObject tablesObj = new JSONObject();

            for (String tableName : TABLES) {
                JSONArray tableArray = new JSONArray();
                Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
                try {
                    String[] columnNames = cursor.getColumnNames();
                    while (cursor.moveToNext()) {
                        JSONObject rowObj = new JSONObject();
                        for (String col : columnNames) {
                            int colIndex = cursor.getColumnIndex(col);
                            int type = cursor.getType(colIndex);
                            switch (type) {
                                case Cursor.FIELD_TYPE_INTEGER:
                                    rowObj.put(col, cursor.getLong(colIndex));
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    rowObj.put(col, cursor.getDouble(colIndex));
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    rowObj.put(col, cursor.getString(colIndex));
                                    break;
                                case Cursor.FIELD_TYPE_BLOB:
                                    byte[] blob = cursor.getBlob(colIndex);
                                    if (blob != null) {
                                        rowObj.put(col, Base64.getEncoder().encodeToString(blob));
                                    }
                                    break;
                                default:
                                    rowObj.put(col, cursor.getString(colIndex));
                                    break;
                            }
                        }
                        tableArray.put(rowObj);
                    }
                } finally {
                    cursor.close();
                }
                tablesObj.put(tableName, tableArray);
            }
            root.put("tables", tablesObj);

            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) return false;
                outputStream.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error exporting backup JSON", e);
            return false;
        } finally {
            db.close();
            dbHelper.close();
        }
    }

    public static boolean restoreBackupJson(Context context, Uri uri) {
        StringBuilder jsonBuilder = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading backup file", e);
            return false;
        }

        try {
            JSONObject root = new JSONObject(jsonBuilder.toString());
            if (!root.has("tables")) {
                Log.e(TAG, "Invalid backup format: missing 'tables'");
                return false;
            }
            JSONObject tablesObj = root.getJSONObject("tables");

            MoneyFlowDbHelper dbHelper = new MoneyFlowDbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("PRAGMA foreign_keys=OFF;");

                String[] reverseTables = {
                        MoneyFlowDbHelper.TABLE_APP_SETTINGS,
                        MoneyFlowDbHelper.TABLE_BUDGETS,
                        MoneyFlowDbHelper.TABLE_SMS_LOGS,
                        MoneyFlowDbHelper.TABLE_SMS_SENDERS,
                        MoneyFlowDbHelper.TABLE_TRANSACTIONS,
                        MoneyFlowDbHelper.TABLE_PAYMENT_METHODS,
                        MoneyFlowDbHelper.TABLE_INCOME_SOURCES,
                        MoneyFlowDbHelper.TABLE_CATEGORIES,
                        MoneyFlowDbHelper.TABLE_TRANSACTION_TYPES,
                        MoneyFlowDbHelper.TABLE_ACCOUNTS,
                        MoneyFlowDbHelper.TABLE_OWNERS
                };

                for (String tbl : reverseTables) {
                    db.delete(tbl, null, null);
                }

                for (String tableName : TABLES) {
                    if (tablesObj.has(tableName)) {
                        JSONArray tableArray = tablesObj.getJSONArray(tableName);
                        for (int i = 0; i < tableArray.length(); i++) {
                            JSONObject rowObj = tableArray.getJSONObject(i);
                            ContentValues cv = new ContentValues();
                            Iterator<String> keys = rowObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                Object val = rowObj.get(key);
                                if (val == JSONObject.NULL) {
                                    cv.putNull(key);
                                } else if (val instanceof Integer) {
                                    cv.put(key, (Integer) val);
                                } else if (val instanceof Long) {
                                    cv.put(key, (Long) val);
                                } else if (val instanceof Double) {
                                    cv.put(key, (Double) val);
                                } else if (val instanceof Boolean) {
                                    cv.put(key, (Boolean) val ? 1 : 0);
                                } else {
                                    cv.put(key, val.toString());
                                }
                            }
                            db.insert(tableName, null, cv);
                        }
                    }
                }

                db.execSQL("PRAGMA foreign_keys=ON;");
                db.setTransactionSuccessful();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error restoring database transaction", e);
                return false;
            } finally {
                db.endTransaction();
                db.close();
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing backup JSON", e);
            return false;
        }
    }
}
