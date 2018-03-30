package com.shaverz.cream.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Comparator;

public class Transaction implements Comparable<Transaction>, Parcelable{
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

    public Transaction(Parcel in) {
        String[] data = new String[6];

        in.readStringArray(data);

        this.id = data[0];
        this.amount = Double.parseDouble(data[1]);
        this.account = data[2];
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(Long.parseLong(data[3]));
        this.category = data[4];
        this.payee = data[5];
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        String id = this.id;
        String amount = String.valueOf(this.amount);
        String account = this.account;
        String date = String.valueOf(this.date.getTime().getTime());
        String category = this.category;
        String payee = this.payee;

        parcel.writeStringArray(new String[] {id,
                amount,
                account,
                date,
                category,
                payee});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

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
