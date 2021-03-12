package com.linglong.sql.util;

import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Stereo on 2019/10/9.
 */
public enum LogicalOperator {

    AND("AND", "&&"),
    OR("OR", "||");

    private final Collection<String> texts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    LogicalOperator(final String... texts) {
        this.texts.addAll(Arrays.asList(texts));
    }

    public static Optional<LogicalOperator> valueFrom(final String text) {
        for (LogicalOperator each : LogicalOperator.values()) {
            if (each.texts.contains(text)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}