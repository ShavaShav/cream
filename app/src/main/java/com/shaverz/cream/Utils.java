package com.shaverz.cream;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    public static final boolean DEV = true;

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
        return Uri.parse(sharedPref.getString(PREF_USER_URI, DEV ? "1" : "")); // use 1st user if dev
    }

    public static String getCurrentUserID(Context context) {
        return getCurrentUserURI(context).getLastPathSegment();
    }

    public static String toISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public static Date fromISO8601UTC(String dateStr) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}
