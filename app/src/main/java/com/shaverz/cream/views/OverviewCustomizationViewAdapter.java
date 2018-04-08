package com.shaverz.cream.views;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;
import com.shaverz.cream.ReportViewFragment;
import com.shaverz.cream.Utils;
import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.OverviewCustomization;

import java.util.List;

/**
 * Created by z on 07/04/2018.
 */

public class OverviewCustomizationViewAdapter extends ArrayAdapter<String> {

    private String[] stringList;
    private List<Boolean> isVisibleList;
    private List<Integer> orderList;

    public OverviewCustomizationViewAdapter(@NonNull Context context) {
        super(context, 0);

        stringList = context.getResources().getStringArray(R.array.overview_items);
        orderList = MainActivity.CURRENT_USER.getOverviewCustomization().getOverviewOrderList();
        isVisibleList = MainActivity.CURRENT_USER.getOverviewCustomization().getOverviewVisibilityList();

        super.addAll(stringList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final int overviewItem = orderList.get(position);
        String overviewItemString = getItem(overviewItem);
        Boolean isVisible = isVisibleList.get(overviewItem);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.overview_customization_row, parent, false);
        }
        // Lookup view for data population
        TextView itemTextView = (TextView) convertView.findViewById(R.id.row_text_view);
        final Switch toggle = (Switch) convertView.findViewById(R.id.switch_id);

        // Populate the data into the template view using the data object
        itemTextView.setText(overviewItemString);
        toggle.setChecked(isVisible);

        // attach switch listener
        toggle.post(new Runnable() {
            public void run() {
                toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isVisible) {
                        ContentValues content = new ContentValues();
                        isVisibleList.set(overviewItem, isVisible);

                        content.put(DB.User.COLUMN_OVERVIEW_VISIBILITY,
                                OverviewCustomization.getVisibleDBString(isVisibleList));
                        getContext().getContentResolver().update(
                                DB.User.buildUserUri(Long.parseLong(MainActivity.CURRENT_USER.getId())),
                                content, null, null);
                    }
                });
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }
}
