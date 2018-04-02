package com.shaverz.cream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.shaverz.cream.models.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ReportViewFragment extends Fragment {


    public static final int EXPENSE_BY_CATEGORY = 1;

    private int reportType;

    public ReportViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reportType = getArguments().getInt("report_type");
        } else {
            reportType = EXPENSE_BY_CATEGORY; // default
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_report_view, container, false);

        Map<String, Double> expMap = new HashMap<String, Double>();

        // create graph entries
        for (Transaction tx : MainActivity.CURRENT_USER.getTransactions()) {
            double amount = tx.getAmount();
            // for each transaction
            if (amount < 0.00) {
                amount = -amount;
                // is expense, add to category count
                String cat = tx.getCategory();
                if (!expMap.containsKey(cat)) {
                    expMap.put(cat, amount);
                } else {
                    expMap.put(cat, expMap.get(cat) + amount);
                }

            }
        }

        PieChart chart = view.findViewById(R.id.pie_chart);
        List<PieEntry> entries = new ArrayList<>();

        Iterator it = expMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = (Map.Entry)it.next();
            entries.add(new PieEntry(pair.getValue().floatValue(), pair.getKey()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        PieDataSet set = new PieDataSet(entries, "Expenses by Category");
        // 6 categories, 6 colors
        set.setColors(new int[] { R.color.chart_blue,
                R.color.chart_green,
                R.color.chart_orange,
                R.color.chart_red,
                R.color.chart_yellow,
                R.color.chart_black}, getContext());


        PieData data = new PieData(set);
        chart.setData(data);

        chart.invalidate(); // refresh

        return view;
    }
}
