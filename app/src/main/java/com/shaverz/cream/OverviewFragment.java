package com.shaverz.cream;


import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.Chart;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.utils.ChartGenerator;
import com.shaverz.cream.views.AccountRecyclerViewAdapter;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverviewFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User>{

    private View mView;
    private LinearLayout accountsCardView;
    private LinearLayout transactionsCardView;
    private LinearLayout incomeVsExpenseCardView;
    private LinearLayout expenseByCategoryCardView;

    private TransactionRecyclerViewAdapter recentTransactionsAdapter;
    private RecyclerView recentTransactionRecyclerView;
    private AccountRecyclerViewAdapter myAccountsAdapter;
    private RecyclerView myAccountsRecyclerView;
    private LinearLayout incomeVsExpensesGraphFrame;
    private LinearLayout expensesByCategoryGraphFrame;
    private LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    private ChartGenerator chartGen;
    private static final int USER_LOADER = 0;

    private boolean isAccountsCompact = true;

    public OverviewFragment() {

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
        incomeVsExpensesGraphFrame = mView.findViewById(R.id.incomevsexpense_chart_frame);
        expenseByCategoryCardView = mView.findViewById(R.id.card_view_expense_by_category);
        expensesByCategoryGraphFrame = mView.findViewById(R.id.expense_by_category_chart_frame);
        recentTransactionRecyclerView = (RecyclerView) mView.findViewById(R.id.recent_transactions_list);
        myAccountsRecyclerView = (RecyclerView) mView.findViewById(R.id.my_accounts_list);

        recentTransactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));
        myAccountsRecyclerView.setAdapter(new AccountRecyclerViewAdapter(new ArrayList<Account>(), isAccountsCompact));

        chartGen = new ChartGenerator(getContext());

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
                            // no args for regular add
                            AddEditAccountFragment addEditFrag = new AddEditAccountFragment();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, addEditFrag, "addEditFrag")
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.action_switch_mode:
                            isAccountsCompact = ! isAccountsCompact;
                            setupMyAccountsMode(isAccountsCompact, menuItem);
                            refreshMyAccounts();
                            break;
                    }
                    return true;
                }
            });
            MenuItem switchModeMenuItem = accountsToolbar.getMenu().findItem(R.id.action_switch_mode);
            setupMyAccountsMode(isAccountsCompact, switchModeMenuItem);
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

        // Attach settings menu to income vs expenses card
        Toolbar expenseByCategoryBar = (Toolbar) mView.findViewById(R.id.toolbar_expense_by_category);
        if (expenseByCategoryBar != null) {
            expenseByCategoryBar.inflateMenu(R.menu.menu_overview_expense_by_category);
            expenseByCategoryBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_hide:
                            expenseByCategoryCardView.setVisibility(View.GONE);
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

    private void setupMyAccountsMode (boolean isCompact, MenuItem switchModeMenuItem) {
        // Change orientation of recycler view
        LinearLayoutManager llm;
        if (isCompact) {
            switchModeMenuItem.setTitle(R.string.menu_switch_list_mode);
            llm = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
        } else {
            switchModeMenuItem.setTitle(R.string.menu_switch_compact_mode);
            llm = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.VERTICAL, false);
        }

        myAccountsRecyclerView.setLayoutManager(llm);
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
                Intent i = new Intent(getContext(),OverviewCustomizationActivity.class);
                startActivity(i);
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
        return new Utils.UserLoader(this.getContext());
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        // refresh current user
        MainActivity.CURRENT_USER = data;

        refreshRecentTransactions();
        refreshMyAccounts();
        refreshIncomeVsExpenses();
        refreshExpensesByCategory();
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        recentTransactionRecyclerView.setAdapter(null);
        myAccountsRecyclerView.setAdapter(null);
        incomeVsExpensesGraphFrame.removeAllViews();
    }

    private void refreshRecentTransactions() {
        List<Transaction> transactions = MainActivity.CURRENT_USER.getTransactions();
        Collections.sort(transactions); // sort by latest

        int numToDisplay = 3;
        if (transactions.size() < numToDisplay) {
            numToDisplay = transactions.size();
        }

        List<Transaction> recentTransactions = transactions.subList(0, numToDisplay); // limit to 3

        // create recentTransactionsAdapter and set view to use
        recentTransactionsAdapter = new TransactionRecyclerViewAdapter(recentTransactions);
        recentTransactionRecyclerView.setAdapter(recentTransactionsAdapter);
    }

    private void refreshIncomeVsExpenses() {
        //TODO: filter by daterange
        Chart chart = chartGen.generateIncomeVsExpensesChart(MainActivity.CURRENT_USER.getTransactions());

        incomeVsExpensesGraphFrame.addView(chart, chartParams);
    }

    private void refreshExpensesByCategory() {
        //TODO: filter by daterange
        Chart chart = chartGen.generateByCategoryChart(MainActivity.CURRENT_USER.getTransactions(), false);

        expensesByCategoryGraphFrame.addView(chart, chartParams);
    }

    private void refreshMyAccounts() {
        // create recentTransactionsAdapter and set view to use
        myAccountsAdapter =
                new AccountRecyclerViewAdapter(MainActivity.CURRENT_USER.getAccountList(),
                isAccountsCompact);
        myAccountsRecyclerView.setAdapter(myAccountsAdapter);
    }
}
