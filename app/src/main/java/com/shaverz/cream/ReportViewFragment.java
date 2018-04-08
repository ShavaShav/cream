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

import com.shaverz.cream.models.Account;
import com.shaverz.cream.utils.AnnualPeriod;
import com.shaverz.cream.utils.ChartGenerator;
import com.shaverz.cream.utils.RecentPeriod;
import com.shaverz.cream.utils.Period;

import java.util.ArrayList;
import java.util.List;

public class ReportViewFragment extends Fragment {

    public static final String ARG_REPORT_TYPE = "report_type";

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
            reportType = ChartGenerator.EXPENSE_BY_CATEGORY; // default
        }

        chartGen = new ChartGenerator(getContext());
        chartGen.shuffleColours(); // shuffle for initial view

        if (reportType == ChartGenerator.MONTHLY_EXPENSES || reportType == ChartGenerator.MONTHLY_INCOME) {
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

        if (reportType != ChartGenerator.DAILY_EXPENSES && reportType != ChartGenerator.DAILY_INCOME) {

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
        chart = makeChartFromSpinners();

        // Add chart to graph_frame, fill it
        graphFrame = view.findViewById(R.id.graph_frame);
        graphFrame.addView(chart, chartParams);

        return view;
    }

    private Chart makeChartFromSpinners() {
        // get account from spinner
        Account account = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());

        if (Integer.parseInt(account.getId()) < 0) {
            account = null;
        }

        // get date range from spinner, if existent
        Period.DateRange dateRange = null;

        if ( periodExists ) {
            // copy transactions within period
            String periodString = periodArrayAdapter.getItem(periodSpinner.getSelectedItemPosition());
            dateRange = period.getDateRange(periodString);
        }

        return chartGen.generateChart(reportType, account, dateRange);
    }

    private class optionsChangeListener implements AdapterView.OnItemSelectedListener {
        // will refresh chart whenever options change
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            graphFrame.removeView(chart);
            chart = makeChartFromSpinners();
            graphFrame.addView(chart, chartParams);
            chart.invalidate();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}
