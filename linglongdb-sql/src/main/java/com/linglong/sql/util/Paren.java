
package com.linglong.sql.util;


public enum Paren {

    PARENTHESES('(', ')'), BRACKET('[', ']'), BRACES('{', '}');

    private final char leftParen;

    private final char rightParen;

    Paren(char leftParen, char rightParen) {
        this.leftParen = leftParen;
        this.rightParen = rightParen;
    }

    public static boolean isLeftParen(final char token) {
        for (Paren each : Paren.values()) {
            if (each.leftParen == token) {
                return true;
            }
        }
        return false;
    }

    public static boolean match(final char leftToken, final char rightToken) {
        for (Paren each : Paren.values()) {
            if (each.leftParen == leftToken && each.rightParen == rightToken) {
                return true;
            }
        }
        return false;
    }

    public char getLeftParen() {
        return leftParen;
    }

    public char getRightParen() {
        return rightParen;
    }
}
