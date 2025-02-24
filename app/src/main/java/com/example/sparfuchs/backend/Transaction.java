package com.example.sparfuchs.backend;

public class Transaction {

    private double amount;
    private String description;
    private String date;
    private String category;


    public Transaction(String date, String description, double amount) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        setCategory();
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
        this.category= category;


    }

    public void setCategory() {
        TransactionAnalyser transactionAnalyser= new TransactionAnalyser();
        transactionAnalyser.setCategory(this);


    }


    @Override
    public String toString() {
        return "---------------\nTransaktion\n" +
                "Wert: " + amount +
                "\nBeschreibung: " + description +
                "\nDatum: " + date +
                "\nKategorie: "+ category;
    }
}
