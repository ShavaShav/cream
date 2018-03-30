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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
    private Spinner accountSpinner;
    private ArrayAdapter<Account> accountArrayAdapter; // holds account objects so can get id easily
    private Spinner periodSpinner;
    private ArrayAdapter<String> periodArrayAdapter;

    private Calendar startDate;
    private Calendar endDate;

    private static final int LIST_LOADER = 0;

    public static final String TRANSACTION_OBJECT = "transaction";

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

        accountSpinner = (Spinner) mView.findViewById(R.id.spinner_account);
        periodSpinner = (Spinner) mView.findViewById(R.id.spinner_period);
        transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);

        transactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));

        List<Account> accountList = new ArrayList<>(MainActivity.CURRENT_USER.getAccountList());
        accountList.add(0, new Account("-1", "All"));

        accountArrayAdapter = new ArrayAdapter<Account>(getContext(),
                android.R.layout.simple_spinner_item, accountList);

        accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountArrayAdapter);

        periodArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.period_array));

        periodArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(periodArrayAdapter);

        accountSpinner.setOnItemSelectedListener(new optionsChangeListener());
        periodSpinner.setOnItemSelectedListener(new optionsChangeListener());

        getLoaderManager().initLoader(LIST_LOADER, null, this);

        FloatingActionButton addTransactionButton = mView.findViewById(R.id.fab);
        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // no args for regular add
                AddEditTransactionFragment addEditFrag = new AddEditTransactionFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addEditFrag, Utils.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return mView;
    }

    private class optionsChangeListener implements AdapterView.OnItemSelectedListener {
        // will refetch transactions whenever options change
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            getLoaderManager().restartLoader(LIST_LOADER, null, TransactionFragment.this).forceLoad();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
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
    public void onLoadFinished(Loader<User> loader, User user) {
        Log.d(Utils.TAG, "done!");

        // refresh current user
        MainActivity.CURRENT_USER = user;

        // trim transactions to show according to account and period settings
        List<Transaction> transactionList = user.getTransactions(); // all by default

        // get spinner values
        Account a = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());
        String period = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());

        // if specific account selected, use that accounts list
        if (Integer.parseInt(a.getId()) > 0) {
            transactionList = user.getAccount(a.getId()).getTransactionList();
        }

        startDate = Calendar.getInstance(); // now
        endDate = Calendar.getInstance(); // now

        switch (period) {
            case "Today":
                startDate.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "Yesterday":
                startDate.add(Calendar.DAY_OF_YEAR, -2);
                endDate.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "Last 7 days":
                startDate.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "Last 30 days":
                startDate.add(Calendar.DAY_OF_YEAR, -30);
                break;
            default: // show all
                startDate.setTimeInMillis(Long.MIN_VALUE); // earliest possible time
                break;
        }

        // only show transactions within date range
        ArrayList<Transaction> transactionsToShow = new ArrayList<>();
        for (Transaction t : transactionList){
            if (t.getDate().after(startDate) && t.getDate().before(endDate))
                transactionsToShow.add(t);
        }
        Collections.sort(transactionsToShow);

        // create adapter and set view to use
        adapter = new TransactionRecyclerViewAdapter(transactionsToShow);
        transactionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        transactionRecyclerView.setAdapter(null);
    }

}
