package com.shaverz.cream.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;

import java.text.NumberFormat;
import java.util.Locale;

public class CommonUtils {
    public static final String DEFAULT_CURRENCY = "CAD";
    public static final String DEFAULT_LANGUAGE = "English";
    public static final String TAG = "Cream";
    public static final String PREF_USER_URI = "user_uri";

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

    public static String getCurrencyString (double amount) {
        return NumberFormat
                .getCurrencyInstance(MainActivity.CURRENT_USER.getCurrencyLocale())
                .format(amount);
    }

    // customizes a textview with color depending on currency +/- and locale
    public static void setCurrencyTextView (Context context, TextView currencyTextView, double amount) {
        // Format currency according to locale from main activity
        currencyTextView.setText(getCurrencyString(amount));

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

}
