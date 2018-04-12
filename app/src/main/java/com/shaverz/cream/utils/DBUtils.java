package com.shaverz.cream.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.shaverz.cream.R;
import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DBUtils {

    public static User fetchUserModel (Context context) {
        User user = null;
        String userId = CommonUtils.getCurrentUserID(context);

        Cursor cursor = context.getContentResolver().query(
                CommonUtils.getCurrentUserURI(context),
                null,
                DB.User._ID + " = " + userId,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_USERNAME));
            String currency = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_CURRENCY));
            String language = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_LANGUAGE));
            String overviewVisibilityString = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_OVERVIEW_VISIBILITY));
            String overviewOrderString = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_OVERVIEW_ORDER));
            user = new User(userId, name, currency, language, overviewVisibilityString, overviewOrderString);
        } else {
            Log.e(CommonUtils.TAG, "No user found with id: " + userId);
            if (cursor != null) cursor.close();
            return null;
        }

        cursor.close();

        // Get accounts for current user
        cursor = context.getContentResolver().query(
                DB.Account.CONTENT_URI,
                null,
                DB.Account.COLUMN_USER_ID + " = " + userId,
                null,
                null);

        if (cursor == null || cursor.getCount() <= 0) return user; // no accounts

        List<Account> accounts = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(DB.Account._ID));
            String name = cursor.getString(cursor.getColumnIndex(DB.Account.COLUMN_NAME));
            accounts.add(new Account(id, name));
        }

        for (Account account : accounts) {
            if (cursor != null) cursor.close();
            // get transactions for account
            cursor = context.getContentResolver().query(
                    DB.Transaction.CONTENT_URI,
                    null,
                    DB.Transaction.COLUMN_ACCOUNT_ID + " = ?",
                    new String[] { account.getId() },
                    null);

            if (cursor == null || cursor.getCount() <= 0) continue;

            // convert each to transaction object and add to account
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(DB.Transaction._ID));
                double amount = cursor.getDouble(cursor.getColumnIndex(DB.Transaction.COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_CATEGORY));
                String payee = cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_PAYEE));
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_DATE))));
                account.addTransaction(new Transaction(id, amount, account.getName(), date, category, payee));
            }
        }
        cursor.close();
        user.setAccountList(accounts);
        return user;
    }

    public static Uri createNewAccount(Context context, String name, double openingBalance) {
        // insert account
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB.Account.COLUMN_USER_ID, CommonUtils.getCurrentUserID(context));
        contentValues.put(DB.Account.COLUMN_NAME, name);

        String accountID = context.getContentResolver()
                .insert(DB.Account.CONTENT_URI, contentValues)
                .getLastPathSegment();

        contentValues.clear();
        contentValues.put(DB.Transaction.COLUMN_ACCOUNT_ID, accountID);
        contentValues.put(DB.Transaction.COLUMN_AMOUNT, openingBalance);
        contentValues.put(DB.Transaction.COLUMN_CATEGORY, context.getString(R.string.opening_balance_category));
        contentValues.put(DB.Transaction.COLUMN_DATE, System.currentTimeMillis());
        contentValues.put(DB.Transaction.COLUMN_PAYEE, context.getString(R.string.opening_balance_payee));

        String transactionID = context.getContentResolver()
                .insert(DB.Transaction.CONTENT_URI, contentValues)
                .getLastPathSegment();

        return DB.Account.buildAccountUri(Long.parseLong(accountID));
    }

    // Fetches user model asynchronously
    public static class UserLoader extends AsyncTaskLoader<User> {

        public UserLoader(Context context) {
            super(context);
        }

        @Override
        public User loadInBackground() {
            return fetchUserModel(super.getContext());
        }
    }
}
