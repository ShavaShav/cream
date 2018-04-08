package com.shaverz.cream.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by z on 08/04/2018.
 */

public class OverviewCustomization {
    // This must match default order of cardviews in OverviewFragment
    // and the array in arrays.xml
    public static final int HIGH_SPENDING = 0;
    public static final int MY_ACCOUNTS = 1;
    public static final int RECENT_TRANSACTIONS = 2;
    public static final int EXPENSE_VS_EXPENSE = 3;
    public static final int EXPENSE_BY_CATEGORY = 4;

    private List<Boolean> isVisibleList;
    private List<Integer> orderList;
    private static final String strSeparator = "__,__";

    public OverviewCustomization (String visibleCustomization, String orderCustomization) {
        isVisibleList = parseVisibleDBString(visibleCustomization);
        orderList = parseOrderDBString(orderCustomization);

    }

    // default setup for overview
    public OverviewCustomization () {
        isVisibleList = new ArrayList<>();
        isVisibleList.add(HIGH_SPENDING, false);
        isVisibleList.add(MY_ACCOUNTS, true);
        isVisibleList.add(RECENT_TRANSACTIONS, true);
        isVisibleList.add(EXPENSE_VS_EXPENSE, true);
        isVisibleList.add(EXPENSE_BY_CATEGORY, true);

        orderList = new ArrayList<>();
        orderList.add(0, HIGH_SPENDING);
        orderList.add(1, MY_ACCOUNTS);
        orderList.add(2, RECENT_TRANSACTIONS);
        orderList.add(3, EXPENSE_VS_EXPENSE);
        orderList.add(4, EXPENSE_BY_CATEGORY);
    }

    public List<Boolean> getOverviewVisibilityList () {
        return isVisibleList;
    }

    public List<Integer> getOverviewOrderList () {
        return orderList;
    }

    private List<Boolean> parseVisibleDBString(String str) {
        List<Boolean> list = new ArrayList<>();;
        String[] arr = str.split(strSeparator);
        for (String s : arr) {
            list.add(Boolean.parseBoolean(s));
        }
        return list;
    }

    public static String getVisibleDBString(List<Boolean> isVisibleList){
        String str = "";
        for (int i = 0; i < isVisibleList.size(); i++) {
            str = str + isVisibleList.get(i);
            // Do not append comma at the end of last element
            if(i < isVisibleList.size()-1){
                str = str+strSeparator;
            }
        }
        return str;
    }

    public String getVisibleDBString(){
        return getVisibleDBString(isVisibleList);
    }

    private List<Integer> parseOrderDBString(String str) {
        List<Integer> list = new ArrayList<>();;
        String[] arr = str.split(strSeparator);
        for (String s : arr) {
            list.add(Integer.parseInt(s));
        }
        return list;
    }

    public static String getOrderDBString(List<Integer> orderList){
        String str = "";
        for (int i = 0; i < orderList.size(); i++) {
            str = str + orderList.get(i);
            // Do not append comma at the end of last element
            if(i < orderList.size()-1){
                str = str+strSeparator;
            }
        }
        return str;
    }

    public String getOrderDBString(){
        return getOrderDBString(orderList);
    }
}
