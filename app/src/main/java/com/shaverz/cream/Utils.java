package com.shaverz.cream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static void storeCurrentUserId(Activity context, String userId) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.preference_user_id), userId);
        editor.apply();
    }

    public static String getCurrentUserId(Activity context) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.preference_user_id), "");
    }

}
