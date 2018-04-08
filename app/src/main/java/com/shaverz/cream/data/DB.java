// DatabaseDescription.java
// Describes the table name and column names for this app's database,
// and other information required by the ContentProvider
package com.shaverz.cream.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DB {
   // ContentProvider's name: typically the package name
   public static final String AUTHORITY =
      "com.shaverz.cream.data";

   // base URI used to interact with the ContentProvider
   private static final Uri BASE_CONTENT_URI =
      Uri.parse("content://" + AUTHORITY);

   // nested class defines contents of the contacts table
   public static final class Account implements BaseColumns {
      public static final String TABLE_NAME = "accounts"; // table's name

      // Uri for the contacts table
      public static final Uri CONTENT_URI =
              BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

      // column names for contacts table's columns
      public static final String COLUMN_NAME = "name";
      public static final String COLUMN_USER_ID = "user_id";

      // creates a Uri for a specific account
      public static Uri buildAccountUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
   }

   // nested class defines contents of the transactions table
   public static final class Transaction implements BaseColumns {
      public static final String TABLE_NAME = "transactions"; // table's name

      // Uri for the transactions table
      public static final Uri CONTENT_URI =
              BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

      // column names for transactions table's columns
      public static final String COLUMN_AMOUNT = "amount";
      public static final String COLUMN_DATE = "date";
      public static final String COLUMN_CATEGORY = "category";
      public static final String COLUMN_PAYEE = "payee";
      public static final String COLUMN_ACCOUNT_ID = "account_id";

      // creates a Uri for a specific transaction
      public static Uri buildTransactionUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
   }

   // nested class defines contents of the users table
   public static final class User implements BaseColumns {
      public static final String TABLE_NAME = "users"; // table's name

      // Uri for the users table
      public static final Uri CONTENT_URI =
              BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

      // column names for users table's columns
      public static final String COLUMN_USERNAME = "username";
      public static final String COLUMN_PASSWORD = "password";
      public static final String COLUMN_CURRENCY = "currency";
      public static final String COLUMN_LANGUAGE = "language";
      public static final String COLUMN_OVERVIEW_ORDER = "overview_order";
      public static final String COLUMN_OVERVIEW_VISIBILITY = "overview_visibility";


      // creates a Uri for a specific user
      public static Uri buildUserUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
   }
}