package com.shaverz.cream.models;


import java.util.Date;

public class Transaction {
        public final static int WITHDRAWAL = 0;
        public final static int DEPOSIT = 1;

        public final String id;
        public final String accountName;
        public final int type; // withdrawal or deposit
        public final double amount;
        public final Date date;

        public Transaction(String id, double amount, int type, String accountName, Date date) throws Exception {
            this.id = id;
            this.accountName = accountName;
            if (type != WITHDRAWAL && type != DEPOSIT) {
                throw new Exception("Invalid type!");
            }
            if (amount < 0.00) {
                throw new Exception("Amount must be positive!");
            }
            this.type = type;
            this.amount = amount;
            this.date = date;
        }

        @Override
        public String toString() {
            return accountName + ": " + (type == WITHDRAWAL ? "-":"+") + amount;
        }
}
