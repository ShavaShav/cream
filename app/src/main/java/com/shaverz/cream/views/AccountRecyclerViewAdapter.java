package com.shaverz.cream.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shaverz.cream.AddEditAccountFragment;
import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;
import com.shaverz.cream.Utils;
import com.shaverz.cream.models.Account;

import java.util.List;

public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private final List<Account> accounts;
    private final boolean isCompact;
    private Context context;


    public AccountRecyclerViewAdapter(List<Account> items, boolean isCompact) {
        this.isCompact = isCompact;
        accounts = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;
        if (isCompact) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.fragment_account_column, parent, false);
        } else {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.fragment_account_row, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Account a = accounts.get(position);

        holder.nameView.setText(a.getName());
        Utils.setCurrencyTextView(context, holder.balanceView, a.getBalance());
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameView;
        public final TextView balanceView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameView = (TextView) view.findViewById(R.id.accountTextView);
            balanceView = (TextView) view.findViewById(R.id.balanceTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: Replace with menu for edit/delete etc
                    Account account = accounts.get(getLayoutPosition());

                    AddEditAccountFragment addEditFrag = new AddEditAccountFragment();
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(MainActivity.ACCOUNT_OBJECT, account);
                    addEditFrag.setArguments(arguments);

                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addEditFrag, "addEditAccount")
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }
}
