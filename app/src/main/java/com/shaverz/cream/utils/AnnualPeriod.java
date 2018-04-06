package com.shaverz.cream.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

            // 1 year from start date
            endDate.set(Calendar.YEAR, year + 1);
            endDate.set(Calendar.DAY_OF_YEAR, 1);
            endDate.set(Calendar.HOUR, 0);
            endDate.set(Calendar.MINUTE, 0);

        } catch (Exception e) {
            // not a number, do others
            switch (periodString) {
                case "Past 6 Months":
                    startDate.add(Calendar.MONTH, -6);
                    break;
                case "Past Year":
                    startDate.add(Calendar.YEAR, -1);
                    break;
                default: // past year
                    startDate.add(Calendar.YEAR, -1);
                    break;
            }
        }
        return new Period.DateRange(startDate,endDate);
    }
}
