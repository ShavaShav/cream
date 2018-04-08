package com.shaverz.cream.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by z on 06/04/2018.
 */

public class AnnualPeriod implements Period {

    private final String[] STRINGS;

    public AnnualPeriod() {
        List<String> strings = new ArrayList<>();
        strings.add("Past 6 Months"); // add a couple options for recent past
        strings.add("Past Year");

        // add last 10 years
        Calendar year = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            strings.add(String.valueOf(year.get(Calendar.YEAR)));
            year.add(Calendar.YEAR, -1);
        }
        STRINGS = strings.toArray(new String[strings.size()]);
    }

    @Override
    public String[] getPeriodStrings() {
        return STRINGS;
    }

    @Override
    public DateRange getDateRange(String periodString) {
        Calendar startDate = Calendar.getInstance(); // now
        Calendar endDate = Calendar.getInstance(); // now

        try {
            // parse start year from string
            Integer year = Integer.parseInt(periodString);

            // set start date to beginning of year
            startDate.set(Calendar.YEAR, year);
            startDate.set(Calendar.DAY_OF_YEAR, 1);
            startDate.set(Calendar.HOUR, 0);
            startDate.set(Calendar.MINUTE, 0);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);

            // 1 millisecond before new years
            endDate.set(Calendar.YEAR, year);
            endDate.set(Calendar.MONTH, 11);
            endDate.set(Calendar.DAY_OF_MONTH, 31);
            endDate.set(Calendar.HOUR, 23);
            endDate.set(Calendar.MINUTE, 59);
            endDate.set(Calendar.SECOND, 59);
            endDate.set(Calendar.MILLISECOND, 59);

        } catch (Exception e) {
            // not a number, do others
            switch (periodString) {
                case "Past 6 Months":
                    startDate.add(Calendar.MONTH, -5);
                    startDate.set(Calendar.DAY_OF_MONTH, 1);
                    startDate.set(Calendar.HOUR, 0);
                    startDate.set(Calendar.MINUTE, 0);
                    startDate.set(Calendar.SECOND, 0);
                    break;
                case "Past Year":
                    startDate.add(Calendar.MONTH, -11);
                    break;
                default: // past year
                    startDate.add(Calendar.YEAR, -1);
                    break;
            }
        }
        return new Period.DateRange(startDate,endDate);
    }
}
