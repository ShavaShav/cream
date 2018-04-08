package com.shaverz.cream.models;

import android.util.Log;

import com.shaverz.cream.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Calendar;
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
    private OverviewCustomization overviewCustomization;

    public User(String id, String username, String currency, String language,
                String visibleCustomization, String orderCustomization) {
        this.id = id;
        this.username = username;
        this.currencyLocale = CommonUtils.convertToLocale(currency);
        this.language = language;
        this.accountList = new ArrayList<>();
        this.overviewCustomization = new OverviewCustomization(visibleCustomization, orderCustomization);
    }

    public double getAmountSpentToday() {
        Calendar dayAgo = Calendar.getInstance();
        dayAgo.add(Calendar.DAY_OF_YEAR, -1);
        double amount = 0.0;
        for (Transaction tx : getTransactions()) {
            if (tx.getDate().after(dayAgo) && tx.getAmount() < 0.0) {
                amount += tx.getAmount();
            }
        }
        return -(amount);
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

    // can do this since Account names are unique for user
    public Account findAccountByName(String name) {
        for (Account a : accountList) {
            if (a.getName().equals(name))
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

    public OverviewCustomization getOverviewCustomization() {
        return overviewCustomization;
    }

}
