package com.linglong.sql.util;

import com.google.common.base.Strings;

public enum QuoteCharacter {

    BACK_QUOTE("`", "`"),

    SINGLE_QUOTE("'", "'"),

    QUOTE("\"", "\""),

    BRACKETS("[", "]"),

    NONE("", "");

    private final String startDelimiter;

    private final String endDelimiter;

    QuoteCharacter(String startDelimiter, String endDelimiter) {
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
    }

    public static QuoteCharacter getQuoteCharacter(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return QuoteCharacter.NONE;
        }
        for (QuoteCharacter each : QuoteCharacter.values()) {
            if (QuoteCharacter.NONE != each && each.startDelimiter.charAt(0) == value.charAt(0)) {
                return each;
            }
        }
        return QuoteCharacter.NONE;
    }
}
