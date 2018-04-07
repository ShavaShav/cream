package com.shaverz.cream;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shaverz.cream.utils.ChartGenerator;


public class ReportsFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Fragment[] reportTabFragments;
    private static FragmentManager fragmentManager;

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("tag", "Creating view for reports");
        // preload fragments
        reportTabFragments = new Fragment[] {
                new ExpenseFragment(),
                new IncomeFragment(),
                new CashFlowFragment(),
                new BalanceFragment()
        };

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // Save fragment manager
        fragmentManager = getActivity().getSupportFragmentManager();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.reports_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.setTabTextColors(R.color.app_bar_title_color, R.color.colorAccent);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.titlebar_reports));
    }

       /*
        FRAGMENTS
     */

    public static class ExpenseFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_expense, container, false);
            view.findViewById(R.id.report_expense_by_category).setOnClickListener(new ReportClickListener());
            view.findViewById(R.id.report_daily_expense).setOnClickListener(new ReportClickListener());
            view.findViewById(R.id.report_monthly_expense).setOnClickListener(new ReportClickListener());
            return view;
        }
    }

    public static class IncomeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_income, container, false);
            view.findViewById(R.id.report_income_by_category).setOnClickListener(new ReportClickListener());
            view.findViewById(R.id.report_daily_income).setOnClickListener(new ReportClickListener());
            view.findViewById(R.id.report_monthly_income).setOnClickListener(new ReportClickListener());
            return view;
        }
    }

    public static class CashFlowFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_cash_flow, container, false);
            view.findViewById(R.id.report_income_vs_expense).setOnClickListener(new ReportClickListener());
            return view;
        }
    }

    public static class BalanceFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_balance, container, false);
            view.findViewById(R.id.report_daily_balance).setOnClickListener(new ReportClickListener());
            return view;
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return reportTabFragments[position];

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return reportTabFragments.length;
        }
    }

    private static class ReportClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            // Pass report graph type to view frag according to button clicked
            Bundle arguments = new Bundle();

            switch (view.getId()) {
                case R.id.report_expense_by_category:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.EXPENSE_BY_CATEGORY);
                    break;
                case R.id.report_daily_expense:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.DAILY_EXPENSES);
                    break;
                case R.id.report_monthly_expense:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.MONTHLY_EXPENSES);
                    break;
                case R.id.report_income_by_category:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.INCOME_BY_CATEGORY);
                    break;
                case R.id.report_daily_income:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.DAILY_INCOME);
                    break;
                case R.id.report_monthly_income:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.MONTHLY_INCOME);
                    break;
                case R.id.report_income_vs_expense:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.INCOME_VS_EXPENSE);
                    break;
                case R.id.report_daily_balance:
                    arguments.putInt(ReportViewFragment.ARG_REPORT_TYPE,
                            ChartGenerator.DAILY_BALANCE);
                    break;
            }

            ReportViewFragment reportViewFragment = new ReportViewFragment();
            reportViewFragment.setArguments(arguments);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, reportViewFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
