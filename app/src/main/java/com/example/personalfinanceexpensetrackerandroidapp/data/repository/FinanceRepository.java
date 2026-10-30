package com.example.personalfinanceexpensetrackerandroidapp.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.personalfinanceexpensetrackerandroidapp.data.db.MoneyFlowDbHelper;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.*;
import com.example.personalfinanceexpensetrackerandroidapp.util.NotificationHelper;
import com.example.personalfinanceexpensetrackerandroidapp.util.SecurityUtils;
import com.example.personalfinanceexpensetrackerandroidapp.util.SmsParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceRepository {
    private final Context context;
    private final MoneyFlowDbHelper dbHelper;
    private final ExecutorService executor;

    public enum ResultType {
        DELETED, DEACTIVATED
    }

    public interface OwnersCallback { void onResult(List<Owner> owners); }
    public interface AccountsCallback { void onResult(List<Account> accounts); }
    public interface CategoriesCallback { void onResult(List<Category> categories); }
    public interface IncomeSourcesCallback { void onResult(List<IncomeSource> sources); }
    public interface PaymentMethodsCallback { void onResult(List<PaymentMethod> methods); }
    public interface TransactionTypesCallback { void onResult(List<TransactionType> types); }
    public interface BudgetsCallback { void onResult(List<Budget> budgets); }
    public interface TransactionsCallback { void onResult(List<Transaction> transactions); }
    public interface SmsSendersCallback { void onResult(List<SmsSender> senders); }
    public interface SmsLogsCallback { void onResult(List<SmsLog> logs); }
    public interface BooleanCallback { void onResult(boolean success); }
    public interface LongCallback { void onResult(long id); }
    public interface ResultTypeCallback { void onResult(ResultType result); }

    public FinanceRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new MoneyFlowDbHelper(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void getOwners(OwnersCallback callback) {
        executor.execute(() -> {
            List<Owner> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, type, is_default, is_active FROM " + MoneyFlowDbHelper.TABLE_OWNERS, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String type = cursor.getString(2);
                    boolean isDefault = cursor.getInt(3) == 1;
                    boolean isActive = cursor.getInt(4) == 1;
                    list.add(new Owner(id, name, type, isDefault, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertOwner(String name, String type, boolean isDefault, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (isDefault) {
                ContentValues cvReset = new ContentValues();
                cvReset.put("is_default", 0);
                db.update(MoneyFlowDbHelper.TABLE_OWNERS, cvReset, null, null);
            }
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("type", type);
            cv.put("is_default", isDefault ? 1 : 0);
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_OWNERS, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void updateOwner(Owner owner, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (owner.isDefault()) {
                ContentValues cvReset = new ContentValues();
                cvReset.put("is_default", 0);
                db.update(MoneyFlowDbHelper.TABLE_OWNERS, cvReset, null, null);
            }
            ContentValues cv = new ContentValues();
            cv.put("name", owner.getName());
            cv.put("type", owner.getType());
            cv.put("is_default", owner.isDefault() ? 1 : 0);
            cv.put("is_active", owner.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_OWNERS, cv, "id = ?", new String[]{String.valueOf(owner.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivateOwner(long ownerId, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE owner_id = ?", new String[]{String.valueOf(ownerId)});
            boolean hasTransactions = false;
            if (cursor.moveToFirst()) {
                hasTransactions = cursor.getInt(0) > 0;
            }
            cursor.close();

            if (hasTransactions) {
                ContentValues cv = new ContentValues();
                cv.put("is_active", 0);
                db.update(MoneyFlowDbHelper.TABLE_OWNERS, cv, "id = ?", new String[]{String.valueOf(ownerId)});
                callback.onResult(ResultType.DEACTIVATED);
            } else {
                db.delete(MoneyFlowDbHelper.TABLE_OWNERS, "id = ?", new String[]{String.valueOf(ownerId)});
                callback.onResult(ResultType.DELETED);
            }
        });
    }

    public void getTotalAccountBalance(LongCallback callback) {
        executor.execute(() -> {
            long total = 0;
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT SUM(balance_minor_units) FROM " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " WHERE is_active = 1", null);
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getLong(0);
            }
            cursor.close();
            if (callback != null) {
                callback.onResult(total);
            }
        });
    }

    public void getAccounts(AccountsCallback callback) {
        executor.execute(() -> {
            List<Account> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, type, balance, balance_minor_units, currency, icon, is_active FROM " + MoneyFlowDbHelper.TABLE_ACCOUNTS, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String type = cursor.getString(2);
                    double balance = cursor.getDouble(3);
                    long balanceMinorUnits = cursor.getLong(4);
                    String currency = cursor.getString(5);
                    String icon = cursor.getString(6);
                    boolean isActive = cursor.getInt(7) == 1;
                    list.add(new Account(id, name, type, balance, balanceMinorUnits, currency, icon, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertAccount(String name, String type, double balance, String currency, String icon, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long minorUnits = Math.round(balance * 100.0);
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("type", type);
            cv.put("balance", balance);
            cv.put("balance_minor_units", minorUnits);
            cv.put("currency", currency);
            cv.put("icon", icon);
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_ACCOUNTS, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void insertAccountReturningId(String name, String type, double balance, String currency, String icon, LongCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long minorUnits = Math.round(balance * 100.0);
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("type", type);
            cv.put("balance", balance);
            cv.put("balance_minor_units", minorUnits);
            cv.put("currency", currency);
            cv.put("icon", icon);
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_ACCOUNTS, null, cv);
            if (callback != null) callback.onResult(id);
        });
    }

    public void updateAccount(Account account, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", account.getName());
            cv.put("type", account.getType());
            cv.put("balance", account.getBalance());
            cv.put("balance_minor_units", account.getBalanceMinorUnits());
            cv.put("currency", account.getCurrency());
            cv.put("icon", account.getIcon());
            cv.put("is_active", account.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_ACCOUNTS, cv, "id = ?", new String[]{String.valueOf(account.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivateAccount(long accountId, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE account_id = ? OR destination_account_id = ?", new String[]{String.valueOf(accountId), String.valueOf(accountId)});
            boolean hasTransactions = false;
            if (cursor.moveToFirst()) {
                hasTransactions = cursor.getInt(0) > 0;
            }
            cursor.close();

            if (hasTransactions) {
                ContentValues cv = new ContentValues();
                cv.put("is_active", 0);
                db.update(MoneyFlowDbHelper.TABLE_ACCOUNTS, cv, "id = ?", new String[]{String.valueOf(accountId)});
                callback.onResult(ResultType.DEACTIVATED);
            } else {
                db.delete(MoneyFlowDbHelper.TABLE_ACCOUNTS, "id = ?", new String[]{String.valueOf(accountId)});
                callback.onResult(ResultType.DELETED);
            }
        });
    }

    public void getCategories(CategoriesCallback callback) {
        executor.execute(() -> {
            List<Category> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, type, icon, owner_id, is_active FROM " + MoneyFlowDbHelper.TABLE_CATEGORIES, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String type = cursor.getString(2);
                    String icon = cursor.getString(3);
                    long ownerId = cursor.getLong(4);
                    boolean isActive = cursor.getInt(5) == 1;
                    list.add(new Category(id, name, type, icon, ownerId, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertCategory(String name, String type, String icon, long ownerId, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("type", type);
            cv.put("icon", icon);
            if (ownerId <= 0) {
                cv.putNull("owner_id");
            } else {
                cv.put("owner_id", ownerId);
            }
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_CATEGORIES, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void updateCategory(Category category, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", category.getName());
            cv.put("type", category.getType());
            cv.put("icon", category.getIcon());
            if (category.getOwnerId() <= 0) {
                cv.putNull("owner_id");
            } else {
                cv.put("owner_id", category.getOwnerId());
            }
            cv.put("is_active", category.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_CATEGORIES, cv, "id = ?", new String[]{String.valueOf(category.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivateCategory(long categoryId, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE category_id = ?", new String[]{String.valueOf(categoryId)});
            boolean hasTransactions = false;
            if (cursor.moveToFirst()) {
                hasTransactions = cursor.getInt(0) > 0;
            }
            cursor.close();

            if (hasTransactions) {
                ContentValues cv = new ContentValues();
                cv.put("is_active", 0);
                db.update(MoneyFlowDbHelper.TABLE_CATEGORIES, cv, "id = ?", new String[]{String.valueOf(categoryId)});
                callback.onResult(ResultType.DEACTIVATED);
            } else {
                db.delete(MoneyFlowDbHelper.TABLE_CATEGORIES, "id = ?", new String[]{String.valueOf(categoryId)});
                callback.onResult(ResultType.DELETED);
            }
        });
    }

    public void getIncomeSources(IncomeSourcesCallback callback) {
        executor.execute(() -> {
            List<IncomeSource> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, owner_id, is_active FROM " + MoneyFlowDbHelper.TABLE_INCOME_SOURCES, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    long ownerId = cursor.getLong(2);
                    boolean isActive = cursor.getInt(3) == 1;
                    list.add(new IncomeSource(id, name, ownerId, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertIncomeSource(String name, long ownerId, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            if (ownerId <= 0) {
                cv.putNull("owner_id");
            } else {
                cv.put("owner_id", ownerId);
            }
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_INCOME_SOURCES, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void updateIncomeSource(IncomeSource source, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", source.getName());
            if (source.getOwnerId() <= 0) {
                cv.putNull("owner_id");
            } else {
                cv.put("owner_id", source.getOwnerId());
            }
            cv.put("is_active", source.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_INCOME_SOURCES, cv, "id = ?", new String[]{String.valueOf(source.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivateIncomeSource(long sourceId, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE income_source_id = ?", new String[]{String.valueOf(sourceId)});
            boolean hasTransactions = false;
            if (cursor.moveToFirst()) {
                hasTransactions = cursor.getInt(0) > 0;
            }
            cursor.close();

            if (hasTransactions) {
                ContentValues cv = new ContentValues();
                cv.put("is_active", 0);
                db.update(MoneyFlowDbHelper.TABLE_INCOME_SOURCES, cv, "id = ?", new String[]{String.valueOf(sourceId)});
                callback.onResult(ResultType.DEACTIVATED);
            } else {
                db.delete(MoneyFlowDbHelper.TABLE_INCOME_SOURCES, "id = ?", new String[]{String.valueOf(sourceId)});
                callback.onResult(ResultType.DELETED);
            }
        });
    }

    public void getPaymentMethods(PaymentMethodsCallback callback) {
        executor.execute(() -> {
            List<PaymentMethod> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, icon, is_active FROM " + MoneyFlowDbHelper.TABLE_PAYMENT_METHODS, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String icon = cursor.getString(2);
                    boolean isActive = cursor.getInt(3) == 1;
                    list.add(new PaymentMethod(id, name, icon, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertPaymentMethod(String name, String icon, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("icon", icon);
            cv.put("is_active", 1);
            long id = db.insert(MoneyFlowDbHelper.TABLE_PAYMENT_METHODS, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void updatePaymentMethod(PaymentMethod method, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", method.getName());
            cv.put("icon", method.getIcon());
            cv.put("is_active", method.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_PAYMENT_METHODS, cv, "id = ?", new String[]{String.valueOf(method.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivatePaymentMethod(long methodId, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE payment_method_id = ?", new String[]{String.valueOf(methodId)});
            boolean hasTransactions = false;
            if (cursor.moveToFirst()) {
                hasTransactions = cursor.getInt(0) > 0;
            }
            cursor.close();

            if (hasTransactions) {
                ContentValues cv = new ContentValues();
                cv.put("is_active", 0);
                db.update(MoneyFlowDbHelper.TABLE_PAYMENT_METHODS, cv, "id = ?", new String[]{String.valueOf(methodId)});
                callback.onResult(ResultType.DEACTIVATED);
            } else {
                db.delete(MoneyFlowDbHelper.TABLE_PAYMENT_METHODS, "id = ?", new String[]{String.valueOf(methodId)});
                callback.onResult(ResultType.DELETED);
            }
        });
    }

    public void getTransactionTypes(TransactionTypesCallback callback) {
        executor.execute(() -> {
            List<TransactionType> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, is_active FROM " + MoneyFlowDbHelper.TABLE_TRANSACTION_TYPES, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    boolean isActive = cursor.getInt(2) == 1;
                    list.add(new TransactionType(id, name, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void getBudgets(BudgetsCallback callback) {
        executor.execute(() -> {
            List<Budget> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, owner_id, category_id, amount, start_date, end_date, period FROM " + MoneyFlowDbHelper.TABLE_BUDGETS, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    long ownerId = cursor.getLong(1);
                    long categoryId = cursor.getLong(2);
                    double amount = cursor.getDouble(3);
                    String startDate = cursor.getString(4);
                    String endDate = cursor.getString(5);
                    String period = cursor.getString(6);
                    list.add(new Budget(id, ownerId, categoryId, amount, startDate, endDate, period));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public static class TransactionFilter {
        private final Long ownerId;
        private final Long accountId;
        private final Long categoryId;
        private final Long transactionTypeId;
        private final String startDate;
        private final String endDate;
        private final String searchQuery;

        public TransactionFilter() {
            this(null, null, null, null, null, null, null);
        }

        public TransactionFilter(Long ownerId, Long accountId, Long categoryId, Long transactionTypeId, String startDate, String endDate, String searchQuery) {
            this.ownerId = ownerId;
            this.accountId = accountId;
            this.categoryId = categoryId;
            this.transactionTypeId = transactionTypeId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.searchQuery = searchQuery;
        }

        public Long getOwnerId() { return ownerId; }
        public Long getAccountId() { return accountId; }
        public Long getCategoryId() { return categoryId; }
        public Long getTransactionTypeId() { return transactionTypeId; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getSearchQuery() { return searchQuery; }

        public TransactionFilter copy(Long ownerId, Long accountId, Long categoryId, Long transactionTypeId, String startDate, String endDate, String searchQuery) {
            return new TransactionFilter(
                    ownerId != null ? ownerId : this.ownerId,
                    accountId != null ? accountId : this.accountId,
                    categoryId != null ? categoryId : this.categoryId,
                    transactionTypeId != null ? transactionTypeId : this.transactionTypeId,
                    startDate != null ? startDate : this.startDate,
                    endDate != null ? endDate : this.endDate,
                    searchQuery != null ? searchQuery : this.searchQuery
            );
        }

        public static TransactionFilter defaultFilter() {
            return new TransactionFilter(null, null, null, null, null, null, null);
        }

        public TransactionFilter setSearchQuery(String searchQuery) {
            return copy(null, null, null, null, null, null, searchQuery);
        }

        public TransactionFilter setFilters(Long ownerId, Long accountId, Long categoryId, Long transactionTypeId) {
            return copy(ownerId, accountId, categoryId, transactionTypeId, null, null, null);
        }

        public TransactionFilter setFilters(Long ownerId, Long accountId, Long categoryId, Long transactionTypeId, String startDate, String endDate, String searchQuery) {
            return new TransactionFilter(ownerId, accountId, categoryId, transactionTypeId, startDate, endDate, searchQuery);
        }
    }

    public void getTransactions(TransactionFilter filter, TransactionsCallback callback) {
        executor.execute(() -> {
            List<Transaction> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT t.id, t.owner_id, t.account_id, t.destination_account_id, t.transaction_type_id, " +
                            "t.category_id, t.income_source_id, t.payment_method_id, t.amount, t.date, t.note, t.payee_payer, " +
                            "t.is_recurring, t.attachment_uri, t.is_active, " +
                            "o.name as owner_name, " +
                            "a.name as account_name, " +
                            "da.name as destination_account_name, " +
                            "tt.name as transaction_type_name, " +
                            "c.name as category_name, " +
                            "i.name as income_source_name, " +
                            "p.name as payment_method_name " +
                            "FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " t " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_OWNERS + " o ON t.owner_id = o.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " a ON t.account_id = a.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " da ON t.destination_account_id = da.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_TRANSACTION_TYPES + " tt ON t.transaction_type_id = tt.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_CATEGORIES + " c ON t.category_id = c.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_INCOME_SOURCES + " i ON t.income_source_id = i.id " +
                            "LEFT JOIN " + MoneyFlowDbHelper.TABLE_PAYMENT_METHODS + " p ON t.payment_method_id = p.id " +
                            "WHERE t.is_active = 1"
            );

            List<String> argsList = new ArrayList<>();

            if (filter != null) {
                if (filter.getOwnerId() != null && filter.getOwnerId() != -1L) {
                    queryBuilder.append(" AND t.owner_id = ?");
                    argsList.add(String.valueOf(filter.getOwnerId()));
                }
                if (filter.getAccountId() != null && filter.getAccountId() != -1L) {
                    queryBuilder.append(" AND (t.account_id = ? OR t.destination_account_id = ?)");
                    argsList.add(String.valueOf(filter.getAccountId()));
                    argsList.add(String.valueOf(filter.getAccountId()));
                }
                if (filter.getCategoryId() != null && filter.getCategoryId() != -1L) {
                    queryBuilder.append(" AND t.category_id = ?");
                    argsList.add(String.valueOf(filter.getCategoryId()));
                }
                if (filter.getTransactionTypeId() != null && filter.getTransactionTypeId() != -1L) {
                    queryBuilder.append(" AND t.transaction_type_id = ?");
                    argsList.add(String.valueOf(filter.getTransactionTypeId()));
                }
                if (filter.getStartDate() != null && !filter.getStartDate().isEmpty()) {
                    queryBuilder.append(" AND t.date >= ?");
                    argsList.add(filter.getStartDate());
                }
                if (filter.getEndDate() != null && !filter.getEndDate().isEmpty()) {
                    queryBuilder.append(" AND t.date <= ?");
                    argsList.add(filter.getEndDate());
                }
                if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                    queryBuilder.append(" AND (t.note LIKE ? OR t.payee_payer LIKE ?)");
                    String searchPattern = "%" + filter.getSearchQuery() + "%";
                    argsList.add(searchPattern);
                    argsList.add(searchPattern);
                }
            }

            queryBuilder.append(" ORDER BY t.date DESC, t.id DESC");

            Cursor cursor = db.rawQuery(queryBuilder.toString(), argsList.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    long ownerId = cursor.getLong(1);
                    long accountId = cursor.getLong(2);
                    Long destAccountId = cursor.isNull(3) ? null : cursor.getLong(3);
                    long typeId = cursor.getLong(4);
                    Long catId = cursor.isNull(5) ? null : cursor.getLong(5);
                    Long incId = cursor.isNull(6) ? null : cursor.getLong(6);
                    long payId = cursor.isNull(7) ? 0 : cursor.getLong(7);
                    long amount = cursor.getLong(8);
                    String date = cursor.getString(9);
                    String note = cursor.getString(10);
                    String payeePayer = cursor.getString(11);
                    boolean isRecurring = cursor.getInt(12) == 1;
                    String attachmentUri = cursor.getString(13);
                    boolean isActive = cursor.getInt(14) == 1;

                    String ownerName = cursor.getString(15);
                    String accountName = cursor.getString(16);
                    String destAccountName = cursor.getString(17);
                    String typeName = cursor.getString(18);
                    String catName = cursor.getString(19);
                    String incName = cursor.getString(20);
                    String payName = cursor.getString(21);

                    list.add(new Transaction(id, ownerId, accountId, destAccountId, typeId, catId, incId, payId,
                            amount, date, note, payeePayer, isRecurring, attachmentUri, isActive,
                            ownerName, accountName, destAccountName, typeName, catName, incName, payName));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    private void adjustAccountBalance(SQLiteDatabase db, long accountId, long deltaAmountMinorUnits) {
        Cursor cursor = db.rawQuery("SELECT balance_minor_units, balance FROM " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " WHERE id = ?", new String[]{String.valueOf(accountId)});
        if (cursor.moveToFirst()) {
            long currentMinorUnits = cursor.getLong(0);
            cursor.close();
            long newMinorUnits = currentMinorUnits + deltaAmountMinorUnits;
            double newBalanceDouble = newMinorUnits / 100.0;
            ContentValues cv = new ContentValues();
            cv.put("balance_minor_units", newMinorUnits);
            cv.put("balance", newBalanceDouble);
            db.update(MoneyFlowDbHelper.TABLE_ACCOUNTS, cv, "id = ?", new String[]{String.valueOf(accountId)});
        } else {
            cursor.close();
        }
    }

    private void applyTransactionBalanceEffect(SQLiteDatabase db, long typeId, long accountId, Long destAccountId, long amount, boolean reverse) {
        String typeName = null;
        Cursor cursor = db.rawQuery("SELECT name FROM " + MoneyFlowDbHelper.TABLE_TRANSACTION_TYPES + " WHERE id = ?", new String[]{String.valueOf(typeId)});
        if (cursor.moveToFirst()) {
            typeName = cursor.getString(0);
        }
        cursor.close();

        long sign = reverse ? -1L : 1L;

        boolean isIncome = typeId == 1 || "IN".equalsIgnoreCase(typeName) || "INCOME".equalsIgnoreCase(typeName);
        boolean isExpense = typeId == 2 || "OUT".equalsIgnoreCase(typeName) || "EXPENSE".equalsIgnoreCase(typeName);
        boolean isTransfer = typeId == 3 || "TRANSFER".equalsIgnoreCase(typeName);

        if (isIncome) {
            adjustAccountBalance(db, accountId, amount * sign);
        } else if (isExpense) {
            adjustAccountBalance(db, accountId, -amount * sign);
        } else if (isTransfer) {
            adjustAccountBalance(db, accountId, -amount * sign);
            if (destAccountId != null && destAccountId > 0) {
                adjustAccountBalance(db, destAccountId, amount * sign);
            }
        }
    }

    public void insertTransaction(Transaction tx, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                cv.put("owner_id", tx.getOwnerId());
                cv.put("account_id", tx.getAccountId());
                cv.put("destination_account_id", tx.getDestinationAccountId());
                cv.put("transaction_type_id", tx.getTransactionTypeId());
                cv.put("category_id", tx.getCategoryId());
                cv.put("income_source_id", tx.getIncomeSourceId());
                cv.put("payment_method_id", tx.getPaymentMethodId());
                cv.put("amount", tx.getAmount());
                cv.put("date", tx.getDate());
                cv.put("note", tx.getNote());
                cv.put("payee_payer", tx.getPayeePayer());
                cv.put("is_recurring", tx.isRecurring() ? 1 : 0);
                cv.put("attachment_uri", tx.getAttachmentUri());
                cv.put("is_active", 1);

                long id = db.insert(MoneyFlowDbHelper.TABLE_TRANSACTIONS, null, cv);
                if (id != -1L) {
                    applyTransactionBalanceEffect(db, tx.getTransactionTypeId(), tx.getAccountId(), tx.getDestinationAccountId(), tx.getAmount(), false);
                    db.setTransactionSuccessful();
                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            } catch (Exception e) {
                callback.onResult(false);
            } finally {
                db.endTransaction();
            }
        });
    }

    public void updateTransaction(Transaction newTx, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                Cursor cursor = db.rawQuery("SELECT id, owner_id, account_id, destination_account_id, transaction_type_id, category_id, income_source_id, payment_method_id, amount, date, note, payee_payer, is_recurring, attachment_uri, is_active FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE id = ?", new String[]{String.valueOf(newTx.getId())});
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    callback.onResult(false);
                    return;
                }
                Transaction oldTx = new Transaction(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getLong(2),
                        cursor.isNull(3) ? null : cursor.getLong(3),
                        cursor.getLong(4),
                        cursor.isNull(5) ? null : cursor.getLong(5),
                        cursor.isNull(6) ? null : cursor.getLong(6),
                        cursor.getLong(7),
                        cursor.getLong(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getString(11),
                        cursor.getInt(12) == 1,
                        cursor.getString(13),
                        cursor.getInt(14) == 1,
                        null, null, null, null, null, null, null
                );
                cursor.close();

                applyTransactionBalanceEffect(db, oldTx.getTransactionTypeId(), oldTx.getAccountId(), oldTx.getDestinationAccountId(), oldTx.getAmount(), true);
                applyTransactionBalanceEffect(db, newTx.getTransactionTypeId(), newTx.getAccountId(), newTx.getDestinationAccountId(), newTx.getAmount(), false);

                ContentValues cv = new ContentValues();
                cv.put("owner_id", newTx.getOwnerId());
                cv.put("account_id", newTx.getAccountId());
                cv.put("destination_account_id", newTx.getDestinationAccountId());
                cv.put("transaction_type_id", newTx.getTransactionTypeId());
                cv.put("category_id", newTx.getCategoryId());
                cv.put("income_source_id", newTx.getIncomeSourceId());
                cv.put("payment_method_id", newTx.getPaymentMethodId());
                cv.put("amount", newTx.getAmount());
                cv.put("date", newTx.getDate());
                cv.put("note", newTx.getNote());
                cv.put("payee_payer", newTx.getPayeePayer());
                cv.put("is_recurring", newTx.isRecurring() ? 1 : 0);
                cv.put("attachment_uri", newTx.getAttachmentUri());
                cv.put("is_active", newTx.isActive() ? 1 : 0);

                int rows = db.update(MoneyFlowDbHelper.TABLE_TRANSACTIONS, cv, "id = ?", new String[]{String.valueOf(newTx.getId())});
                if (rows > 0) {
                    db.setTransactionSuccessful();
                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            } catch (Exception e) {
                callback.onResult(false);
            } finally {
                db.endTransaction();
            }
        });
    }

    public void softDeleteTransaction(long transactionId, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                Cursor cursor = db.rawQuery("SELECT id, owner_id, account_id, destination_account_id, transaction_type_id, category_id, income_source_id, payment_method_id, amount, date, note, payee_payer, is_recurring, attachment_uri, is_active FROM " + MoneyFlowDbHelper.TABLE_TRANSACTIONS + " WHERE id = ?", new String[]{String.valueOf(transactionId)});
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    callback.onResult(false);
                    return;
                }
                Transaction tx = new Transaction(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getLong(2),
                        cursor.isNull(3) ? null : cursor.getLong(3),
                        cursor.getLong(4),
                        cursor.isNull(5) ? null : cursor.getLong(5),
                        cursor.isNull(6) ? null : cursor.getLong(6),
                        cursor.getLong(7),
                        cursor.getLong(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getString(11),
                        cursor.getInt(12) == 1,
                        cursor.getString(13),
                        cursor.getInt(14) == 1,
                        null, null, null, null, null, null, null
                );
                cursor.close();

                if (tx.isActive()) {
                    applyTransactionBalanceEffect(db, tx.getTransactionTypeId(), tx.getAccountId(), tx.getDestinationAccountId(), tx.getAmount(), true);
                    ContentValues cv = new ContentValues();
                    cv.put("is_active", 0);
                    int rows = db.update(MoneyFlowDbHelper.TABLE_TRANSACTIONS, cv, "id = ?", new String[]{String.valueOf(transactionId)});
                    if (rows > 0) {
                        db.setTransactionSuccessful();
                        callback.onResult(true);
                    } else {
                        callback.onResult(false);
                    }
                } else {
                    callback.onResult(true);
                }
            } catch (Exception e) {
                callback.onResult(false);
            } finally {
                db.endTransaction();
            }
        });
    }

    public void getSmsSenders(SmsSendersCallback callback) {
        executor.execute(() -> {
            List<SmsSender> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT s.id, s.sender_name, s.keyword_filter, s.account_id, s.is_active, a.name FROM " + MoneyFlowDbHelper.TABLE_SMS_SENDERS + " s LEFT JOIN " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " a ON s.account_id = a.id", null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String senderName = cursor.getString(1);
                    String keywordFilter = cursor.getString(2);
                    long accountId = cursor.getLong(3);
                    boolean isActive = cursor.getInt(4) == 1;
                    String accountName = cursor.isNull(5) ? "None" : cursor.getString(5);
                    list.add(new SmsSender(id, senderName, keywordFilter, accountId, accountName, isActive));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void insertSmsSender(String senderName, String keywordFilter, long accountId, boolean isActive, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("sender_name", senderName);
            cv.put("keyword_filter", keywordFilter);
            cv.put("account_id", accountId);
            cv.put("is_active", isActive ? 1 : 0);
            long id = db.insert(MoneyFlowDbHelper.TABLE_SMS_SENDERS, null, cv);
            callback.onResult(id != -1L);
        });
    }

    public void updateSmsSender(SmsSender sender, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("sender_name", sender.getSenderName());
            cv.put("keyword_filter", sender.getKeywordFilter());
            cv.put("account_id", sender.getAccountId());
            cv.put("is_active", sender.isActive() ? 1 : 0);
            int rows = db.update(MoneyFlowDbHelper.TABLE_SMS_SENDERS, cv, "id = ?", new String[]{String.valueOf(sender.getId())});
            callback.onResult(rows > 0);
        });
    }

    public void deleteOrDeactivateSmsSender(long id, ResultTypeCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(MoneyFlowDbHelper.TABLE_SMS_SENDERS, "id = ?", new String[]{String.valueOf(id)});
            callback.onResult(rows > 0 ? ResultType.DELETED : ResultType.DEACTIVATED);
        });
    }

    public void importSmsSenders(List<String> senderNames, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            boolean success = true;
            for (String name : senderNames) {
                Cursor cursor = db.rawQuery("SELECT id FROM " + MoneyFlowDbHelper.TABLE_SMS_SENDERS + " WHERE sender_name = ?", new String[]{name});
                boolean exists = cursor.moveToFirst();
                cursor.close();
                if (!exists) {
                    ContentValues cv = new ContentValues();
                    cv.put("sender_name", name);
                    cv.put("keyword_filter", "");
                    cv.put("account_id", 3L);
                    cv.put("is_active", 1);
                    long id = db.insert(MoneyFlowDbHelper.TABLE_SMS_SENDERS, null, cv);
                    if (id == -1L) success = false;
                }
            }
            if (callback != null) callback.onResult(success);
        });
    }

    public void getSmsLogs(SmsLogsCallback callback) {
        executor.execute(() -> {
            List<SmsLog> list = new ArrayList<>();
            long installTimestamp = SecurityUtils.getInstallTimestamp(context);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, sender, message_body, received_date, parsed_amount, transaction_type, reference, status, transaction_id, account_id FROM " + MoneyFlowDbHelper.TABLE_SMS_LOGS + " ORDER BY id DESC", null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String sender = cursor.getString(1);
                    String messageBody = cursor.getString(2);
                    String receivedDate = cursor.getString(3);
                    try {
                        java.util.Date d = sdf.parse(receivedDate);
                        if (d != null && d.getTime() < installTimestamp) {
                            continue;
                        }
                    } catch (Exception ignored) {}
                    double parsedAmount = cursor.getDouble(4);
                    String transactionType = cursor.getString(5);
                    String reference = cursor.getString(6);
                    String status = cursor.getString(7);
                    Long transactionId = cursor.isNull(8) ? null : cursor.getLong(8);
                    long accountId = cursor.getLong(9);
                    list.add(new SmsLog(id, sender, messageBody, receivedDate, parsedAmount, transactionType, reference, status, transactionId, accountId));
                } while (cursor.moveToNext());
            }
            cursor.close();
            callback.onResult(list);
        });
    }

    public void updateSmsLogStatus(long logId, String status, Long transactionId, BooleanCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("status", status);
            if (transactionId != null) {
                cv.put("transaction_id", transactionId);
            }
            int rows = db.update(MoneyFlowDbHelper.TABLE_SMS_LOGS, cv, "id = ?", new String[]{String.valueOf(logId)});
            callback.onResult(rows > 0);
        });
    }

    public void handleIncomingSms(String sender, String body) {
        executor.execute(() -> {
            try {
                long installTimestamp = SecurityUtils.getInstallTimestamp(context);
                if (System.currentTimeMillis() < installTimestamp) {
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT account_id FROM " + MoneyFlowDbHelper.TABLE_SMS_SENDERS + " WHERE sender_name = ? AND is_active = 1", new String[]{sender});
                long accountId = 3L;
                boolean matched = false;
                if (cursor.moveToFirst()) {
                    accountId = cursor.getLong(0);
                    matched = true;
                }
                cursor.close();

                if (!matched) {
                    Cursor cursorLike = db.rawQuery("SELECT sender_name, account_id FROM " + MoneyFlowDbHelper.TABLE_SMS_SENDERS + " WHERE is_active = 1", null);
                    if (cursorLike.moveToFirst()) {
                        do {
                            String name = cursorLike.getString(0);
                            if (sender != null && sender.contains(name)) {
                                accountId = cursorLike.getLong(1);
                                matched = true;
                                break;
                            }
                        } while (cursorLike.moveToNext());
                    }
                    cursorLike.close();
                }

                if (!matched) return;

                SmsParser.ParsedResult parsed = SmsParser.parse(body);
                if (!parsed.success) return;

                accountId = ensureAccountExists(db, accountId, sender);

                ContentValues cv = new ContentValues();
                cv.put("sender", sender);
                cv.put("message_body", body);
                cv.put("received_date", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
                cv.put("parsed_amount", parsed.amount);
                cv.put("transaction_type", parsed.transactionType);
                cv.put("reference", parsed.reference);
                cv.put("status", "PENDING");
                cv.put("account_id", accountId);
                db.insert(MoneyFlowDbHelper.TABLE_SMS_LOGS, null, cv);

                NotificationHelper.showSmsNotification(context, sender, parsed.amount, parsed.transactionType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private long ensureAccountExists(SQLiteDatabase db, long accountId, String senderName) {
        Cursor cursor = db.rawQuery("SELECT id FROM " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " WHERE id = ?", new String[]{String.valueOf(accountId)});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            return accountId;
        }

        Cursor nameCursor = db.rawQuery("SELECT id FROM " + MoneyFlowDbHelper.TABLE_ACCOUNTS + " WHERE name = ?", new String[]{senderName});
        if (nameCursor.moveToFirst()) {
            long existingId = nameCursor.getLong(0);
            nameCursor.close();
            return existingId;
        }
        nameCursor.close();

        String type = "MOBILE_WALLET";
        String icon = "wallet";
        String upper = senderName != null ? senderName.toUpperCase() : "";
        if (upper.contains("BANK") || upper.contains("CITY") || upper.contains("DBBL") || upper.contains("BRAC") || upper.contains("EBL")) {
            type = "BANK";
            icon = "bank";
        } else if (upper.contains("CASH")) {
            type = "CASH";
            icon = "cash";
        }

        ContentValues cv = new ContentValues();
        cv.put("name", senderName != null ? senderName : "SMS Account");
        cv.put("type", type);
        cv.put("balance", 0.0);
        cv.put("balance_minor_units", 0L);
        cv.put("currency", "BDT");
        cv.put("icon", icon);
        cv.put("is_active", 1);

        long newAccountId = db.insert(MoneyFlowDbHelper.TABLE_ACCOUNTS, null, cv);
        if (newAccountId != -1L) {
            ContentValues senderCv = new ContentValues();
            senderCv.put("account_id", newAccountId);
            db.update(MoneyFlowDbHelper.TABLE_SMS_SENDERS, senderCv, "sender_name = ?", new String[]{senderName});
            return newAccountId;
        }

        return accountId;
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT value FROM " + MoneyFlowDbHelper.TABLE_APP_SETTINGS + " WHERE `key` = ?", new String[]{key});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return defaultValue;
    }

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put("key", key);
            cv.put("value", value);
            db.insertWithOnConflict(MoneyFlowDbHelper.TABLE_APP_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
