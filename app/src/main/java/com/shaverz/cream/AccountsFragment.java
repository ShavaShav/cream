package com.shaverz.cream;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.User;
import com.shaverz.cream.utils.CommonUtils;
import com.shaverz.cream.views.AccountRecyclerViewAdapter;

import java.util.ArrayList;

public class AccountsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<User>{

    private View mView;
    private AccountRecyclerViewAdapter myAccountsAdapter;
    private RecyclerView myAccountsRecyclerView;
    private TextView totalBalanceTextView;

    private static final int USER_LOADER = 0;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_accounts, container, false);

        totalBalanceTextView = mView.findViewById(R.id.totalBalanceTextView);
        myAccountsRecyclerView = (RecyclerView) mView.findViewById(R.id.accounts_list);
        myAccountsRecyclerView.setAdapter(new AccountRecyclerViewAdapter(new ArrayList<Account>(), false));

        FloatingActionButton addAccountButton = mView.findViewById(R.id.fab);
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // no args for regular add
                AddEditAccountFragment addEditFrag = new AddEditAccountFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addEditFrag, "addEditFrag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        getLoaderManager().initLoader(USER_LOADER, null, this).forceLoad();

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
                .setActionBarTitle(getString(R.string.titlebar_accounts));
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new CommonUtils.UserLoader(this.getContext());
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        // refresh current user and accounts
        MainActivity.CURRENT_USER = data;

        CommonUtils.setCurrencyTextView(getContext(), totalBalanceTextView, data.getBalance());

        myAccountsAdapter = new AccountRecyclerViewAdapter(MainActivity.CURRENT_USER.getAccountList(), false);
        myAccountsRecyclerView.setAdapter(myAccountsAdapter);
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        myAccountsRecyclerView.setAdapter(null);
    }

}
