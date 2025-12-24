package org.yyubin.api.common;

import java.text.DecimalFormat;
import java.util.Locale;

public final class CountFormatter {

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");

    private CountFormatter() {
    }

    public static String format(Long value) {
        if (value == null) {
            return null;
        }
        return format(value.longValue());
    }

    public static String format(long value) {
        if (value < 1000) {
            return Long.toString(value);
        }
        if (value < 1_000_000) {
            return ONE_DECIMAL.format(value / 1000.0) + "k";
        }
        if (value < 1_000_000_000) {
            return ONE_DECIMAL.format(value / 1_000_000.0) + "M";
        }
        return ONE_DECIMAL.format(value / 1_000_000_000.0) + "B";
    }
}
