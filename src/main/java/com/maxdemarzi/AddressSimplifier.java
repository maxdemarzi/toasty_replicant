package com.maxdemarzi;

import org.simmetrics.simplifiers.Simplifier;

import java.util.HashMap;
import java.util.Iterator;

public class AddressSimplifier implements Simplifier {
    @Override
    public String simplify(String pString) {
        String value = pString.toUpperCase();
        Iterator<String> i = commonItems.keySet().iterator();
        while(i.hasNext()) {
            String regex = i.next();
            String newWord = commonItems.get(regex);
            value = value.replaceAll(regex, newWord);
        }
        return value;
    }

    private static HashMap<String, String> commonItems = new HashMap<String,String>() {{
        put(" ST$", " STREET");
        put(" ST ", " STREET ");
        put("^P O| P O|^POST OFFICE| POST OFFICE", "PO");
        put(" P O| POST OFFICE", " PO");
        put(" AVE$", " AVENUE");
        put(" AVE ", " AVENUE ");
        put(" BVD$| BLVD$", " BOULEVARD");
        put(" BVD | BLVD ", " BOULEVARD ");
        put(" RD$", " ROAD");
        put(" RD ", " ROAD ");
        put(" STE ", " SUITE ");
        put(" RTE$| RT$", " ROUTE");
        put("^RTE|^RT", "ROUTE");
        put(" RT | RTE ", " ROUTE ");
        put("^1ST|^1|1ST$", "FIRST");
        put(" 1 | 1ST ", " FIRST ");
        put("^2ND|^2|2ND$", "SECOND");
        put(" 2 | 2ND ", " SECOND ");
        put("^3RD|^3|3RD$", "THIRD");
        put("^ 3 | 3RD ", " THIRD ");
        put("^4TH|^4|4TH$", "FOURTH");
        put(" 4 | 4TH ", " FOURTH ");
        put("^5TH|^5|5TH$", "FIFTH");
        put(" 5 | 5TH ", " FIFTH ");
        put("^6TH|^6|6TH$", "SIXTH");
        put("^ 6 | 6TH ", " SIXTH ");
        put("^7TH|^7|7TH$", "SEVENTH");
        put("^ 7 | 7TH ", " SEVENTH ");
        put("^8TH|^8|8TH$", "EIGHTH");
        put("^ 8 | 8TH ", " EIGHTH ");
        put("^9TH|^9|9TH$", "NINTH");
        put("^ 9 | 9TH ", " NINTH ");
        put("^10TH|^10|10TH$", "TENTH");
        put("^ 10 | 10TH ", " TENTH ");
    }};

    @Override
    public String toString() {
        return "AddressSimplifier";
    }

}