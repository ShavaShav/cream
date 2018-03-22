package com.shaverz.cream;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GENERAL_SETUP = 0;
    private static final int ACCOUNTS_SETUP = 1;
    private static final int NUM_PAGES = 2;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ImageButton nextButton;

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
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new SetupPageChangeListener());

        // Connect the dot indicators to view pager
        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);
    }

    private void finishSetup() {
        // TODO: Save values from fragments for use in MainActivity
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
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_setup_account, container, false);
        }
    }

    public static class GeneralAccountFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_setup_general, container, false);
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
