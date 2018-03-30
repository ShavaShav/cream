package com.shaverz.cream.models;

import com.shaverz.cream.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by z on 27/03/2018.
 */

public class User {
    private String id;
    private String username;
    private Locale currencyLocale;
    private String language;

    private List<Account> accountList;

    public User(String id, String username, String currency, String language) {
        this.id = id;
        this.username = username;
        this.currencyLocale = Utils.convertToLocale(currency);
        this.language = language;
        this.accountList = new ArrayList<>();
    }

    public double getBalance () {
        double balance = 0;

        for (Account account : accountList) {
            balance += account.getBalance();
        }
        return balance;
    }

    public Account getAccount(String id) {
        for (Account a : accountList) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Locale getCurrencyLocale() {
        return currencyLocale;
    }

    public String getLanguage() {
        return language;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }

    public void addAccount(Account a) {
        this.accountList.add(a);
    }

    public List<Transaction> getTransactions(){
        List<Transaction> list = new ArrayList<>();
        for (Account account : accountList) {
            list.addAll(account.getTransactionList());
        }
        Collections.sort(list);
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "(" + id + ")" + username + ": " + language + " - " + currencyLocale);
        for (Account account : accountList) {
            sb.append("\n-> " + account.treeString());
        }
        return sb.toString();
    }
}
