package com.shaverz.cream;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OverviewFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User>{

    private View mView;
    private LinearLayout accountsCardView;
    private LinearLayout transactionsCardView;
    private LinearLayout incomeVsExpenseCardView;
    private TransactionRecyclerViewAdapter adapter;
    private RecyclerView transactionRecyclerView;

    private static final int USER_LOADER = 0;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_overview, container, false);

        // Fragment has own menu for customization
        setHasOptionsMenu(true);

        accountsCardView = mView.findViewById(R.id.card_view_my_accounts);
        transactionsCardView = mView.findViewById(R.id.card_view_transactions);
        incomeVsExpenseCardView = mView.findViewById(R.id.card_view_income_vs_expense);
        transactionRecyclerView = (RecyclerView) mView.findViewById(R.id.recent_transactions_list);

        transactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));

        // Attach settings menu to accounts card
        Toolbar accountsToolbar = (Toolbar) mView.findViewById(R.id.toolbar_accounts);
        if (accountsToolbar != null) {
            accountsToolbar.inflateMenu(R.menu.menu_overview_accounts);
            accountsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_hide:
                            accountsCardView.setVisibility(View.GONE);
                            break;
                        case R.id.action_reorder:
                            break;
                        case R.id.action_reorder_account:
                            break;
                        case R.id.action_new_account:
                            break;
                        case R.id.action_switch_mode:
                            break;
                    }
                    return true;
                }
            });
        }

        // Attach settings menu to transactions card
        final Toolbar transactionsBar = (Toolbar) mView.findViewById(R.id.toolbar_transactions);
        if (transactionsBar != null) {
            transactionsBar.inflateMenu(R.menu.menu_overview_transactions);
            transactionsBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_hide:
                            transactionsCardView.setVisibility(View.GONE);
                            break;
                        case R.id.action_reorder:
                            break;
                        case R.id.action_settings:
                            break;
                    }
                    return true;
                }
            });
        }

        // Attach settings menu to income vs expenses card
        Toolbar incomeExpenseBar = (Toolbar) mView.findViewById(R.id.toolbar_income_vs_expense);
        if (incomeExpenseBar != null) {
            incomeExpenseBar.inflateMenu(R.menu.menu_overview_income_vs_expenses);
            incomeExpenseBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_hide:
                            incomeVsExpenseCardView.setVisibility(View.GONE);
                            break;
                        case R.id.action_reorder:
                            break;
                        case R.id.action_settings:
                            break;
                    }
                    return true;
                }
            });
        }

        getLoaderManager().initLoader(USER_LOADER, null, this).forceLoad();

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_overview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_customize:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_overview));
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        Log.d(Utils.TAG,"Loading!");
        return new Utils.UserLoader(this.getContext());
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        Log.d(Utils.TAG,"Done!");

        // refresh current user
        MainActivity.CURRENT_USER = data;

        refreshSelectedTransactions();
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        transactionRecyclerView.setAdapter(null);
    }

    private void refreshSelectedTransactions() {

        List<Transaction> transactions = MainActivity.CURRENT_USER.getTransactions();
        Collections.sort(transactions); // sort by latest
        List<Transaction> recentTransactions = transactions.subList(0, 3); // limit to 3

        // create adapter and set view to use
        adapter = new TransactionRecyclerViewAdapter(recentTransactions);
        transactionRecyclerView.setAdapter(adapter);
    }
}
