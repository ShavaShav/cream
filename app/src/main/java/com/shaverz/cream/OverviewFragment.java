package com.shaverz.cream;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.models.User;
import com.shaverz.cream.utils.ChartGenerator;
import com.shaverz.cream.utils.CommonUtils;
import com.shaverz.cream.utils.CurrencyAxisValueFormatter;
import com.shaverz.cream.utils.DBUtils;
import com.shaverz.cream.utils.Period;
import com.shaverz.cream.utils.RecentPeriod;
import com.shaverz.cream.views.AccountRecyclerViewAdapter;
import com.shaverz.cream.views.TransactionRecyclerViewAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

public class OverviewFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User>{

    private View mView;
    private LinearLayout accountsCardView;
    private LinearLayout transactionsCardView;
    private LinearLayout incomeVsExpenseCardView;
    private LinearLayout expenseByCategoryCardView;
    private LinearLayout highSpendingAlertsCardView;
    private LinearLayout overviewFrame;

    private List<View> cardViews;
    private TextView highSpendingTextView;
    private TransactionRecyclerViewAdapter recentTransactionsAdapter;
    private RecyclerView recentTransactionRecyclerView;
    private AccountRecyclerViewAdapter myAccountsAdapter;
    private RecyclerView myAccountsRecyclerView;
    private FloatingActionsMenu overviewFAB;
    private TextView totalBalanceTextView;
    private AlertDialog.Builder incomeVsExpensePeriodDialog;
    private TextView incomeVsExpensePeriodTextView;
    private AlertDialog.Builder expenseByCategoryPeriodDialog;
    private TextView expenseByCategoryPeriodTextView;

    private ChartGenerator chartGen;
    private PieChart incomeVsExpenseChart;
    private Period.DateRange incomeVsExpensePeriod;
    private PieChart expenseByCategoryChart;
    private Period.DateRange expenseByCategoryPeriod;

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

        highSpendingAlertsCardView = mView.findViewById(R.id.card_view_high_spending_alert);
        accountsCardView = mView.findViewById(R.id.card_view_my_accounts);
        transactionsCardView = mView.findViewById(R.id.card_view_transactions);
        incomeVsExpenseCardView = mView.findViewById(R.id.card_view_income_vs_expense);
        expenseByCategoryCardView = mView.findViewById(R.id.card_view_expense_by_category);
        overviewFrame = mView.findViewById(R.id.overview_frame);
        highSpendingTextView = mView.findViewById(R.id.textview_high_spending);
        overviewFAB = mView.findViewById(R.id.overviewFab);
        totalBalanceTextView = mView.findViewById(R.id.totalBalanceTextView);
        incomeVsExpensePeriodTextView = mView.findViewById(R.id.incomeVsExpensePeriod);
        expenseByCategoryPeriodTextView = mView.findViewById(R.id.expenseByCategoryPeriod);

        // Add in order according to R.arrays.overview_customization
        cardViews = new ArrayList<>();
        cardViews.add(highSpendingAlertsCardView);
        cardViews.add(accountsCardView);
        cardViews.add(transactionsCardView);
        cardViews.add(incomeVsExpenseCardView);
        cardViews.add(expenseByCategoryCardView);

        recentTransactionRecyclerView = (RecyclerView) mView.findViewById(R.id.recent_transactions_list);
        myAccountsRecyclerView = (RecyclerView) mView.findViewById(R.id.my_accounts_list);

        recentTransactionRecyclerView.setAdapter(new TransactionRecyclerViewAdapter(new ArrayList<Transaction>()));
        myAccountsRecyclerView.setAdapter(new AccountRecyclerViewAdapter(new ArrayList<Account>(), isAccountsCompact));

        chartGen = new ChartGenerator(getContext());

        incomeVsExpenseChart = (PieChart) mView.findViewById(R.id.incomevsexpensePieChart);
        expenseByCategoryChart = (PieChart) mView.findViewById(R.id.expenseByCategoryChart);

        incomeVsExpenseChart.getLegend().setEnabled(false);
        incomeVsExpenseChart.getDescription().setEnabled(false);

        expenseByCategoryChart.getLegend().setEnabled(false);
        expenseByCategoryChart.getDescription().setEnabled(false);

        incomeVsExpensePeriodDialog = new AlertDialog.Builder(getContext());
        incomeVsExpensePeriodDialog.setIcon(R.drawable.ic_time);
        incomeVsExpensePeriodDialog.setTitle(R.string.period_title);

        expenseByCategoryPeriodDialog = new AlertDialog.Builder(getContext());
        expenseByCategoryPeriodDialog.setIcon(R.drawable.ic_time);
        expenseByCategoryPeriodDialog.setTitle(R.string.period_title);

