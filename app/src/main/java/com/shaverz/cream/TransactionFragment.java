package com.shaverz.cream;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private View mView;
    private TransactionRecyclerViewAdapter adapter;
    private RecyclerView transactionRecyclerView;
    private Spinner accountSpinner;
    private ArrayAdapter<Account> accountArrayAdapter; // holds account objects so can get id easily
    private Spinner periodSpinner;
    private ArrayAdapter<String> periodArrayAdapter;
    private TextView periodOpeningView;
    private TextView periodClosingView;

    private static final int LIST_LOADER = 0;
    private boolean reverseOrder = false;

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

        // Fragment has own menu for customization
        setHasOptionsMenu(true);

        accountSpinner = (Spinner) mView.findViewById(R.id.spinner_account);
        periodSpinner = (Spinner) mView.findViewById(R.id.spinner_period);
        transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.transaction_list);
        periodOpeningView = (TextView) mView.findViewById(R.id.periodOpeningAmountView);
        periodClosingView = (TextView) mView.findViewById(R.id.periodClosingAmountView);

        transactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));

        // make a copy of accounts list to show
        List<Account> accountList = new ArrayList<>(MainActivity.CURRENT_USER.getAccountList());
        accountList.add(0, new Account("-1", "All")); // Fake account for "All" setting

        accountArrayAdapter = new ArrayAdapter<Account>(getContext(),
                android.R.layout.simple_spinner_item, accountList);

        accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountArrayAdapter);

        periodArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                Period.strings);

        periodArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(periodArrayAdapter);

        accountSpinner.setOnItemSelectedListener(new optionsChangeListener());
        periodSpinner.setOnItemSelectedListener(new optionsChangeListener());

        getLoaderManager().initLoader(LIST_LOADER, null, this).forceLoad();

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
            refreshSelectedTransactions(); // just change the items, don't restart loader
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_transactions));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transactions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_reorder:
                reverseOrder = ! reverseOrder;
                refreshSelectedTransactions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new Utils.UserLoader(this.getContext());
    }

    private void refreshSelectedTransactions() {
        // trim transactions to show according to account and period settings
        List<Transaction> transactionList = MainActivity.CURRENT_USER.getTransactions(); // all by default

        // get spinner values
        Account a = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());
        String period = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());

        // if specific account selected, use that accounts list. (Fake "All" account has negative id)
        if (Integer.parseInt(a.getId()) > 0) {
            transactionList =  MainActivity.CURRENT_USER.getAccount(a.getId()).getTransactionList();
        }

        Period.DateRange dateRange = Period.getDateRange(period);

        // only show transactions within date range -> makes a copy so user models aren't overwritten
        final ArrayList<Transaction> transactionsToShow = new ArrayList<>();

        double openingPeriodBalance = 0.00;
        double periodBalance = 0.00;

        for (Transaction t : transactionList){
            if (t.getDate().after(dateRange.startDate) && t.getDate().before(dateRange.endDate)){
                transactionsToShow.add(t);
                periodBalance += t.getAmount();
            } else if (t.getDate().before(dateRange.startDate)) {
                openingPeriodBalance += t.getAmount(); // transaction occurred before period.
            }
        }

        double closingPeriodBalance = openingPeriodBalance + periodBalance;

        // Format currency according to locale from main activity
        Utils.setCurrencyTextView(getContext(), periodOpeningView, openingPeriodBalance);
        Utils.setCurrencyTextView(getContext(), periodClosingView, closingPeriodBalance);

        // Sort transactions
        Comparator<Transaction> cmp = null;

        if (reverseOrder) cmp = Collections.reverseOrder();

        Collections.sort(transactionsToShow, cmp);

        // create adapter and set view to use
        adapter = new TransactionRecyclerViewAdapter(transactionsToShow);
        transactionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User user) {
        // refresh current user
        MainActivity.CURRENT_USER = user;

        refreshSelectedTransactions();
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        transactionRecyclerView.setAdapter(null);
    }

}
