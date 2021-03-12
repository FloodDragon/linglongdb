package com.linglong.sql.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class SQLUtil {
    
    public static Number getExactlyNumber(final String value, final int radix) {
        try {
            return getBigInteger(value, radix);
        } catch (final NumberFormatException ex) {
            return new BigDecimal(value);
        }
    }
    
    private static Number getBigInteger(final String value, final int radix) {
        BigInteger result = new BigInteger(value, radix);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
    }
    
    public static String getExactlyValue(final String value) {
        return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
    }
    
    public static String getExactlyExpression(final String value) {
        return Strings.isNullOrEmpty(value) ? value : CharMatcher.anyOf(" ").removeFrom(value);
    }
    
    public static String getExpressionWithoutOutsideParentheses(final String value) {
        int parenthesesOffset = getParenthesesOffset(value);
        return 0 == parenthesesOffset ? value : value.substring(parenthesesOffset, value.length() - parenthesesOffset);
    }
    
    private static int getParenthesesOffset(final String value) {
        int result = 0;
        while (Paren.PARENTHESES.getLeftParen() == value.charAt(result)) {
            result++;
        }
        return result;
    }
}
