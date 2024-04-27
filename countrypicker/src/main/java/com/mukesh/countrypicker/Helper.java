package com.mukesh.countrypicker;

import java.util.Currency;

public class Helper {

    public static String getCountrySymbol(String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return currency.getSymbol();
        } catch (Exception ignore){}
        return null;
    }

}
