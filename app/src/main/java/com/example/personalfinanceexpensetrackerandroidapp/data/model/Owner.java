package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class Owner {
    private final long id;
    private final String name;
    private final String type;
    private final boolean isDefault;
    private final boolean isActive;

    public Owner(long id, String name, String type, boolean isDefault, boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isDefault = isDefault;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isDefault() { return isDefault; }
    public boolean isActive() { return isActive; }
}
