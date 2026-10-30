package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class TransactionType {
    private final long id;
    private final String name;
    private final boolean isActive;

    public TransactionType(long id, String name, boolean isActive) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
}
