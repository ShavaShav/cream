package com.shaverz.cream.views;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shaverz.cream.R;
import com.shaverz.cream.data.DB;

public class TransactionAdapter extends BaseAdapter {
    Cursor cursor;
    Context context;
    LayoutInflater inflater;

    public TransactionAdapter(Context context, Cursor cursor) {
        this.cursor = cursor;
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        cursor.moveToPosition(i);

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_transaction_row, viewGroup,
                    false);
            holder = new Holder(view);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.mIdView.setText(cursor.getString(cursor.getColumnIndex(DB.Transaction._ID)));
        holder.mContentView.setText(cursor.getString(cursor.getColumnIndex(DB.Account.COLUMN_NAME)));

        return view;
    }

    private class Holder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;

        public Holder(View view) {
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

    }
}
