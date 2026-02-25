package com.example.submanager.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private double amount;
    private String currency; // "MXN" or "USD"
    private String category; // streaming, home, software, etc.
    private long billingDate; // timestamp
    private String notes;
    private boolean isPaid;
    private String iconUrl;
    private int colorRes;

    // Constructors
    public Expense() {}

    public Expense(String name, double amount, String currency, String category,
                   long billingDate, String notes, boolean isPaid, String iconUrl, int colorRes) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.billingDate = billingDate;
        this.notes = notes;
        this.isPaid = isPaid;
        this.iconUrl = iconUrl;
        this.colorRes = colorRes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getBillingDate() { return billingDate; }
    public void setBillingDate(long billingDate) { this.billingDate = billingDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public int getColorRes() { return colorRes; }
    public void setColorRes(int colorRes) { this.colorRes = colorRes; }
}

