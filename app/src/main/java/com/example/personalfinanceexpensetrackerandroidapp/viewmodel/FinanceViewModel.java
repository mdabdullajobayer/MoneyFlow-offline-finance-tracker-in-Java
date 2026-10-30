package com.example.personalfinanceexpensetrackerandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.*;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {
    private final FinanceRepository repository;

    private final MutableLiveData<List<Owner>> _owners = new MutableLiveData<>();
    private final LiveData<List<Owner>> owners = _owners;

    private final MutableLiveData<List<Account>> _accounts = new MutableLiveData<>();
    private final LiveData<List<Account>> accounts = _accounts;

    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    private final LiveData<List<Category>> categories = _categories;

    private final MutableLiveData<List<IncomeSource>> _incomeSources = new MutableLiveData<>();
    private final LiveData<List<IncomeSource>> incomeSources = _incomeSources;

    private final MutableLiveData<List<PaymentMethod>> _paymentMethods = new MutableLiveData<>();
    private final LiveData<List<PaymentMethod>> paymentMethods = _paymentMethods;

    private final MutableLiveData<List<TransactionType>> _transactionTypes = new MutableLiveData<>();
    private final LiveData<List<TransactionType>> transactionTypes = _transactionTypes;

    private final MutableLiveData<List<Budget>> _budgets = new MutableLiveData<>();
    private final LiveData<List<Budget>> budgets = _budgets;

    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    private final LiveData<List<Transaction>> transactions = _transactions;

    private final MutableLiveData<List<SmsSender>> _smsSenders = new MutableLiveData<>();
    private final LiveData<List<SmsSender>> smsSenders = _smsSenders;

    private final MutableLiveData<List<SmsLog>> _smsLogs = new MutableLiveData<>();
    private final LiveData<List<SmsLog>> smsLogs = _smsLogs;

    private final MutableLiveData<Long> _totalAccountBalance = new MutableLiveData<>(0L);
    private final LiveData<Long> totalAccountBalance = _totalAccountBalance;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FinanceRepository(application);
        refreshAll();
    }

    public LiveData<List<Owner>> getOwners() { return owners; }
    public LiveData<List<Account>> getAccounts() { return accounts; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<List<IncomeSource>> getIncomeSources() { return incomeSources; }
    public LiveData<List<PaymentMethod>> getPaymentMethods() { return paymentMethods; }
    public LiveData<List<TransactionType>> getTransactionTypes() { return transactionTypes; }
    public LiveData<List<Budget>> getBudgets() { return budgets; }
    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<List<SmsSender>> getSmsSenders() { return smsSenders; }
    public LiveData<List<SmsLog>> getSmsLogs() { return smsLogs; }
    public LiveData<Long> getTotalAccountBalanceLiveData() { return totalAccountBalance; }

    public void getTotalAccountBalance(FinanceRepository.LongCallback callback) {
        repository.getTotalAccountBalance(callback);
    }

    public void refreshTotalAccountBalance() {
        repository.getTotalAccountBalance(_totalAccountBalance::postValue);
    }

    public void refreshAll() {
        refreshOwners();
        refreshAccounts();
        refreshCategories();
        refreshIncomeSources();
        refreshPaymentMethods();
        refreshTransactionTypes();
        refreshBudgets();
        refreshTransactions(null);
        refreshTotalAccountBalance();
    }

    public void refreshOwners() {
        repository.getOwners(_owners::postValue);
    }

    public void refreshAccounts() {
        repository.getAccounts(_accounts::postValue);
        refreshTotalAccountBalance();
    }

    public void refreshCategories() {
        repository.getCategories(_categories::postValue);
    }

    public void refreshIncomeSources() {
        repository.getIncomeSources(_incomeSources::postValue);
    }

    public void refreshPaymentMethods() {
        repository.getPaymentMethods(_paymentMethods::postValue);
    }

    public void refreshTransactionTypes() {
        repository.getTransactionTypes(_transactionTypes::postValue);
    }

    public void refreshBudgets() {
        repository.getBudgets(_budgets::postValue);
    }

    public void refreshTransactions(FinanceRepository.TransactionFilter filter) {
        repository.getTransactions(filter, _transactions::postValue);
    }

    public void addOwner(String name, String type, boolean isDefault, FinanceRepository.BooleanCallback onDone) {
        repository.insertOwner(name, type, isDefault, success -> {
            if (success) refreshOwners();
            onDone.onResult(success);
        });
    }

    public void updateOwner(Owner owner, FinanceRepository.BooleanCallback onDone) {
        repository.updateOwner(owner, success -> {
            if (success) refreshOwners();
            onDone.onResult(success);
        });
    }

    public void deleteOrDeactivateOwner(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivateOwner(id, result -> {
            refreshOwners();
            onDone.onResult(result);
        });
    }

    public void addAccount(String name, String type, double balance, String currency, String icon, FinanceRepository.BooleanCallback onDone) {
        repository.insertAccount(name, type, balance, currency, icon, success -> {
            if (success) refreshAccounts();
            onDone.onResult(success);
        });
    }

    public void addAccountWithId(String name, String type, double balance, String currency, String icon, FinanceRepository.LongCallback onDone) {
        repository.insertAccountReturningId(name, type, balance, currency, icon, id -> {
            if (id != -1L) refreshAccounts();
            if (onDone != null) onDone.onResult(id);
        });
    }

    public void updateAccount(Account account, FinanceRepository.BooleanCallback onDone) {
        repository.updateAccount(account, success -> {
            if (success) refreshAccounts();
            onDone.onResult(success);
        });
    }

    public void deleteOrDeactivateAccount(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivateAccount(id, result -> {
            refreshAccounts();
            onDone.onResult(result);
        });
    }

    public void addCategory(String name, String type, String icon, long ownerId, FinanceRepository.BooleanCallback onDone) {
        repository.insertCategory(name, type, icon, ownerId, success -> {
            if (success) refreshCategories();
            onDone.onResult(success);
        });
    }

    public void updateCategory(Category category, FinanceRepository.BooleanCallback onDone) {
        repository.updateCategory(category, success -> {
            if (success) refreshCategories();
            onDone.onResult(success);
        });
    }

    public void deleteOrDeactivateCategory(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivateCategory(id, result -> {
            refreshCategories();
            onDone.onResult(result);
        });
    }

    public void addIncomeSource(String name, long ownerId, FinanceRepository.BooleanCallback onDone) {
        repository.insertIncomeSource(name, ownerId, success -> {
            if (success) refreshIncomeSources();
            onDone.onResult(success);
        });
    }

    public void updateIncomeSource(IncomeSource source, FinanceRepository.BooleanCallback onDone) {
        repository.updateIncomeSource(source, success -> {
            if (success) refreshIncomeSources();
            onDone.onResult(success);
        });
    }

    public void deleteOrDeactivateIncomeSource(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivateIncomeSource(id, result -> {
            refreshIncomeSources();
            onDone.onResult(result);
        });
    }

    public void addPaymentMethod(String name, String icon, FinanceRepository.BooleanCallback onDone) {
        repository.insertPaymentMethod(name, icon, success -> {
            if (success) refreshPaymentMethods();
            onDone.onResult(success);
        });
    }

    public void updatePaymentMethod(PaymentMethod method, FinanceRepository.BooleanCallback onDone) {
        repository.updatePaymentMethod(method, success -> {
            if (success) refreshPaymentMethods();
            onDone.onResult(success);
        });
    }

    public void deleteOrDeactivatePaymentMethod(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivatePaymentMethod(id, result -> {
            refreshPaymentMethods();
            onDone.onResult(result);
        });
    }

    public void addTransaction(Transaction transaction, FinanceRepository.BooleanCallback onDone) {
        repository.insertTransaction(transaction, success -> {
            if (success) {
                refreshTransactions(null);
                refreshAccounts();
            }
            onDone.onResult(success);
        });
    }

    public void updateTransaction(Transaction transaction, FinanceRepository.BooleanCallback onDone) {
        repository.updateTransaction(transaction, success -> {
            if (success) {
                refreshTransactions(null);
                refreshAccounts();
            }
            onDone.onResult(success);
        });
    }

    public void softDeleteTransaction(long transactionId, FinanceRepository.BooleanCallback onDone) {
        repository.softDeleteTransaction(transactionId, success -> {
            if (success) {
                refreshTransactions(null);
                refreshAccounts();
            }
            onDone.onResult(success);
        });
    }

    public void refreshSmsSenders() {
        repository.getSmsSenders(_smsSenders::postValue);
    }

    public void refreshSmsLogs() {
        repository.getSmsLogs(_smsLogs::postValue);
    }

    public void addSmsSender(String name, String keyword, long accountId, boolean isActive, FinanceRepository.BooleanCallback onDone) {
        repository.insertSmsSender(name, keyword, accountId, isActive, success -> {
            if (success) refreshSmsSenders();
            onDone.onResult(success);
        });
    }

    public void updateSmsSender(SmsSender sender, FinanceRepository.BooleanCallback onDone) {
        repository.updateSmsSender(sender, success -> {
            if (success) refreshSmsSenders();
            onDone.onResult(success);
        });
    }

    public void deleteSmsSender(long id, FinanceRepository.ResultTypeCallback onDone) {
        repository.deleteOrDeactivateSmsSender(id, result -> {
            refreshSmsSenders();
            onDone.onResult(result);
        });
    }

    public void importSmsSenders(List<String> senderNames, FinanceRepository.BooleanCallback onDone) {
        repository.importSmsSenders(senderNames, success -> {
            if (success) refreshSmsSenders();
            if (onDone != null) onDone.onResult(success);
        });
    }

    public void updateSmsLogStatus(long logId, String status, Long transactionId, FinanceRepository.BooleanCallback onDone) {
        repository.updateSmsLogStatus(logId, status, transactionId, success -> {
            if (success) refreshSmsLogs();
            onDone.onResult(success);
        });
    }

}
