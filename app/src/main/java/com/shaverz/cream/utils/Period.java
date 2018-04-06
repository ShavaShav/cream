package com.shaverz.cream.utils;

import android.util.Pair;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    }

}
