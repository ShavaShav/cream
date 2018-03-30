package com.shaverz.cream;

import android.util.Pair;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by z on 29/03/2018.
 */

public class Period {

    public static final String[] strings = {
            "All",
            "Today",
            "Yesterday",
            "Last 7 Days",
            "Last 30 days"};


    public static DateRange getDateRange(String periodString) {

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
            case "Last 7 days":
                startDate.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "Last 30 days":
                startDate.add(Calendar.DAY_OF_YEAR, -30);
                break;
            default: // show all
                startDate.setTimeInMillis(Long.MIN_VALUE); // earliest possible time
                break;
        }
        return new DateRange(startDate,endDate);

    }

    public static class DateRange {
        public final Calendar startDate;
        public final Calendar endDate;

        public DateRange(Calendar startDate, Calendar endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

}
