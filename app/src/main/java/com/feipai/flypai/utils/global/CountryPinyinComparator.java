package com.feipai.flypai.utils.global;

import com.feipai.flypai.beans.CountryBean;

import java.util.Comparator;

public class CountryPinyinComparator implements Comparator<CountryBean> {
    public int compare(CountryBean o1, CountryBean o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
