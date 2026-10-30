package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class SmsLog {
    private long id;
    private String sender;
    private String messageBody;
    private String receivedDate;
    private double parsedAmount;
    private String transactionType; // "IN" or "OUT"
    private String reference;
    private String status; // "PENDING", "APPROVED", "REJECTED", "IGNORED"
    private Long transactionId;
    private long accountId;

    public SmsLog(long id, String sender, String messageBody, String receivedDate, double parsedAmount, String transactionType, String reference, String status, Long transactionId, long accountId) {
        this.id = id;
        this.sender = sender;
        this.messageBody = messageBody;
        this.receivedDate = receivedDate;
        this.parsedAmount = parsedAmount;
        this.transactionType = transactionType;
        this.reference = reference;
        this.status = status;
        this.transactionId = transactionId;
        this.accountId = accountId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }

    public double getParsedAmount() { return parsedAmount; }
    public void setParsedAmount(double parsedAmount) { this.parsedAmount = parsedAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
}
