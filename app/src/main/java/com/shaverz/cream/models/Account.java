package com.shaverz.cream.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by z on 27/03/2018.
 */

public class Account {
    private String id;
    private String name;
    private List<Transaction> transactionList;

    public Account(String id, String name) {
        this.id = id;
        this.name = name;
        transactionList = new ArrayList<>();
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
