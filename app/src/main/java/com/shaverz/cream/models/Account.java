package com.shaverz.cream.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by z on 27/03/2018.
 */

public class Account implements Parcelable{
    private String id;
    private String name;
    private List<Transaction> transactionList;

    public Account(String id, String name) {
        this.id = id;
        this.name = name;
        transactionList = new ArrayList<>();
    }

    public Account(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);

        this.id = data[0];
        this.name = data[1];
        transactionList = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.id, this.name});
    }

    public double getBalance() {
        double balance = 0;

        for (Transaction tx : transactionList) {
            balance += tx.getAmount();
        }
        return balance;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void addTransaction(Transaction t) {
        transactionList.add(t);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String treeString() {
        StringBuilder sb = new StringBuilder("(" + id + ")" + name);
        for (Transaction tx : transactionList) {
            sb.append("\n\t" + tx);
        }
        return sb.toString();
    }
}
