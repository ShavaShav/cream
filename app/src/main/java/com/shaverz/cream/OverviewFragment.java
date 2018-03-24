package com.shaverz.cream;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class OverviewFragment extends Fragment {

    private View mView;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_overview, container, false);

        // Attach settings menu to accounts card
        Toolbar accountsToolbar = (Toolbar) mView.findViewById(R.id.toolbar_accounts);
        if (accountsToolbar != null) {
            accountsToolbar.inflateMenu(R.menu.menu_overview_accounts);
            accountsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return true;
                }
            });
        }

        // Attach settings menu to transactions card
        Toolbar transactionsBar = (Toolbar) mView.findViewById(R.id.toolbar_transactions);
        if (transactionsBar != null) {
            transactionsBar.inflateMenu(R.menu.menu_overview_transactions);
            transactionsBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return true;
                }
            });
        }

        // Attach settings menu to transactions card
        Toolbar incomeExpenseBar = (Toolbar) mView.findViewById(R.id.toolbar_income_vs_expense);
        if (incomeExpenseBar != null) {
            incomeExpenseBar.inflateMenu(R.menu.menu_overview_income_vs_expenses);
            incomeExpenseBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        default:
                            return true;
                    }
                }
            });
        }

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
                .setActionBarTitle(getString(R.string.titlebar_overview));
    }

}
