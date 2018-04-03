package com.shaverz.cream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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

    public static final String ARG_REPORT_TYPE = "report_type";
    public static final int EXPENSE_BY_CATEGORY = 1;
    public static final int DAILY_EXPENSES = 2;
    public static final int MONTHLY_EXPENSES = 3;
    public static final int INCOME_BY_CATEGORY = 4;
    public static final int DAILY_INCOME = 5;
    public static final int MONTHLY_INCOME = 6;
    public static final int DAILY_BALANCE = 7;
    public static final int INCOME_VS_EXPENSE = 8;

//    private Spinner accountSpinner;
//    private ArrayAdapter<Account> accountArrayAdapter; // holds account objects so can get id easily
//    private Spinner periodSpinner;
//    private ArrayAdapter<String> periodArrayAdapter;

    private int reportType;
    private Chart chart;

    private static final int[] GRAPH_COLORS = new int[] {
            R.color.chart_blue,
            R.color.chart_green,
            R.color.chart_orange,
            R.color.chart_red,
            R.color.chart_yellow,
            R.color.chart_black };

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

        // generate chart for report
        chart = generateChart(reportType);

        // Add chart to graph_frame, fill it
        RelativeLayout rl = view.findViewById(R.id.graph_frame);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rl.addView(chart, params);

        chart.invalidate(); // refresh

        return view;
    }

    public Chart generateChart(int reportType) {
        switch (reportType) {
            case EXPENSE_BY_CATEGORY:
                return generateByCategoryChart(false);
            case DAILY_EXPENSES:
                return new LineChart(getContext()); // empty chart
            case MONTHLY_EXPENSES:
                return new LineChart(getContext()); // empty chart
            case INCOME_BY_CATEGORY:
                return generateByCategoryChart(true);
            case DAILY_INCOME:
                return new LineChart(getContext()); // empty chart
            case MONTHLY_INCOME:
                return new LineChart(getContext()); // empty chart
            case DAILY_BALANCE:
                return new LineChart(getContext()); // empty chart
            case INCOME_VS_EXPENSE:
                return new LineChart(getContext()); // empty chart
            default:
                return new LineChart(getContext()); // empty chart
        }

    }

    private PieChart generateByCategoryChart(boolean income) {
        Map<String, Double> catAmtMap = new HashMap<String, Double>();

        // Map income or expenses by category
        for (Transaction tx : MainActivity.CURRENT_USER.getTransactions()) {
            double amount = tx.getAmount();
            boolean addToMap = false;
            // Only count transactions according to flag
            if (amount < 0.00) {
                addToMap = !income; // expense
                amount = -amount; // make positive to show in chart
                // income
            } else if (amount > 0.00) {
                addToMap = income; // income
            }

            if (addToMap) {
                String cat = tx.getCategory();
                if (!catAmtMap.containsKey(cat)) {
                    catAmtMap.put(cat, amount);
                } else {
                    catAmtMap.put(cat, catAmtMap.get(cat) + amount);
                }
            }
        }

        // Convert to chart entries
        List<PieEntry> entries = new ArrayList<>();

        Iterator it = catAmtMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = (Map.Entry) it.next();
            entries.add(new PieEntry(pair.getValue().floatValue(), pair.getKey()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        // Connect chart parts and return
        PieDataSet set = new PieDataSet(entries,
                (income ? "Income" : "Expenses") + " by Category");
        set.setColors(GRAPH_COLORS, getContext());

        PieData data = new PieData(set);
        PieChart chart = new PieChart(getContext());
        chart.setData(data);

        return chart;
    }
}
