package com.shaverz.cream;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ReportsFragment extends Fragment {
    private static final int EXPENSE = 0;
    private static final int INCOME = 1;
    private static final int CASHFLOW = 2;
    private static final int BALANCE = 3;
    private static final int NUM_PAGES = 4;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
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

            return view;
        }
    }

    public static class IncomeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_income, container, false);

            return view;
        }
    }

    public static class CashFlowFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_cash_flow, container, false);

            return view;
        }
    }

    public static class BalanceFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_reports_balance, container, false);

            return view;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case EXPENSE:
                    return new ExpenseFragment();
                case INCOME:
                    return new IncomeFragment();
                case CASHFLOW:
                    return new CashFlowFragment();
                case BALANCE:
                    return new BalanceFragment();
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return NUM_PAGES;
        }
    }

}
