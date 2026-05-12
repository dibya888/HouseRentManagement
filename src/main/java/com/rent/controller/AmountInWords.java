package com.rent.controller;

public class AmountInWords {

    private static final String[] belowTwenty = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String toWordsBDT(double amount) {
        long taka = (long) Math.floor(amount + 0.00001);
        long paisa = Math.round((amount - taka) * 100);

        String takaWords = convert(taka).trim();
        if (takaWords.isEmpty()) takaWords = "Zero";

        if (paisa > 0) {
            String paisaWords = convert(paisa).trim();
            return takaWords + " Taka and " + paisaWords + " Paisa Only";
        }
        return takaWords + " Taka Only";
    }

    private static String convert(long n) {
        if (n == 0) return "";
        if (n < 20) return belowTwenty[(int) n];
        if (n < 100) return tens[(int) (n / 10)] + (n % 10 != 0 ? " " + convert(n % 10) : "");
        if (n < 1000) return convert(n / 100) + " Hundred" + (n % 100 != 0 ? " " + convert(n % 100) : "");
        if (n < 100000) return convert(n / 1000) + " Thousand" + (n % 1000 != 0 ? " " + convert(n % 1000) : "");
        if (n < 10000000) return convert(n / 100000) + " Lakh" + (n % 100000 != 0 ? " " + convert(n % 100000) : "");
        return convert(n / 10000000) + " Crore" + (n % 10000000 != 0 ? " " + convert(n % 10000000) : "");
    }
}