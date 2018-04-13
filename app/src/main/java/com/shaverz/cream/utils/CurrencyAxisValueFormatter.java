package com.shaverz.cream.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by z on 13/04/2018.
 */

public class CurrencyAxisValueFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String valueString = CommonUtils.getCurrencyString(value);
        return valueString.substring(0, valueString.indexOf('.'));

    }
}
