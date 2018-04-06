package com.shaverz.cream.utils;

import android.content.Context;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.shaverz.cream.MainActivity;
import com.shaverz.cream.R;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by z on 06/04/2018.
 */

public class ChartGenerator {
    private Context context;

    public static final int EXPENSE_BY_CATEGORY = 1;
    public static final int DAILY_EXPENSES = 2;
    public static final int MONTHLY_EXPENSES = 3;
    public static final int INCOME_BY_CATEGORY = 4;
    public static final int DAILY_INCOME = 5;
    public static final int MONTHLY_INCOME = 6;
    public static final int DAILY_BALANCE = 7;
    public static final int INCOME_VS_EXPENSE = 8;
    
    public ChartGenerator(Context context) {
        this.context = context;
    }

    private String formatDate (Calendar c) {
        return new SimpleDateFormat("MMM dd").format(c.getTime());
    }

    private String formatMonth (Calendar c) {
        return new SimpleDateFormat("MMM").format(c.getTime());
    }

    private int[] graphColours = new int[] {
            R.color.chart_blue,
            R.color.chart_green,
            R.color.chart_orange,
            R.color.chart_red,
            R.color.chart_black };

    public void shuffleColours() {
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

    public Chart generateChart(int reportType, Account account, Period.DateRange dateRange) {

        // trim transactions to show according to account and period settings
        List<Transaction> transactionList;

        if (account != null) {
            transactionList = MainActivity.CURRENT_USER.getAccount(account.getId()).getTransactionList();
        } else {
            transactionList = MainActivity.CURRENT_USER.getTransactions(); // all by default
        }

        List<Transaction> transactionsToChart;
        double openingTransactionListBalance = 0.0;

        if (dateRange == null) {
            // no period, no further filter
            transactionsToChart = transactionList;
            dateRange = new Period.DateRange();
        } else {
            // only show data for transactions within date range -> makes a copy so user models aren't overwritten
            transactionsToChart = new ArrayList<>();
            for (Transaction t : transactionList) {
                if (t.getDate().after(dateRange.startDate) && t.getDate().before(dateRange.endDate)) {
                    transactionsToChart.add(t);
                } else if (t.getDate().before(dateRange.startDate)) {
                    openingTransactionListBalance += t.getAmount();
                }
            }
        }

        switch (reportType) {
            case EXPENSE_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, false);
            case DAILY_EXPENSES:
                return generateDailyTxChart(transactionsToChart, false);
            case MONTHLY_EXPENSES:
                return generateMonthlyTxChart(transactionsToChart, false, dateRange);
            case INCOME_BY_CATEGORY:
                return generateByCategoryChart(transactionsToChart, true);
            case DAILY_INCOME:
                return generateDailyTxChart(transactionsToChart, true);
            case MONTHLY_INCOME:
                return generateMonthlyTxChart(transactionsToChart, true, dateRange);
            case DAILY_BALANCE:
                return generateDailyBalanceChart(transactionsToChart, dateRange, openingTransactionListBalance);
            case INCOME_VS_EXPENSE:
                return generateIncomeVsExpensesChart(transactionsToChart);
            default:
                return new LineChart(context); // empty chart
        }
    }

    /*
        INCOME AND EXPENSE - VARIED REPORTS
     */

