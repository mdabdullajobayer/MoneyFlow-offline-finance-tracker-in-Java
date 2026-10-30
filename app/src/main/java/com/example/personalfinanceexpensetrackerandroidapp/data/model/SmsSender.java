package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class SmsSender {
    private long id;
    private String senderName;
    private String keywordFilter;
    private long accountId;
    private String accountName;
    private boolean isActive;

    public SmsSender(long id, String senderName, String keywordFilter, long accountId, String accountName, boolean isActive) {
        this.id = id;
        this.senderName = senderName;
        this.keywordFilter = keywordFilter;
        this.accountId = accountId;
        this.accountName = accountName;
        this.isActive = isActive;
    }

    public SmsSender(long id, String senderName, String keywordFilter, long accountId, boolean isActive) {
        this(id, senderName, keywordFilter, accountId, null, isActive);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getKeywordFilter() { return keywordFilter; }
    public void setKeywordFilter(String keywordFilter) { this.keywordFilter = keywordFilter; }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