        final Period recentPeriod = new RecentPeriod();
        final ArrayAdapter<String> periodArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_singlechoice,
                recentPeriod.getPeriodStrings());

        incomeVsExpensePeriodDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        incomeVsExpensePeriodDialog.setAdapter(periodArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String periodString = periodArrayAdapter.getItem(which);
                incomeVsExpensePeriodTextView.setText(periodString);
                incomeVsExpensePeriod = recentPeriod.getDateRange(periodString);
                refreshIncomeVsExpenses();
            }
        });

        expenseByCategoryPeriodDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        expenseByCategoryPeriodDialog.setAdapter(periodArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String periodString = periodArrayAdapter.getItem(which);
                expenseByCategoryPeriodTextView.setText(periodString);
                expenseByCategoryPeriod = recentPeriod.getDateRange(periodString);
                refreshExpensesByCategory();
            }
        });

        // Attach settings menu to accounts card
        Toolbar accountsToolbar = (Toolbar) mView.findViewById(R.id.toolbar_accounts);
        if (accountsToolbar != null) {
            accountsToolbar.inflateMenu(R.menu.menu_overview_accounts);
            accountsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_hide:
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_reorder:
                            startOverViewCustomizationActivity();
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
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_reorder:
                            startOverViewCustomizationActivity();
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
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_reorder:
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_settings:
                            incomeVsExpensePeriodDialog.show();
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
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_reorder:
                            startOverViewCustomizationActivity();
                            break;
                        case R.id.action_settings:
                            expenseByCategoryPeriodDialog.show();
                            break;
                    }
                    return true;
                }
            });
        }

        // Remove all views, will add back in according to overview settings
        overviewFrame.removeAllViews();

        final FloatingActionButton addIncomeButton = mView.findViewById(R.id.add_income_button);
        addIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putBoolean(MainActivity.IS_INCOME_BOOLEAN, true);

                AddEditTransactionFragment fragment = new AddEditTransactionFragment();
                fragment.setArguments(arguments);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

                overviewFAB.collapse();
            }
        });

        final FloatingActionButton addExpenseButton = mView.findViewById(R.id.add_expense_button);
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putBoolean(MainActivity.IS_INCOME_BOOLEAN, false);

                AddEditTransactionFragment fragment = new AddEditTransactionFragment();
                fragment.setArguments(arguments);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

                overviewFAB.collapse();
            }
        });

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
                startOverViewCustomizationActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        getLoaderManager().restartLoader(USER_LOADER, null, this).forceLoad();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_overview));
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new DBUtils.UserLoader(this.getContext());
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        // refresh current user
        MainActivity.CURRENT_USER = data;

        List<Boolean> isVisibleList = data.getOverviewCustomization().getOverviewVisibilityList();
        List<Integer> orderList = data.getOverviewCustomization().getOverviewOrderList();

        overviewFrame.removeAllViews();

        // add all views back in order, make some invisible
        for (int index : orderList) {
            overviewFrame.addView(cardViews.get(index));
            if (isVisibleList.get(index))
                cardViews.get(index).setVisibility(View.VISIBLE);
            else
                cardViews.get(index).setVisibility(GONE);

        }

        refreshHighSpendingAlert();
        refreshMyAccounts();
        refreshRecentTransactions();
        refreshIncomeVsExpenses();
        refreshExpensesByCategory();
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        recentTransactionRecyclerView.setAdapter(null);
        myAccountsRecyclerView.setAdapter(null);

    }

    public void startOverViewCustomizationActivity() {
        Intent i = new Intent(getContext(),OverviewCustomizationActivity.class);
        startActivity(i);
    }

    private void refreshHighSpendingAlert() {
        double todaysExpense = MainActivity.CURRENT_USER.getAmountSpentToday();
        if (todaysExpense >= 500.0) {
            String message = "You have spent " + NumberFormat
                    .getCurrencyInstance(MainActivity.CURRENT_USER.getCurrencyLocale())
                    .format(todaysExpense) + " today!";
            highSpendingTextView.setText(message);
            highSpendingTextView.setSelected(true);
        } else {
            highSpendingAlertsCardView.setVisibility(GONE); // hide self, no alert to show
        }

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
        List<Transaction> transactionList = MainActivity.CURRENT_USER.getTransactions();
        List<Transaction> transactionsToChart;
        if (incomeVsExpensePeriod == null) {
            // no period, no further filter
            transactionsToChart = transactionList;
            incomeVsExpensePeriodTextView.setText("All");
        } else {
            // only show data for transactions within date range -> makes a copy so user models aren't overwritten
            transactionsToChart = new ArrayList<>();
            for (Transaction t : transactionList) {
                if (t.getDate().after(incomeVsExpensePeriod.startDate)
                        && t.getDate().before(incomeVsExpensePeriod.endDate)) {
                    transactionsToChart.add(t);
                }
            }
        }

        PieData data = chartGen.generateIncomeVsExpensesData(transactionsToChart);
        incomeVsExpenseChart.setData(data);
        incomeVsExpenseChart.notifyDataSetChanged();
        incomeVsExpenseChart.invalidate();
    }

    private void refreshExpensesByCategory() {
        List<Transaction> transactionList = MainActivity.CURRENT_USER.getTransactions();
        List<Transaction> transactionsToChart;
        if (expenseByCategoryPeriod == null) {
            // no period, no further filter
            transactionsToChart = transactionList;
            expenseByCategoryPeriodTextView.setText("All");
        } else {
            // only show data for transactions within date range -> makes a copy so user models aren't overwritten
            transactionsToChart = new ArrayList<>();
            for (Transaction t : transactionList) {
                if (t.getDate().after(expenseByCategoryPeriod.startDate)
                        && t.getDate().before(expenseByCategoryPeriod.endDate)) {
                    transactionsToChart.add(t);
                }
            }
        }

        PieData data = chartGen.generateByCategoryData(transactionsToChart, false);
        expenseByCategoryChart.setData(data);
        expenseByCategoryChart.notifyDataSetChanged();
        expenseByCategoryChart.invalidate();
    }

    private void refreshMyAccounts() {
        CommonUtils.setCurrencyTextView(getContext(),
                totalBalanceTextView, MainActivity.CURRENT_USER.getBalance());

        // create recentTransactionsAdapter and set view to use
        myAccountsAdapter =
                new AccountRecyclerViewAdapter(MainActivity.CURRENT_USER.getAccountList(),
                isAccountsCompact);
        myAccountsRecyclerView.setAdapter(myAccountsAdapter);
    }
}
