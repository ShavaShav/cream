package com.shaverz.cream.utils;

import java.util.Calendar;

/**
 * Created by z on 06/04/2018.
 */

public class CommonPeriod implements Period {
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
                startDate.setTimeInMillis(Long.MIN_VALUE); // earliest possible time
                break;
        }
        return new Period.DateRange(startDate,endDate);

    }

}
