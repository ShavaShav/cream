package com.shaverz.cream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


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
    private boolean periodExists;
    private Chart chart;


    private static int[] graphColours = new int[] {
            R.color.chart_blue,
            R.color.chart_green,
            R.color.chart_orange,
            R.color.chart_red,
            R.color.chart_black };

    private static void shuffleColours() {
        Random rnd = new Random();
        for (int i = graphColours.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            //  Swap
            int temp = graphColours[index];
            graphColours[index] = graphColours[i];
            graphColours[i] = temp;
        }
    }

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
        shuffleColours(); // shuffle for initial view
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and get views
        View view = inflater.inflate(R.layout.fragment_report_view, container, false);
        accountSpinner = (Spinner) view.findViewById(R.id.spinner_account);

        // make a copy of accounts list to show
        List<Account> accountList = new ArrayList<>(MainActivity.CURRENT_USER.getAccountList());
        accountList.add(0, new Account("-1", "All")); // Fake account for "All" setting


        accountArrayAdapter = new ArrayAdapter<Account>(getContext(),
                android.R.layout.simple_spinner_item, accountList);

        accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountArrayAdapter);
        // Stop spinner from calling it's listener on init
        accountSpinner.post(new Runnable() {
            public void run() {
                accountSpinner.setOnItemSelectedListener(new ReportViewFragment.optionsChangeListener());
            }
        });

        periodExists = false; // assume no period option

        if (reportType != DAILY_EXPENSES
                && reportType != DAILY_INCOME
                && reportType != MONTHLY_EXPENSES
                && reportType != MONTHLY_INCOME) {

            periodExists = true;

            periodSpinner = (Spinner) view.findViewById(R.id.spinner_period);
            periodArrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item,
                    Period.strings);

            periodArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            periodSpinner.setAdapter(periodArrayAdapter);
            // Stop spinner from calling it's listener on init
            periodSpinner.post(new Runnable() {
                public void run() {
                    periodSpinner.setOnItemSelectedListener(new ReportViewFragment.optionsChangeListener());
                }
            });
        }

        if (!periodExists) {
            // disappear period tech and spinner
            view.findViewById(R.id.spinner_period).setVisibility(View.GONE);
            view.findViewById(R.id.period_textView).setVisibility(View.GONE);
        }

        // generate chart for report
        chart = generateChart(reportType);

        // Add chart to graph_frame, fill it
        graphFrame = view.findViewById(R.id.graph_frame);
        graphFrame.addView(chart, chartParams);

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

        // get account from spinner
        Account a = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());

        // if specific account selected, use that accounts list. (Fake "All" account has negative id)
        if (Integer.parseInt(a.getId()) > 0) {
            transactionList = MainActivity.CURRENT_USER.getAccount(a.getId()).getTransactionList();
        }

        List<Transaction> transactionsToChart;

        if ( ! periodExists) {
            // no period, no further filter
            transactionsToChart = transactionList;
        } else {
            // copy transactions within period
            String period = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());
            Period.DateRange dateRange = Period.getDateRange(period);

            // only show data for transactions within date range -> makes a copy so user models aren't overwritten
            transactionsToChart = new ArrayList<>();
            for (Transaction t : transactionList){
                if (t.getDate().after(dateRange.startDate) && t.getDate().before(dateRange.endDate)) {
                    transactionsToChart.add(t);
                }
            }
        }

        switch (reportType) {
            case EXPENSE_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, false);
            case DAILY_EXPENSES:
                return generateDailyTxChart(transactionsToChart, false); // empty chart
            case MONTHLY_EXPENSES:
                return generateMonthlyTxChart();
            case INCOME_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, true);
            case DAILY_INCOME:
                return generateDailyTxChart(transactionsToChart, true);
            case MONTHLY_INCOME:
                return generateMonthlyTxChart();
            case DAILY_BALANCE:
                return new LineChart(getContext()); // empty chart
            case INCOME_VS_EXPENSE:
                return new LineChart(getContext()); // empty chart
            default:
                return new LineChart(getContext()); // empty chart
        }

    }

    private String formatDate (Calendar c) {
        return new SimpleDateFormat("MMM dd").format(c.getTime());
    }

    private BarChart generateMonthlyTxChart(){
        return null;
    }

    private BarChart generateDailyTxChart(final List<Transaction> transactions, boolean income) {
        Map<String, Double> dayTxMap = new HashMap<String, Double>();

        Log.d(Utils.TAG, "transactions: " + transactions.size());

        // Store string names of dates through the week
        List<String> daysOfWeek = new ArrayList<>();
        Calendar beginWeek = Calendar.getInstance();
        for (int i = 0; i < 7; i ++) {
            daysOfWeek.add(0, formatDate(beginWeek));
            beginWeek.add(Calendar.DAY_OF_YEAR, -1);
        }

        // set to beginning of day to capture all tx for first day
        beginWeek.set(Calendar.HOUR_OF_DAY, 0);
        beginWeek.set(Calendar.MINUTE, 0);
        // will reuse date obj

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

            // only count transactions for last week
            addToMap = addToMap && tx.getDate().after(beginWeek);

            if (addToMap) {
                // Convert date to readable month and day string, hash on that
                String date = formatDate(tx.getDate());
                if (!dayTxMap.containsKey(date)) {
                    dayTxMap.put(date, amount);
                } else {
                    dayTxMap.put(date, dayTxMap.get(date) + amount);
                }
            }
        }

        // Convert to chart entries
        List<BarEntry> entries = new ArrayList<>();

        // Map index of days of week to bar entries
        for (int i = 0; i < daysOfWeek.size(); i++) {
            String day = daysOfWeek.get(i);
            if (dayTxMap.containsKey(day)) {
                entries.add(new BarEntry(i, dayTxMap.get(day).floatValue()));
            } else {
                entries.add(new BarEntry(i, 0f));
            }
        }

        // Connect dataset to chart
        BarDataSet set = new BarDataSet(entries,
                "Daily " + (income ? "Income" : "Expenses"));
        set.setColors(new int[]{ income ? R.color.chart_green : R.color.chart_red}, getContext());

        BarData data = new BarData(set);
        BarChart chart = new BarChart(getContext());

        // Set data and style
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);

        chart.invalidate();

        return chart;
    }

    private PieChart generateByCategoryChart(List<Transaction> transactions, boolean income) {
        Map<String, Double> catAmtMap = new HashMap<String, Double>();

        Log.d(Utils.TAG, "transactions: " + transactions.size());


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
        set.setColors(graphColours, getContext());

        PieData data = new PieData(set);
        PieChart chart = new PieChart(getContext());

        chart.setData(data);
        chart.invalidate();

        return chart;
    }
}