    public BarChart generateMonthlyTxChart(final List<Transaction> transactions, boolean income, Period.DateRange dateRange){
        Map<String, Double> monthTxMap = new HashMap<String, Double>();

        // Store string names of months
        List<String> monthsOfYear = new ArrayList<>();
        Calendar c = (Calendar) dateRange.startDate.clone();
        // each month in range
        for (c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
             c.before(dateRange.endDate);
             c.add(Calendar.MONTH, 1)) {
            monthsOfYear.add(formatMonth(c));
        }

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
                // Convert date to readable month string, hash on that
                String month = formatMonth(tx.getDate());
                if (!monthTxMap.containsKey(month)) {
                    monthTxMap.put(month, amount);
                } else {
                    monthTxMap.put(month, monthTxMap.get(month) + amount);
                }
            }
        }

        // Convert to chart entries
        List<BarEntry> entries = new ArrayList<>();

        // Map index of months of year to bar entries
        for (int i = 0; i < monthsOfYear.size(); i++) {
            String month = monthsOfYear.get(i);
            if (monthTxMap.containsKey(month)) {
                entries.add(new BarEntry(i, monthTxMap.get(month).floatValue()));
            } else {
                entries.add(new BarEntry(i, 0f)); // create data for empty months
            }
        }

        // Connect dataset to chart
        BarDataSet set = new BarDataSet(entries,
                "Monthly " + (income ? "Income" : "Expenses"));
        set.setColors(new int[]{ income ? R.color.chart_green : R.color.chart_red}, context);

        BarData data = new BarData(set);
        BarChart chart = new BarChart(context);

        // Set data and style
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthsOfYear));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);
        chart.getXAxis().setLabelCount(monthsOfYear.size());

        chart.invalidate();

        return chart;
    }

    public BarChart generateDailyTxChart(final List<Transaction> transactions, boolean income) {
        Map<String, Double> dayTxMap = new HashMap<String, Double>();

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
        set.setColors(new int[]{ income ? R.color.chart_green : R.color.chart_red}, context);

        BarData data = new BarData(set);
        BarChart chart = new BarChart(context);

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
        chart.getXAxis().setLabelCount(daysOfWeek.size());

        chart.invalidate();

        return chart;
    }

    public PieChart generateByCategoryChart(List<Transaction> transactions, boolean income) {
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
        set.setColors(graphColours, context);

        PieData data = new PieData(set);
        PieChart chart = new PieChart(context);

        chart.setData(data);
        chart.invalidate();

        return chart;
    }

    /*
        INCOME VS EXPENSES
     */

    public PieChart generateIncomeVsExpensesChart(List<Transaction> transactions) {
        Double totalExpenses = 0.00;
        Double totalIncome = 0.00;

        // Sum income and expenses
        for (Transaction tx : transactions) {
            double amount = tx.getAmount();
            // Only count transactions according to flag
            if (amount < 0.00) {
                totalExpenses -= amount;
            } else if (amount > 0.00) {
                totalIncome += amount; // income
            }
        }

        // Convert to chart entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalIncome.floatValue(), "Income"));
        entries.add(new PieEntry(totalExpenses.floatValue(), "Expenses"));

        // Connect chart parts and return
        PieDataSet set = new PieDataSet(entries, "Income vs Expenses");
        set.setColors(new int[]{R.color.chart_green, R.color.chart_red}, context);

        PieData data = new PieData(set);
        PieChart chart = new PieChart(context);

        chart.setData(data);
        chart.invalidate();

        return chart;
    }

    /*
        INCOME VS EXPENSES
     */

    public LineChart generateDailyBalanceChart(List<Transaction> transactions,
                                                Period.DateRange dateRange,
                                                double openingTransactionListBalance) {
        Map<String, Double> dayTxMap = new HashMap<String, Double>();

        // Store string names of dates through the week
        List<String> days = new ArrayList<>();

        Calendar d = (Calendar) dateRange.startDate.clone();
        // each day in range
        for (d.set(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
             d.before(dateRange.endDate);
             d.add(Calendar.DAY_OF_YEAR, 1)) {
            days.add(formatDate(d));
        }

        Collections.reverse(transactions);

        double runningBalance = openingTransactionListBalance;

        // Map income/expense by day, and get opening balance
        for (Transaction tx : transactions) {
            runningBalance += tx.getAmount();
            if (tx.getDate().after(dateRange.startDate) && tx.getDate().before(dateRange.endDate)){
                // Convert date to readable month and day string, hash the current day's balance on that
                String date = formatDate(tx.getDate());
                dayTxMap.put(date, runningBalance);
            }
        }

        // Convert to chart entries
        List<Entry> entries = new ArrayList<>();

        // Map index of days of week to bar entries
        Double lastBalance = openingTransactionListBalance;
        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            if (dayTxMap.containsKey(day)) {
                entries.add(new Entry(i, dayTxMap.get(day).floatValue()));
                lastBalance = dayTxMap.get(day);
            } else {
                entries.add(new Entry(i, lastBalance.floatValue()));
            }
        }

        // Connect dataset to chart
        LineDataSet set = new LineDataSet(entries, "Daily Balance");
        set.setColors(new int[]{R.color.chart_black}, context);

        LineData data = new LineData(set);
        LineChart chart = new LineChart(context);

        // Set data and style
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);

        chart.invalidate();

        return chart;
    }
}
