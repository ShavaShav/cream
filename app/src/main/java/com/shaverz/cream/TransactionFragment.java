package com.shaverz.cream;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.views.TransactionAdapter;

public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private View mView;
    private TransactionAdapter adapter;
    private ListView transactionsView;

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

        // Setting to null adapter until cursor loaded
        transactionsView = (ListView) mView.findViewById(R.id.transaction_list);
        TransactionAdapter adapter = new TransactionAdapter(getContext(), null);
        transactionsView.setAdapter(adapter);

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

        // Reload transactions
        getLoaderManager().restartLoader(LIST_LOADER, null, this);

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_transactions));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri userTransactionsUri = DB.Transaction.buildUserTransactionUri(Long.parseLong(Utils.getCurrentUserID(getContext())));
        CursorLoader cursorLoader = new CursorLoader(getContext(), userTransactionsUri, null,
                null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(Utils.TAG, DatabaseUtils.dumpCursorToString(cursor));
        adapter = new TransactionAdapter(getContext(), cursor);

        transactionsView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        transactionsView.setAdapter(null);
    }

}
