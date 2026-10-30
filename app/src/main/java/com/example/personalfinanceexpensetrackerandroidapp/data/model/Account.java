package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class Account {
    private final long id;
    private final String name;
    private final String type;
    private final double balance;
    private final long balanceMinorUnits;
    private final String currency;
    private final String icon;
    private final boolean isActive;

    public Account(long id, String name, String type, double balance, long balanceMinorUnits, String currency, String icon, boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.balanceMinorUnits = balanceMinorUnits;
        this.currency = currency;
        this.icon = icon;
        this.isActive = isActive;
    }

    public Account(long id, String name, String type, double balance, String currency, String icon, boolean isActive) {
        this(id, name, type, balance, Math.round(balance * 100.0), currency, icon, isActive);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getBalance() { return balance; }
    public long getBalanceMinorUnits() { return balanceMinorUnits; }
    public String getCurrency() { return currency; }
    public String getIcon() { return icon; }
    public boolean isActive() { return isActive; }
}
