package com.example.submanager.data.model;

public class PopularService {
    private String name;
    private String category;
    private int iconRes;
    private int colorRes;

    public PopularService(String name, String category, int iconRes, int colorRes) {
        this.name = name;
        this.category = category;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getIconRes() { return iconRes; }
    public int getColorRes() { return colorRes; }
}

