package com.shaverz.cream.utils;

import java.util.Calendar;

/**
 * Created by z on 29/03/2018.
 */

public interface Period {

    String[] getPeriodStrings();

    DateRange getDateRange(String periodString);

    class DateRange {
        public final Calendar startDate;
        public final Calendar endDate;

        public DateRange(Calendar startDate, Calendar endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public DateRange() {
            this.startDate = Calendar.getInstance();
            this.startDate.setTimeInMillis(Long.MIN_VALUE); // earliest possible time

            this.endDate = Calendar.getInstance(); // now
        }
    }

}
