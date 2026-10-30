package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class Transaction {
    private final long id;
    private final long ownerId;
    private final long accountId;
    private final Long destinationAccountId;
    private final long transactionTypeId;
    private final Long categoryId;
    private final Long incomeSourceId;
    private final long paymentMethodId;
    private final long amount;
    private final String date;
    private final String note;
    private final String payeePayer;
    private final boolean isRecurring;
    private final String attachmentUri;
    private final boolean isActive;

    private final String ownerName;
    private final String accountName;
    private final String destinationAccountName;
    private final String transactionTypeName;
    private final String categoryName;
    private final String incomeSourceName;
    private final String paymentMethodName;

    public Transaction(long id, long ownerId, long accountId, Long destinationAccountId,
                       long transactionTypeId, Long categoryId, Long incomeSourceId,
                       long paymentMethodId, long amount, String date, String note,
                       String payeePayer, boolean isRecurring, String attachmentUri, boolean isActive,
                       String ownerName, String accountName, String destinationAccountName,
                       String transactionTypeName, String categoryName, String incomeSourceName,
                       String paymentMethodName) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountId = accountId;
        this.destinationAccountId = destinationAccountId;
        this.transactionTypeId = transactionTypeId;
        this.categoryId = categoryId;
        this.incomeSourceId = incomeSourceId;
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.payeePayer = payeePayer;
        this.isRecurring = isRecurring;
        this.attachmentUri = attachmentUri;
        this.isActive = isActive;
        this.ownerName = ownerName;
        this.accountName = accountName;
        this.destinationAccountName = destinationAccountName;
        this.transactionTypeName = transactionTypeName;
        this.categoryName = categoryName;
        this.incomeSourceName = incomeSourceName;
        this.paymentMethodName = paymentMethodName;
    }

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public long getAccountId() { return accountId; }
    public Long getDestinationAccountId() { return destinationAccountId; }
    public long getTransactionTypeId() { return transactionTypeId; }
    public Long getCategoryId() { return categoryId; }
    public Long getIncomeSourceId() { return incomeSourceId; }
    public long getPaymentMethodId() { return paymentMethodId; }
    public long getAmount() { return amount; }
    public String getDate() { return date; }
    public String getNote() { return note; }
    public String getPayeePayer() { return payeePayer; }
    public boolean isRecurring() { return isRecurring; }
    public String getAttachmentUri() { return attachmentUri; }
    public boolean isActive() { return isActive; }
    public String getOwnerName() { return ownerName; }
    public String getAccountName() { return accountName; }
    public String getDestinationAccountName() { return destinationAccountName; }
    public String getTransactionTypeName() { return transactionTypeName; }
    public String getCategoryName() { return categoryName; }
    public String getIncomeSourceName() { return incomeSourceName; }
    public String getPaymentMethodName() { return paymentMethodName; }
}
