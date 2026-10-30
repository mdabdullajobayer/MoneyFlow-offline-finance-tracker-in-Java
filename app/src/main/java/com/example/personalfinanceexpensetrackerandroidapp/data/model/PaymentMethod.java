package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class PaymentMethod {
    private final long id;
    private final String name;
    private final String icon;
    private final boolean isActive;

    public PaymentMethod(long id, String name, String icon, boolean isActive) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public boolean isActive() { return isActive; }
}
