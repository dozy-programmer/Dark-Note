package com.mukesh.countrypicker;

import java.util.Currency;
import java.util.regex.Pattern;

public class Helper {

    private static final Pattern pattern = Pattern.compile("[a-zA-Z]+");

    public static String getCountrySymbol(String currencyCode){
        Currency currency = Currency.getInstance(currencyCode);
        return currency.getSymbol();
    }

    public static boolean onlyContainsLetters(String text) {
        return pattern.matcher(text).matches();
    }

    public static String removeAllLetters(String text){
        return pattern.matcher(text).replaceAll("");
    }

}
