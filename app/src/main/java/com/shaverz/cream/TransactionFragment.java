package com.shaverz.cream;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Transaction>> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private User userModel;
    private View mView;
    private TransactionRecyclerViewAdapter adapter;

    private static final int LIST_LOADER = 0;

    public TransactionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transactions, container, false);

        Log.d(Utils.TAG, "Creating view");

        // This stops layout skipping while data is being loaded
        final RecyclerView transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        TransactionRecyclerViewAdapter adapter = new TransactionRecyclerViewAdapter(new ArrayList<Transaction>());
        transactionRecyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(LIST_LOADER, null, this).forceLoad();

        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        super.onResume();

        // Reload userModel
      //  getLoaderManager().restartLoader(LIST_LOADER, null, this);

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_transactions));
    }

    @Override
    public Loader<List<Transaction>> onCreateLoader(int id, Bundle args) {
        // create the user loader - fetches all user data so views stay in sync
        TaskLoader loader = new TaskLoader(this.getContext());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Transaction>> loader, List<Transaction> transactions) {

        // get list view
        final RecyclerView transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);

        // create adapter and set view to use list
        TransactionRecyclerViewAdapter adapter = new TransactionRecyclerViewAdapter(transactions);
        transactionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Transaction>> loader) {
        // destroy adapter
        final RecyclerView transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        transactionRecyclerView.setAdapter(null);
    }

    // Fetches tasks asynchronously
    public static class TaskLoader extends AsyncTaskLoader<List<Transaction>> {
        private Context context;

        public TaskLoader(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public List<Transaction> loadInBackground() {
            Cursor cursor = context.getContentResolver().query(
                DB.Transaction.buildUserTransactionUri(Long.parseLong(Utils.getCurrentUserID(getContext()))),
                null,
                null,
                null,
                null
            );

            List<Transaction> transactionsToShow = new ArrayList<>();

            Log.d(Utils.TAG, "cursor=" + DatabaseUtils.dumpCursorToString(cursor));

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String account = cursor.getString(cursor.getColumnIndex(DB.Account.COLUMN_NAME));
                String id = cursor.getString(cursor.getColumnIndex(DB.Transaction._ID));
                double amount = cursor.getDouble(cursor.getColumnIndex(DB.Transaction.COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_CATEGORY));
                String payee = cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_CATEGORY));
                Date date = Utils
                        .fromISO8601UTC(cursor.getString(cursor.getColumnIndex(DB.Transaction.COLUMN_DATE)));
                Transaction t = new Transaction(id, amount, account, date, category, payee);
                Log.d(Utils.TAG, "Adding transaction " + t.toString());
                transactionsToShow.add(t);

            }
            return transactionsToShow;
        }
    }
}
