// AddressBookContentProvider.java
// ContentProvider subclass for manipulating the app's database
package com.shaverz.cream.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.shaverz.cream.R;
import com.shaverz.cream.data.DB.*;

public class AppContentProvider extends ContentProvider {
   // used to access the database
   private AppDatabaseHelper dbHelper;

   // UriMatcher helps ContentProvider determine operation to perform
   private static final UriMatcher uriMatcher =
      new UriMatcher(UriMatcher.NO_MATCH);

   // constants used with UriMatcher to determine operation to perform
   private static final int USERS = 0; // manipulate users table
   private static final int ONE_USER = 1; // manipulate users table
   private static final int ONE_ACCOUNT = 2; // manipulate one account
   private static final int ACCOUNTS = 3; // manipulate accounts table - does join with transactions to get balance
   private static final int ACCOUNTS_FOR_USER = 4; // manipulate accounts table - does join with transactions to get balance
   private static final int ONE_TRANSACTION = 5; // manipulate one transaction
   private static final int TRANSACTIONS = 6; // manipulate transaction table
   private static final int TRANSACTIONS_FOR_ACCOUNT = 7; // manipulate transaction table - fetch
   private static final int TRANSACTIONS_FOR_USER = 8; // manipulate transaction table -fetch

   // static block to configure this ContentProvider's UriMatcher
   static {
      // Uri for Account with the specified id (#) - For update, delete, fetch
      uriMatcher.addURI(DB.AUTHORITY,
              User.TABLE_NAME + "/#", ONE_USER);

      // Uri for Users table - insert
      uriMatcher.addURI(DB.AUTHORITY,
              User.TABLE_NAME, USERS);

      // Uri for Account with the specified id (#) - For update, delete, fetch
      uriMatcher.addURI(DB.AUTHORITY,
         Account.TABLE_NAME + "/#", ONE_ACCOUNT);

      // Uri for Accounts table - insert
      uriMatcher.addURI(DB.AUTHORITY,
              Account.TABLE_NAME, ACCOUNTS);

      // Uri for Accounts table for user (#) - fetch
      uriMatcher.addURI(DB.AUTHORITY,
         Account.TABLE_NAME + "/" + User.TABLE_NAME + "/#", ACCOUNTS_FOR_USER);

      // Uri for Transactions - insert
      uriMatcher.addURI(DB.AUTHORITY,
              Transaction.TABLE_NAME, TRANSACTIONS);

      // Uri for Transaction with the specified id (#) - For update, delete, fetch
      uriMatcher.addURI(DB.AUTHORITY,
              Transaction.TABLE_NAME + "/#", ONE_TRANSACTION);

      // Uri for Transactions for specific account id (#) - fetch
      uriMatcher.addURI(DB.AUTHORITY,
              Transaction.TABLE_NAME + "/" + Account.TABLE_NAME + "/#", TRANSACTIONS_FOR_ACCOUNT);

      // Uri for Transactions for specific user id (#) - fetch
      uriMatcher.addURI(DB.AUTHORITY,
              Transaction.TABLE_NAME + "/" + User.TABLE_NAME + "/#", TRANSACTIONS_FOR_USER);
   }

   // called when the AppContentProvider is created
   @Override
   public boolean onCreate() {
      // create the AppDatabaseHelper
      dbHelper = new AppDatabaseHelper(getContext());
      return true; // ContentProvider successfully created
   }

   // required method: Not used in this app, so we return null
   @Override
   public String getType(Uri uri) {
      return null;
   }

