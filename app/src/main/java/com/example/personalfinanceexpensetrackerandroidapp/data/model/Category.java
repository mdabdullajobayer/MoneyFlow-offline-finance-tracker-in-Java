package com.example.personalfinanceexpensetrackerandroidapp.data.model;

public class Category {
    private final long id;
    private final String name;
    private final String type;
    private final String icon;
    private final long ownerId;
    private final boolean isActive;

    public Category(long id, String name, String type, String icon, long ownerId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.ownerId = ownerId;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
    public long getOwnerId() { return ownerId; }
    public boolean isActive() { return isActive; }
}
