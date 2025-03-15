package com.example.sparfuchs.backend;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id; 
    private double amount;
    private String description;
    private String date;
    private String category;

    public TransactionEntity(String date, String description, double amount) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category ="";
    }

    public TransactionEntity() {}

    // Getter & Setter
    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public void setId(int id) {
        this.id = id;
    }

    private Category assignCategory(List<Category> pCategories) {
        TransactionAnalyser transactionAnalyser = new TransactionAnalyser(pCategories);
        return transactionAnalyser.getCategory(this);
    }

    @Override
    public String toString() {
        return "---------------\nTransaktion\n" +
                "ID: " + id +
                "\nWert: " + amount +
                "\nBeschreibung: " + description +
                "\nDatum: " + date +
                "\nKategorie: " + category;
    }
}