   // query the database
   @Override
   public Cursor query(Uri uri, String[] projection,
                       String selection, String[] selectionArgs, String sortOrder) {

      // create SQLiteQueryBuilder for querying table
      SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

      switch (uriMatcher.match(uri)) {
         case ONE_USER: // all users will be selected
            queryBuilder.setTables(User.TABLE_NAME);
            queryBuilder.appendWhere(
                    User._ID + "=" + uri.getLastPathSegment());
            break;
         case USERS: // all users will be selected
            queryBuilder.setTables(User.TABLE_NAME);
            break;
         case ONE_ACCOUNT: // account with specified id will be selected
            queryBuilder.setTables(Account.TABLE_NAME);
            queryBuilder.appendWhere(
               Account._ID + "=" + uri.getLastPathSegment());
            break;
         case ACCOUNTS: // all accounts will be selected
            queryBuilder.setTables(Account.TABLE_NAME);
            break;
         case ACCOUNTS_FOR_USER: // join of account and user table, for specific user
            queryBuilder.setTables(
                  Account.TABLE_NAME
                  + " INNER JOIN "
                  + User.TABLE_NAME
                  + " ON "
                  + Account.TABLE_NAME + "." + Account.COLUMN_USER_ID
                  + "="
                  + User.TABLE_NAME + "." + User._ID);
            queryBuilder.appendWhere(
                    Account.COLUMN_USER_ID + "=" + uri.getLastPathSegment());
            break;
         case ONE_TRANSACTION: // transaction with specified id will be selected
            queryBuilder.setTables(Transaction.TABLE_NAME);
            queryBuilder.appendWhere(
                    Transaction._ID + "=" + uri.getLastPathSegment());
            break;
         case TRANSACTIONS: // all transactions will be selected
            queryBuilder.setTables(Transaction.TABLE_NAME);
            break;
         case TRANSACTIONS_FOR_USER:
            queryBuilder.setTables(
                    User.TABLE_NAME
                    + " INNER JOIN "
                    + Account.TABLE_NAME
                    + " ON "
                    + User.TABLE_NAME + "." + User._ID
                    + "="
                    + Account.TABLE_NAME + "." + Account.COLUMN_USER_ID
                    + " INNER JOIN "
                    + Transaction.TABLE_NAME
                    + " ON "
                    + Transaction.TABLE_NAME + "." + Transaction.COLUMN_ACCOUNT_ID
                    + "="
                    + Account.TABLE_NAME + "." + Account._ID);
            break;
         case TRANSACTIONS_FOR_ACCOUNT:
            queryBuilder.setTables(Account.TABLE_NAME
                    + " INNER JOIN "
                    + Transaction.TABLE_NAME
                    + " ON "
                    + Account.TABLE_NAME + "." + Account._ID
                    + "="
                    + Transaction.TABLE_NAME + "." +Transaction.COLUMN_ACCOUNT_ID);
            queryBuilder.appendWhere(
                    Account.COLUMN_USER_ID + "=" + uri.getLastPathSegment());
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_query_uri) + uri);
      }

      // execute the query to select one or all accounts
      Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
         projection, selection, selectionArgs, null, null, sortOrder);

      // configure to watch for content changes
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
      return cursor;
   }

   // insert a new account/user in the database
   @Override
   public Uri insert(Uri uri, ContentValues values) {
      Uri newUri = null;
      long id = 0;

      switch (uriMatcher.match(uri)) {
         case ACCOUNTS:
            // insert the new account--success yields new account's row id
            id = dbHelper.getWritableDatabase().insert(
               Account.TABLE_NAME, null, values);

            // if the account was inserted, create an appropriate Uri;
            // otherwise, throw an exception
            if (id > 0) { // SQLite row IDs start at 1
               newUri = Account.buildAccountUri(id);

               // notify observers that the database changed
               getContext().getContentResolver().notifyChange(uri, null);
            }
            else
               throw new SQLException(
                  getContext().getString(R.string.insert_failed) + uri);
            break;
         case TRANSACTIONS:
            // insert the new account--success yields new account's row id
            id = dbHelper.getWritableDatabase().insert(
                    Transaction.TABLE_NAME, null, values);

            // if the account was inserted, create an appropriate Uri;
            // otherwise, throw an exception
            if (id > 0) { // SQLite row IDs start at 1
               newUri = Transaction.buildTransactionUri(id);

               // notify observers that the database changed
               getContext().getContentResolver().notifyChange(uri, null);
            }
            else
               throw new SQLException(
                 getContext().getString(R.string.insert_failed) + uri);
            break;
         case USERS:
            // insert the new user--success yields new users's row id
            id = dbHelper.getWritableDatabase().insert(
                    User.TABLE_NAME, null, values);

            // if the user was inserted, create an appropriate Uri;
            // otherwise, throw an exception
            if (id > 0) { // SQLite row IDs start at 1
               newUri = User.buildUserUri(id);

               // notify observers that the database changed
               getContext().getContentResolver().notifyChange(uri, null);
            }
            else
               throw new SQLException(
                       getContext().getString(R.string.insert_failed) + uri);
            break;
         case ACCOUNTS_FOR_USER:

            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_insert_uri) + uri);
      }

      return newUri;
   }

   // update an existing account in the database
   @Override
   public int update(Uri uri, ContentValues values,
                     String selection, String[] selectionArgs) {
      int numberOfRowsUpdated; // 1 if update successful; 0 otherwise
      String id;

      switch (uriMatcher.match(uri)) {
         case ONE_ACCOUNT:
            // get from the uri the id of account to update
            id = uri.getLastPathSegment();

            // update the account
            numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
               Account.TABLE_NAME, values, Account._ID + "=" + id,
               selectionArgs);
            break;
         case ONE_TRANSACTION:
            // get from the uri the id of transaction to update
            id = uri.getLastPathSegment();

            // update the transaction
            numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
               Transaction.TABLE_NAME, values, Transaction._ID + "=" + id,
               selectionArgs);
            break;
         case ONE_USER:
            // get from the uri the id of transaction to update
            id = uri.getLastPathSegment();

            // update the transaction
            numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
                    User.TABLE_NAME, values, User._ID + "=" + id,
                    selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_update_uri) + uri);
      }

      // if changes were made, notify observers that the database changed
      if (numberOfRowsUpdated != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsUpdated;
   }

   // delete an existing account from the database
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      int numberOfRowsDeleted;
      String id;

      switch (uriMatcher.match(uri)) {
         case ONE_ACCOUNT:
            // get from the uri the id of account to delete
            id = uri.getLastPathSegment();

            // delete the account
            numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
               Account.TABLE_NAME, Account._ID + "=" + id, selectionArgs);
            break;
         case ONE_TRANSACTION:
            // get from the uri the id of transaction to delete
            id = uri.getLastPathSegment();

            // delete the transaction
            numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
               Transaction.TABLE_NAME, Transaction._ID + "=" + id, selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_delete_uri) + uri);
      }

      // notify observers that the database changed
      if (numberOfRowsDeleted != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsDeleted;
   }
}
