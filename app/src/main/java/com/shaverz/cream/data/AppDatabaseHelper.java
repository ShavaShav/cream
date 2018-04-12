// AddressBookDatabaseHelper.java
// SQLiteOpenHelper subclass that defines the app's database
package com.shaverz.cream.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shaverz.cream.data.DB.*;

class AppDatabaseHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "Cream.db";
   private static final int DATABASE_VERSION = 1;

   // constructor
   public AppDatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   // creates the accounts table when the database is created
   @Override
   public void onCreate(SQLiteDatabase db) {
       // SQL for creating the users table
       final String CREATE_USERS_TABLE =
           "CREATE TABLE " + User.TABLE_NAME + "(" +
                   User._ID + " integer primary key, " +
                   User.COLUMN_USERNAME + " text not null unique, " +
                   User.COLUMN_PASSWORD + " text not null, " +
                   User.COLUMN_CURRENCY + " text, " +
                   User.COLUMN_LANGUAGE + " text, " +
                   User.COLUMN_OVERVIEW_ORDER + " text, " +
                   User.COLUMN_OVERVIEW_VISIBILITY + " text);";
       db.execSQL(CREATE_USERS_TABLE);

       // SQL for creating the accounts table
       // account names must be unique for user
       final String CREATE_ACCOUNTS_TABLE =
          "CREATE TABLE " + Account.TABLE_NAME + "(" +
          Account._ID + " integer primary key, " +
          Account.COLUMN_NAME + " text not null, " +
          Account.COLUMN_USER_ID + " text not null, " +
          "FOREIGN KEY (" + Account.COLUMN_USER_ID + ") " +
             "REFERENCES " + User.TABLE_NAME + "(_id) ON DELETE CASCADE " +
          "CONSTRAINT user_account UNIQUE (" +
             Account.COLUMN_USER_ID + ", " + Account.COLUMN_NAME + "));";
       db.execSQL(CREATE_ACCOUNTS_TABLE);

       // SQL for creating the transactions table
       final String CREATE_TRANSACTIONS_TABLE =
           "CREATE TABLE " + Transaction.TABLE_NAME + "(" +
           Transaction._ID + " integer primary key, " +
           Transaction.COLUMN_ACCOUNT_ID + " text not null, " +
           Transaction.COLUMN_AMOUNT + " double default 0.0, " +
           Transaction.COLUMN_DATE + " timestamp not null, " +
           Transaction.COLUMN_CATEGORY + " text not null, " +
           Transaction.COLUMN_PAYEE + " text not null, " +
           "FOREIGN KEY (" + Transaction.COLUMN_ACCOUNT_ID + ") " +
           "REFERENCES " + Account.TABLE_NAME + "(_id) ON DELETE CASCADE)";

       db.execSQL(CREATE_TRANSACTIONS_TABLE);
   }

   // normally defines how to upgrade the database when the schema changes
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion,
                         int newVersion) {

      // on upgrade drop older tables
      db.execSQL("DROP TABLE IF EXISTS " + Transaction.TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + Account.TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_NAME);

      // create new tables
      onCreate(db);
   }
}
