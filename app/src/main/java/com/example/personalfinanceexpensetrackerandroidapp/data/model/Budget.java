package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class Budget {
    private final long id;
    private final long ownerId;
    private final long categoryId;
    private final double amount;
    private final String startDate;
    private final String endDate;
    private final String period;

    public Budget(long id, long ownerId, long categoryId, double amount, String startDate, String endDate, String period) {
        this.id = id;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public long getCategoryId() { return categoryId; }
    public double getAmount() { return amount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getPeriod() { return period; }
}
