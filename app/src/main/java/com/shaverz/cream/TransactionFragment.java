package com.shaverz.cream;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

public class TransactionFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private User userModel;
    private View mView;
    private TransactionRecyclerViewAdapter adapter;
    private RecyclerView transactionRecyclerView;
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
        transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        transactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));

        getLoaderManager().initLoader(LIST_LOADER, null, this).forceLoad();

        FloatingActionButton fab = mView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(DB.Transaction.COLUMN_ACCOUNT_ID, 2);
                values.put(DB.Transaction.COLUMN_AMOUNT, 100.0);
                values.put(DB.Transaction.COLUMN_CATEGORY, "Other");
                values.put(DB.Transaction.COLUMN_DATE, Utils.toISO8601UTC(new Date()));
                values.put(DB.Transaction.COLUMN_PAYEE, "Random");

                Uri uri = getActivity()
                        .getContentResolver()
                        .insert(DB.Transaction.CONTENT_URI, values);

                getLoaderManager()
                        .restartLoader(LIST_LOADER, null, TransactionFragment.this)
                        .forceLoad();
            }
        });

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

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_transactions));
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new Utils.UserLoader(this.getContext());
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        Snackbar.make(mView, "Balance: " + data.getBalance(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        // get list of items from loader's result
        List<Transaction> transactionsToShow = data.getTransactions(); // all by default
        // create adapter and set view to use
        adapter = new TransactionRecyclerViewAdapter(transactionsToShow);
        transactionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        transactionRecyclerView.setAdapter(null);
    }
}
