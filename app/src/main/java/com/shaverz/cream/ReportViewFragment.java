package com.shaverz.cream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.shaverz.cream.models.Account;
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

    private Spinner accountSpinner;
    private ArrayAdapter<Account> accountArrayAdapter; // holds account objects so can get id easily
    private Spinner periodSpinner;
    private ArrayAdapter<String> periodArrayAdapter;
    private RelativeLayout graphFrame;
    private RelativeLayout.LayoutParams chartParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
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
        // Inflate the layout for this fragment and get views
        View view = inflater.inflate(R.layout.fragment_report_view, container, false);
        accountSpinner = (Spinner) view.findViewById(R.id.spinner_account);
        periodSpinner = (Spinner) view.findViewById(R.id.spinner_period);

        // make a copy of accounts list to show
        List<Account> accountList = new ArrayList<>(MainActivity.CURRENT_USER.getAccountList());
        accountList.add(0, new Account("-1", "All")); // Fake account for "All" setting

        accountArrayAdapter = new ArrayAdapter<Account>(getContext(),
                android.R.layout.simple_spinner_item, accountList);

        accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountArrayAdapter);

        periodArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                Period.strings);

        periodArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(periodArrayAdapter);

        accountSpinner.setOnItemSelectedListener(new ReportViewFragment.optionsChangeListener());
        periodSpinner.setOnItemSelectedListener(new ReportViewFragment.optionsChangeListener());

        // generate chart for report
        chart = generateChart(reportType);

        // Add chart to graph_frame, fill it
        graphFrame = view.findViewById(R.id.graph_frame);
        graphFrame.addView(chart, chartParams);

        chart.invalidate(); // refresh

        return view;
    }

    private class optionsChangeListener implements AdapterView.OnItemSelectedListener {
        // will refresh chart whenever options change
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            graphFrame.removeView(chart);
            chart = generateChart(reportType);
            graphFrame.addView(chart, chartParams);
            chart.invalidate();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public Chart generateChart(int reportType) {
        // trim transactions to show according to account and period settings
        List<Transaction> transactionList = MainActivity.CURRENT_USER.getTransactions(); // all by default

        // get spinner values
        Account a = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());
        String period = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());

        // if specific account selected, use that accounts list. (Fake "All" account has negative id)
        if (Integer.parseInt(a.getId()) > 0) {
            transactionList = MainActivity.CURRENT_USER.getAccount(a.getId()).getTransactionList();
        }

        Period.DateRange dateRange = Period.getDateRange(period);

        // only show data for transactions within date range -> makes a copy so user models aren't overwritten
        final ArrayList<Transaction> transactionsToChart = new ArrayList<>();

        for (Transaction t : transactionList){
            if (t.getDate().after(dateRange.startDate) && t.getDate().before(dateRange.endDate)) {
                transactionsToChart.add(t);
            }
        }

        switch (reportType) {
            case EXPENSE_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, false);
            case DAILY_EXPENSES:
                return new LineChart(getContext()); // empty chart
            case MONTHLY_EXPENSES:
                return new LineChart(getContext()); // empty chart
            case INCOME_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, true);
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

    private PieChart generateByCategoryChart(List<Transaction> transactions, boolean income) {
        Map<String, Double> catAmtMap = new HashMap<String, Double>();

        // Map income or expenses by category
        for (Transaction tx : transactions) {
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
