package com.shaverz.cream.utils;

import com.shaverz.cream.MainActivity;
import com.shaverz.cream.models.Transaction;

import java.util.Calendar;
import java.util.List;

/**
 * Created by z on 06/04/2018.
 */

public class RecentPeriod implements Period {
    private final String[] STRINGS = {
            "All",
            "Today",
            "Yesterday",
            "Past Week",
            "Past Month",
            "Past Year"
    };

    @Override
    public String[] getPeriodStrings() {
        return STRINGS;
    }

    public Period.DateRange getDateRange(String periodString) {

        Calendar startDate = Calendar.getInstance(); // now
        Calendar endDate = Calendar.getInstance(); // now

        switch (periodString) {
            case "Today":
                startDate.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "Yesterday":
                startDate.add(Calendar.DAY_OF_YEAR, -2);
                endDate.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "Past Week":
                startDate.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "Past Month":
                startDate.add(Calendar.MONTH, -1);
                break;
            case "Past Year":
                startDate.add(Calendar.YEAR, -1);
                break;
            default: // show all
                List<Transaction> list = MainActivity.CURRENT_USER.getTransactions();
                startDate.setTime(list.get(list.size()-1).getDate().getTime()); // earliest transaction
                startDate.add(Calendar.DAY_OF_YEAR, -1); // move back a day, so comparisons are safe
                break;
        }
        return new Period.DateRange(startDate,endDate);

    }

}
