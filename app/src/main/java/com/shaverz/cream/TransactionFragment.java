package com.shaverz.cream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.List;

public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<User> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private User userModel;
    private View mView;

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
        getLoaderManager().restartLoader(LIST_LOADER, null, this);

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_transactions));
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        // create the user loader - fetches all user data so views stay in sync
        Utils.UserLoader loader = new Utils.UserLoader(this.getContext());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User userModel) {
        // store user model
        this.userModel = userModel;

        // get list view
        final RecyclerView transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        // get list of items from loader's result
        List<Transaction> transactionsToShow = userModel.getTransactions(); // all by default
        // create adapter and set view to use
        TransactionRecyclerViewAdapter adapter = new TransactionRecyclerViewAdapter(transactionsToShow);
        transactionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        // destroy adapter
        final RecyclerView transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        transactionRecyclerView.setAdapter(null);
    }
}
