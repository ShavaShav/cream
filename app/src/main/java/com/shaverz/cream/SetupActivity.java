package com.shaverz.cream;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.utils.CommonUtils;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GENERAL_SETUP = 0;
    private static final int ACCOUNTS_SETUP = 1;
    private static final int NUM_PAGES = 2;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ImageButton nextButton;

    // Store values from inner fragments
    private static String currency = CommonUtils.DEFAULT_CURRENCY;
    private static String language = CommonUtils.DEFAULT_LANGUAGE;
    private static double bankOpeningTransaction = 0.0;
    private static double cashOpeningTransaction = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        nextButton = findViewById(R.id.next_button);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.reports_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new SetupPageChangeListener());

        // Connect the dot indicators to view pager
        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);
    }

    private void finishSetup() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB.User.COLUMN_CURRENCY, currency);
        contentValues.put(DB.User.COLUMN_LANGUAGE, language);

        Uri currentUserURI = CommonUtils.getCurrentUserURI(this);

        // set currency and language for user
        getContentResolver().update(
                currentUserURI,
                contentValues,
                null,
                null);

        // add Bank and Cash accounts
        CommonUtils.createNewAccount(this, "Bank", bankOpeningTransaction);
        CommonUtils.createNewAccount(this, "Cash", cashOpeningTransaction);

        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // kill activity to prevent user from returning on backpress
    }

    // onclick for "next" button
    @Override
    public void onClick(View view) {
        switch (mViewPager.getCurrentItem()) {
            case GENERAL_SETUP:
                mViewPager.setCurrentItem(ACCOUNTS_SETUP); // next page
                break;
            case ACCOUNTS_SETUP:
                finishSetup(); // done clicked
                break;
        }
        mViewPager.setCurrentItem(mViewPager.getCurrentItem());
    }

    // Change the "next" button when viewpager changes
    public class SetupPageChangeListener implements ViewPager.OnPageChangeListener {
        public void onPageScrollStateChanged(int state) {}
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        public void onPageSelected(int position) {
            switch (position) {
                case GENERAL_SETUP:
                    // general setup gets the next button
                    nextButton.setImageDrawable(getDrawable(R.drawable.ic_arrow_forward));
                    break;
                case ACCOUNTS_SETUP:
                    // accounts setup uses finish button
                    nextButton.setImageDrawable(getDrawable(R.drawable.ic_done));
                    break;
            }
        }
    }

    /*
        FRAGMENTS
     */

    public static class SetupAccountFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_setup_account, container, false);

            EditText cashOpeningView = (EditText) view.findViewById(R.id.bank_edittext);
            EditText bankOpeningView = (EditText) view.findViewById(R.id.cash_edittext);

            cashOpeningView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        cashOpeningTransaction = Double.parseDouble(charSequence.toString());
                    } catch (Exception e) {
                        cashOpeningTransaction = 0.00; // invalid double - set to 0
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            bankOpeningView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        bankOpeningTransaction = Double.parseDouble(charSequence.toString());
                    } catch (Exception e) {
                        bankOpeningTransaction = 0.00; // invalid double - set to 0
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            return view;
        }
    }

    public static class GeneralAccountFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_setup_general, container, false);
            Spinner currencySpinner = (Spinner) view.findViewById(R.id.currency_spinner);
            Spinner languageSpinner = (Spinner) view.findViewById(R.id.language_spinner);

            // Create ArrayAdapters using the string arrays and default spinner layouts
            ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.currency_array, android.R.layout.simple_spinner_item);
            final ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.language_array, android.R.layout.simple_spinner_item);

            // Specify the layout to use when the list of choices appears
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Apply the adapters to the spinners
            currencySpinner.setAdapter(currencyAdapter);
            languageSpinner.setAdapter(languageAdapter);

            currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    currency = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    language = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            return view;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case GENERAL_SETUP:
                    return new GeneralAccountFragment();
                case ACCOUNTS_SETUP:
                    return new SetupAccountFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
