package com.shaverz.cream.views;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;
import com.shaverz.cream.utils.CommonUtils;
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
    private boolean reordering = false;

    public OverviewCustomizationViewAdapter(@NonNull Context context) {
        super(context, 0);

        stringList = context.getResources().getStringArray(R.array.overview_items);
        orderList = MainActivity.CURRENT_USER.getOverviewCustomization().getOverviewOrderList();
        isVisibleList = MainActivity.CURRENT_USER.getOverviewCustomization().getOverviewVisibilityList();

        super.addAll(stringList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final int overviewItem = orderList.get(position);
        String overviewItemString = getItem(overviewItem);
        Boolean isVisible = isVisibleList.get(overviewItem);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.overview_customization_row, parent, false);
        }
        // Lookup view for data population
        final ImageView upArrowView = (ImageView) convertView.findViewById(R.id.upArrow);
        final ImageView downArrowView = (ImageView) convertView.findViewById(R.id.downArrow);

        final TextView itemTextView = (TextView) convertView.findViewById(R.id.row_text_view);
        final Switch toggle = (Switch) convertView.findViewById(R.id.switch_id);

        // Populate the data into the template view using the data object
        itemTextView.setText(overviewItemString);
        toggle.setChecked(isVisible);

        upArrowView.post(new Runnable() {
            public void run() {
                // update db entries whenever switch toggled
                upArrowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // If reordering, don't allow movement
                        if (!reordering) {
                            reordering = true;

                            if (position > 0) {
                                // swap with item above in orderList
                                int temp = orderList.get(position-1);
                                orderList.set(position - 1, orderList.get(position));
                                orderList.set(position, temp);

                                saveOrderList();
                                notifyDataSetChanged();
                            }

                            reordering = false;
                        }
                    }
                });
            }
        });


        downArrowView.post(new Runnable() {
            public void run() {
                // update db entries whenever switch toggled
                downArrowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // If reordering, don't allow movement
                        if (!reordering) {
                            reordering = true;

                            if (position < stringList.length - 1) {
                                // swap with item below in orderList
                                int temp = orderList.get(position+1);
                                orderList.set(position + 1, orderList.get(position));
                                orderList.set(position, temp);

                                saveOrderList();
                                notifyDataSetChanged();
                            }

                            reordering = false;
                        }
                    }
                });
            }
        });

        // attach switch listener
        toggle.post(new Runnable() {
            public void run() {
                // update db entries whenever switch toggled
                toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isVisible) {
                        if (!reordering) {
                            Log.d(CommonUtils.TAG, "pos " + position + " visible: " + isVisible);
                            isVisibleList.set(orderList.get(position), isVisible);
                            saveVisibilityList();
                        }
                    }
                });
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void saveOrderList() {
        ContentValues values = new ContentValues();
        values.put(DB.User.COLUMN_OVERVIEW_ORDER,
                OverviewCustomization.getOrderDBString(orderList));

        getContext().getContentResolver().update(
                DB.User.buildUserUri(Long.parseLong(MainActivity.CURRENT_USER.getId())),
                values, null, null);
    }

    private void saveVisibilityList() {
        ContentValues content = new ContentValues();

        content.put(DB.User.COLUMN_OVERVIEW_VISIBILITY,
                OverviewCustomization.getVisibleDBString(isVisibleList));
        getContext().getContentResolver().update(
                DB.User.buildUserUri(Long.parseLong(MainActivity.CURRENT_USER.getId())),
                content, null, null);
    }
}
