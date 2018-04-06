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

import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;
import com.shaverz.cream.utils.AnnualPeriod;
import com.shaverz.cream.utils.ChartGenerator;
import com.shaverz.cream.utils.RecentPeriod;
import com.shaverz.cream.utils.Period;

import java.util.ArrayList;
import java.util.List;

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
    private Period period;
    private Chart chart;
    private ChartGenerator chartGen;

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

        chartGen = new ChartGenerator(getContext());
        chartGen.shuffleColours(); // shuffle for initial view

        if (reportType == MONTHLY_EXPENSES || reportType == MONTHLY_INCOME) {
            period = new AnnualPeriod();
        } else {
            period = new RecentPeriod();
        }
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

        if (reportType != DAILY_EXPENSES && reportType != DAILY_INCOME) {

            periodExists = true;

            periodSpinner = (Spinner) view.findViewById(R.id.spinner_period);
            periodArrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item,
                    period.getPeriodStrings());

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
        Period.DateRange dateRange;

        double openingTransactionListBalance = 0.0;

        if ( ! periodExists) {
            // no period, no further filter
            transactionsToChart = transactionList;
            dateRange = new Period.DateRange();
        } else {
            // copy transactions within period
            String periodString = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());
            dateRange = period.getDateRange(periodString);

            // only show data for transactions within date range -> makes a copy so user models aren't overwritten
            transactionsToChart = new ArrayList<>();
            for (Transaction t : transactionList){
                if (t.getDate().after(dateRange.startDate) && t.getDate().before(dateRange.endDate)) {
                    transactionsToChart.add(t);
                } else if (t.getDate().before(dateRange.startDate)) {
                    openingTransactionListBalance += t.getAmount();
                }
            }
        }

        switch (reportType) {
            case EXPENSE_BY_CATEGORY:
                return chartGen.generateByCategoryChart(transactionsToChart, false);
            case DAILY_EXPENSES:
                return chartGen.generateDailyTxChart(transactionsToChart, false);
            case MONTHLY_EXPENSES:
                return chartGen.generateMonthlyTxChart(transactionsToChart, false, dateRange);
            case INCOME_BY_CATEGORY:
                return chartGen.generateByCategoryChart(transactionsToChart, true);
            case DAILY_INCOME:
                return chartGen.generateDailyTxChart(transactionsToChart, true);
            case MONTHLY_INCOME:
                return chartGen.generateMonthlyTxChart(transactionsToChart, true, dateRange);
            case DAILY_BALANCE:
                return chartGen.generateDailyBalanceChart(transactionsToChart, dateRange, openingTransactionListBalance);
            case INCOME_VS_EXPENSE:
                return chartGen.generateIncomeVsExpensesChart(transactionsToChart);
            default:
                return new LineChart(getContext()); // empty chart
        }

    }

}
