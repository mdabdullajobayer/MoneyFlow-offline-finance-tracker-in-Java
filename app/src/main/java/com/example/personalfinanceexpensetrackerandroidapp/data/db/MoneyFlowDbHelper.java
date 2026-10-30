package com.example.personalfinanceexpensetrackerandroidapp.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MoneyFlowDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "moneyflow.db";
    private static final int DATABASE_VERSION = 4;

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

    public MoneyFlowDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_OWNERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "is_default INTEGER DEFAULT 0, " +
                "is_active INTEGER DEFAULT 1);");

        db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "balance REAL DEFAULT 0.0, " +
                "balance_minor_units INTEGER DEFAULT 0, " +
                "currency TEXT DEFAULT 'BDT', " +
                "icon TEXT, " +
                "is_active INTEGER DEFAULT 1);");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTION_TYPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1);");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "icon TEXT, " +
                "owner_id INTEGER, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_INCOME_SOURCES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "owner_id INTEGER, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_PAYMENT_METHODS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "icon TEXT, " +
                "is_active INTEGER DEFAULT 1);");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "owner_id INTEGER, " +
                "account_id INTEGER, " +
                "destination_account_id INTEGER, " +
                "transaction_type_id INTEGER, " +
                "category_id INTEGER, " +
                "income_source_id INTEGER, " +
                "payment_method_id INTEGER, " +
                "amount INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "note TEXT, " +
                "payee_payer TEXT, " +
                "is_recurring INTEGER DEFAULT 0, " +
                "attachment_uri TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id), " +
                "FOREIGN KEY(account_id) REFERENCES " + TABLE_ACCOUNTS + "(id), " +
                "FOREIGN KEY(destination_account_id) REFERENCES " + TABLE_ACCOUNTS + "(id), " +
                "FOREIGN KEY(transaction_type_id) REFERENCES " + TABLE_TRANSACTION_TYPES + "(id), " +
                "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id), " +
                "FOREIGN KEY(income_source_id) REFERENCES " + TABLE_INCOME_SOURCES + "(id), " +
                "FOREIGN KEY(payment_method_id) REFERENCES " + TABLE_PAYMENT_METHODS + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_SMS_SENDERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_name TEXT NOT NULL, " +
                "keyword_filter TEXT, " +
                "account_id INTEGER, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY(account_id) REFERENCES " + TABLE_ACCOUNTS + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_SMS_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "message_body TEXT NOT NULL, " +
                "received_date TEXT NOT NULL, " +
                "parsed_amount REAL, " +
                "transaction_type TEXT, " +
                "reference TEXT, " +
                "status TEXT NOT NULL, " +
                "transaction_id INTEGER, " +
                "account_id INTEGER, " +
                "FOREIGN KEY(transaction_id) REFERENCES " + TABLE_TRANSACTIONS + "(id), " +
                "FOREIGN KEY(account_id) REFERENCES " + TABLE_ACCOUNTS + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "owner_id INTEGER, " +
                "category_id INTEGER, " +
                "amount REAL NOT NULL, " +
                "start_date TEXT NOT NULL, " +
                "end_date TEXT NOT NULL, " +
                "period TEXT DEFAULT 'MONTHLY', " +
                "FOREIGN KEY(owner_id) REFERENCES " + TABLE_OWNERS + "(id), " +
                "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id));");

        db.execSQL("CREATE TABLE " + TABLE_APP_SETTINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`key` TEXT UNIQUE NOT NULL, " +
                "value TEXT NOT NULL);");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_SENDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENT_METHODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME_SOURCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_TYPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OWNERS);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_OWNERS + " (name, type, is_default, is_active) VALUES ('Personal', 'PERSONAL', 1, 1);");
        db.execSQL("INSERT INTO " + TABLE_OWNERS + " (name, type, is_default, is_active) VALUES ('Business', 'BUSINESS', 0, 1);");

        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name, is_active) VALUES ('IN', 1);");
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name, is_active) VALUES ('OUT', 1);");
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_TYPES + " (name, is_active) VALUES ('TRANSFER', 1);");

        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, balance_minor_units, currency, icon, is_active) VALUES ('Cash in Hand', 'CASH', 0.0, 0, 'BDT', 'cash', 1);");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, balance_minor_units, currency, icon, is_active) VALUES ('Prime Bank Account', 'BANK', 0.0, 0, 'BDT', 'bank', 1);");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNTS + " (name, type, balance, balance_minor_units, currency, icon, is_active) VALUES ('bKash Personal', 'MOBILE_WALLET', 0.0, 0, 'BDT', 'wallet', 1);");

        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name, icon, is_active) VALUES ('Cash', 'cash', 1);");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name, icon, is_active) VALUES ('Card', 'card', 1);");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name, icon, is_active) VALUES ('Bank Transfer', 'bank', 1);");
        db.execSQL("INSERT INTO " + TABLE_PAYMENT_METHODS + " (name, icon, is_active) VALUES ('MFS', 'wallet', 1);");

        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id, is_active) VALUES ('Full-time Job', 1, 1);");
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id, is_active) VALUES ('Freelance', 1, 1);");
        db.execSQL("INSERT INTO " + TABLE_INCOME_SOURCES + " (name, owner_id, is_active) VALUES ('Client Sales', 2, 1);");

        // Seed Default SMS Senders
        db.execSQL("INSERT INTO " + TABLE_SMS_SENDERS + " (sender_name, keyword_filter, account_id, is_active) VALUES ('bKash', 'Tk', 3, 1);");
        db.execSQL("INSERT INTO " + TABLE_SMS_SENDERS + " (sender_name, keyword_filter, account_id, is_active) VALUES ('Nagad', 'Tk', 3, 1);");
        db.execSQL("INSERT INTO " + TABLE_SMS_SENDERS + " (sender_name, keyword_filter, account_id, is_active) VALUES ('CityBank', 'BDT', 2, 1);");

        String[] personalOut = {"Food & Dining", "Groceries", "Transportation", "Utilities", "Entertainment", "Healthcare", "Rent", "Loan Repayment"};
        for (String cat : personalOut) {
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, icon, owner_id, is_active) VALUES ('" + cat + "', 'OUT', 'category', 1, 1);");
        }
        String[] personalIn = {"Salary", "Investments", "Gifts", "Loan Taking"};
        for (String cat : personalIn) {
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, icon, owner_id, is_active) VALUES ('" + cat + "', 'IN', 'category', 1, 1);");
        }
        String[] businessOut = {"Inventory", "Salaries", "Office Rent", "Utilities", "Marketing", "Loan Repayment"};
        for (String cat : businessOut) {
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, icon, owner_id, is_active) VALUES ('" + cat + "', 'OUT', 'category', 2, 1);");
        }
        String[] businessIn = {"Client Sales", "Services", "Loan Taking"};
        for (String cat : businessIn) {
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, type, icon, owner_id, is_active) VALUES ('" + cat + "', 'IN', 'category', 2, 1);");
        }
    }
}
