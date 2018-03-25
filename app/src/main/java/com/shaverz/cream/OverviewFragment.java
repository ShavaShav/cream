package com.shaverz.cream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class OverviewFragment extends Fragment {

    private View mView;
    private CardView accountsCardView;
    private CardView transactionsCardView;
    private CardView incomeVsExpenseCardView;

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
                Intent i = new Intent(getContext(),OverviewCustomizationActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                .setActionBarTitle(getString(R.string.titlebar_overview));
    }

}
