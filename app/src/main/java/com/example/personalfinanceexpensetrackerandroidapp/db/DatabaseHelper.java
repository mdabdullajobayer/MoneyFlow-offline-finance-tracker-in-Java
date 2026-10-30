package com.example.personalfinanceexpensetrackerandroidapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneyflow.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_OWNERS = "owners";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_TRANSACTION_TYPES = "transaction_types";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_INCOME_SOURCES = "income_sources";
    public static final String TABLE_PAYMENT_METHODS = "payment_methods";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_SMS_SENDERS = "sms_senders";
    public static final String TABLE_SMS_LOGS = "sms_logs";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_APP_SETTINGS = "app_settings";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // 1. owners
            db.execSQL("CREATE TABLE " + TABLE_OWNERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "is_default INTEGER DEFAULT 0);");

            // 2. accounts
            db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "balance REAL DEFAULT 0.0, " +
                    "currency TEXT DEFAULT 'BDT', " +
                    "icon TEXT);");

            // 3. transaction_types
            db.execSQL("CREATE TABLE " + TABLE_TRANSACTION_TYPES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL);");

            // 4. categories
            db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "icon TEXT, " +
                    "owner_id INTEGER, " +
                    "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id) ON DELETE CASCADE);");

            // 5. income_sources
            db.execSQL("CREATE TABLE " + TABLE_INCOME_SOURCES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "owner_id INTEGER, " +
                    "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id) ON DELETE CASCADE);");

            // 6. payment_methods
            db.execSQL("CREATE TABLE " + TABLE_PAYMENT_METHODS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "icon TEXT);");

            // 7. transactions
            db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "owner_id INTEGER NOT NULL, " +
                    "account_id INTEGER NOT NULL, " +
                    "transaction_type_id INTEGER NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "income_source_id INTEGER, " +
                    "payment_method_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "note TEXT, " +
                    "payee_payer TEXT, " +
                    "is_recurring INTEGER DEFAULT 0, " +
                    "attachment_uri TEXT, " +
                    "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(account_id) REFERENCES " + TABLE_ACCOUNTS + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(transaction_type_id) REFERENCES " + TABLE_TRANSACTION_TYPES + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(income_source_id) REFERENCES " + TABLE_INCOME_SOURCES + "(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY(payment_method_id) REFERENCES " + TABLE_PAYMENT_METHODS + "(id) ON DELETE CASCADE);");

            // 8. sms_senders
            db.execSQL("CREATE TABLE " + TABLE_SMS_SENDERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "keyword_filter TEXT, " +
                    "account_id INTEGER, " +
                    "FOREIGN KEY(account_id) REFERENCES " + TABLE_ACCOUNTS + "(id) ON DELETE CASCADE);");

            // 9. sms_logs
            db.execSQL("CREATE TABLE " + TABLE_SMS_LOGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL, " +
                    "message_body TEXT NOT NULL, " +
                    "received_date TEXT NOT NULL, " +
                    "parsed_amount REAL, " +
                    "status TEXT NOT NULL, " +
                    "transaction_id INTEGER, " +
                    "FOREIGN KEY(transaction_id) REFERENCES " + TABLE_TRANSACTIONS + "(id) ON DELETE SET NULL);");

            // 10. budgets
            db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "owner_id INTEGER NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL, " +
                    "period TEXT DEFAULT 'MONTHLY', " +
                    "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id) ON DELETE CASCADE);");

            // 11. app_settings
            db.execSQL("CREATE TABLE " + TABLE_APP_SETTINGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "key TEXT UNIQUE NOT NULL, " +
                    "value TEXT NOT NULL);");

            // Seed initial default records
            seedInitialData(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Owners
        db.execSQL("INSERT INTO " + TABLE_OWNERS + " (name, type, is_default) VALUES ('Personal', 'PERSONAL', 1);");
        db.execSQL("INSERT INTO " + TABLE_OWNERS + " (name, type, is_default) VALUES ('Business', 'BUSINESS', 0);");

        // 2. Transaction Types
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name) VALUES ('IN');");
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name) VALUES ('OUT');");
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name) VALUES ('TRANSFER');");

        // 3. Accounts
        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, currency) VALUES ('Cash in Hand', 'CASH', 0.0, 'BDT');");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, currency) VALUES ('Prime Bank Account', 'BANK', 0.0, 'BDT');");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, currency) VALUES ('bKash Personal', 'MOBILE_WALLET', 0.0, 'BDT');");

        // 4. Payment Methods
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name) VALUES ('Cash');");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name) VALUES ('Credit/Debit Card');");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name) VALUES ('Bank Transfer');");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name) VALUES ('Mobile Financial Services (MFS)');");

        // 5. Income Sources
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id) VALUES ('Salary', 1);");
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id) VALUES ('Freelance', 1);");
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id) VALUES ('Client Sales', 2);");
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id) VALUES ('Business Services', 2);");

        // 6. Categories
        // Personal OUT (owner_id = 1)
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Food & Dining', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Groceries', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Transportation', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Utilities', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Entertainment', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Healthcare', 'OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Rent', 'OUT', 1);");
        // Personal IN (owner_id = 1)
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Salary Income', 'IN', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Investments', 'IN', 1);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Gifts', 'IN', 1);");

        // Business OUT (owner_id = 2)
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Inventory / Supplies', 'OUT', 2);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Staff Salaries', 'OUT', 2);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Office Rent', 'OUT', 2);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Marketing', 'OUT', 2);");
        // Business IN (owner_id = 2)
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Client Payments', 'IN', 2);");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, owner_id) VALUES ('Product Sales', 'IN', 2);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_LOGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_SENDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME_SOURCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENT_METHODS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_TYPES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OWNERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SETTINGS);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
