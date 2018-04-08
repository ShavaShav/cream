package com.shaverz.cream.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shaverz.cream.AddEditTransactionFragment;
import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;
import com.shaverz.cream.Utils;
import com.shaverz.cream.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder> {

    private final List<Transaction> transactions;
    private Context context;

    public TransactionRecyclerViewAdapter(List<Transaction> items) {
        transactions = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_transaction_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.accountView.setText(t.getAccount());
        holder.dateView.setText(
                new SimpleDateFormat("MMM dd").format(t.getDate().getTime()));
        holder.payerView.setText(t.getPayee());
        holder.categoryView.setText(t.getCategory());

        // Format currency according to locale from main activity
        Utils.setCurrencyTextView(context, holder.amountView, t.getAmount());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView accountView;
        public final TextView dateView;
        public final TextView payerView;
        public final TextView categoryView;
        public final TextView amountView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            accountView = (TextView) view.findViewById(R.id.accountTextView);
            dateView = (TextView) view.findViewById(R.id.dateTextView);
            payerView = (TextView) view.findViewById(R.id.payeePayerTextView);
            categoryView = (TextView) view.findViewById(R.id.categoryTextView);
            amountView = (TextView) view.findViewById(R.id.amountTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Transaction transaction = transactions.get(getLayoutPosition());

                    AddEditTransactionFragment addEditFrag = new AddEditTransactionFragment();
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(MainActivity.TRANSACTION_OBJECT, transaction);
                    addEditFrag.setArguments(arguments);

                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addEditFrag, Utils.TAG)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }
}
