package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class IncomeSource {
    private final long id;
    private final String name;
    private final long ownerId;
    private final boolean isActive;

    public IncomeSource(long id, String name, long ownerId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public long getOwnerId() { return ownerId; }
    public boolean isActive() { return isActive; }
}
