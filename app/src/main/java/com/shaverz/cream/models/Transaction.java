package com.shaverz.cream.models;


import java.util.Calendar;
import java.util.Comparator;

public class Transaction implements Comparable<Transaction> {
    private String id;
    private double amount;
    private String account; // storing account name redundantly for easy adapter access
    private Calendar date;
    private String category;
    private String payee;

    public Transaction(String id, double amount, String account, Calendar date, String category, String payee) {
        this.id = id;
        this.amount = amount;
        this.account = account;
        this.date = date;
        this.category = category;
        this.payee = payee;
    }

    public String getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public Calendar getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getPayee() {
        return payee;
    }

    @Override
    public String toString() {
        return id + "| " + account + " | "  + category + " | " +
                payee + ": $" + amount + " @ " + date.toString();
    }

    // Sorts transactions
    public static class TransactionComparator implements Comparator<Transaction> {

        @Override
        public int compare(Transaction transaction, Transaction t1) {
            return transaction.compareTo(t1);
        }

    }

    // Latest -> Oldest
    @Override
    public int compareTo(Transaction transaction) {
        return -this.date.compareTo(transaction.date);
    }
}
