package com.shaverz.cream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    public static final String DEFAULT_CURRENCY = "CAD";
    public static final String DEFAULT_LANGUAGE = "English";

    public static final String TAG = "Cream";

    public static final String PREF_USER_URI = "user_uri";

    public static final boolean DEBUG = true;

    public static void storeCurrentUserURI(Context context, Uri userURI) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_USER_URI, userURI.toString());
        editor.apply();
    }

    public static Uri getCurrentUserURI(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return Uri.parse(sharedPref.getString(PREF_USER_URI, ""));
    }

    public static String getCurrentUserID(Context context) {
        return getCurrentUserURI(context).getLastPathSegment();
    }

    public static User fetchUserModel (Context context) {
        User user = null;
        String userId = Utils.getCurrentUserID(context);

        Cursor cursor = context.getContentResolver().query(
                getCurrentUserURI(context),
                null,
                DB.User._ID + " = " + userId,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_USERNAME));
            String currency = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_CURRENCY));
            String language = cursor.getString(cursor.getColumnIndex(DB.User.COLUMN_LANGUAGE));

            user = new User(userId, name, currency, language);
        } else {
            Log.e(TAG, "No user found with id: " + userId);
            if (cursor != null) cursor.close();
            return null;
        }

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

    // customizes a textview with color depending on currency +/- and locale
    public static void setCurrencyTextView (Context context, TextView currencyTextView, double amount) {
        // Format currency according to locale from main activity
        currencyTextView.setText(
                NumberFormat
                        .getCurrencyInstance(MainActivity.CURRENT_USER.getCurrencyLocale())
                        .format(amount));

        // Set to color depending on expense or income
        if (amount > 0.00) {
            currencyTextView.setTextColor(context.getResources().getColor(R.color.text_positive));
        } else if (amount < 0.00 ){
            currencyTextView.setTextColor(context.getResources().getColor(R.color.text_negative));
        } else {
            currencyTextView.setTextColor(context.getResources().getColor(R.color.text_neutral));
        }
    }

    public static Locale convertToLocale(String currencyCode) {
        switch (currencyCode) {
            case "CAN":
                return Locale.CANADA;
            case "EUR":
                return Locale.GERMAN;
            case "GBP":
                return Locale.UK;
            case "JPY":
                return Locale.JAPAN;
            case "USD":
                return Locale.US;
            default:
                return Locale.CANADA;
        }
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
