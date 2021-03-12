
package com.linglong.sql.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public final class ExtractorUtils {

    public static ParserRuleContext getFirstChildNode(final ParserRuleContext node, final RuleName ruleName) {
        Optional<ParserRuleContext> result = findFirstChildNode(node, ruleName);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }

    public static Optional<ParserRuleContext> findFirstChildNode(final ParserRuleContext node, final RuleName ruleName) {
        Queue<ParserRuleContext> parserRuleContexts = new LinkedList<>();
        parserRuleContexts.add(node);
        ParserRuleContext parserRuleContext;
        while (null != (parserRuleContext = parserRuleContexts.poll())) {
            if (isMatchedNode(parserRuleContext, ruleName)) {
                return Optional.of(parserRuleContext);
            }
            for (int i = 0; i < parserRuleContext.getChildCount(); i++) {
                if (parserRuleContext.getChild(i) instanceof ParserRuleContext) {
                    parserRuleContexts.add((ParserRuleContext) parserRuleContext.getChild(i));
                }
            }
        }
        return Optional.absent();
    }

    public static Optional<ParserRuleContext> findFirstChildNodeNoneRecursive(final ParserRuleContext node, final RuleName ruleName) {
        return findFirstChildNodeNoneRecursive(node, ruleName, false);
    }

    public static Optional<ParserRuleContext> findFirstChildNodeNoneRecursive(final ParserRuleContext node, final RuleName ruleName, final boolean noneRoot) {
        if (!noneRoot && isMatchedNode(node, ruleName)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i) instanceof ParserRuleContext) {
                ParserRuleContext child = (ParserRuleContext) node.getChild(i);
                if (isMatchedNode(child, ruleName)) {
                    return Optional.of(child);
                }
            }
        }
        return Optional.absent();
    }

    public static Optional<ParserRuleContext> findSingleNodeFromFirstDescendant(final ParserRuleContext node, final RuleName ruleName) {
        ParserRuleContext nextNode = node;
        do {
            if (isMatchedNode(nextNode, ruleName)) {
                return Optional.of(nextNode);
            }
            if (1 != nextNode.getChildCount() || !(nextNode.getChild(0) instanceof ParserRuleContext)) {
                return Optional.absent();
            }
            nextNode = (ParserRuleContext) nextNode.getChild(0);
        } while (null != nextNode);
        return Optional.absent();
    }

    public static Collection<ParserRuleContext> getAllDescendantNodes(final ParserRuleContext node, final RuleName ruleName) {
        Collection<ParserRuleContext> result = new LinkedList<>();
        if (isMatchedNode(node, ruleName)) {
            result.add(node);
        }
        for (ParserRuleContext each : getChildrenNodes(node)) {
            result.addAll(getAllDescendantNodes(each, ruleName));
        }
        return result;
    }

    public static boolean isMatchedNode(final ParserRuleContext node, final RuleName ruleName) {
        return ruleName.getName().equals(node.getClass().getSimpleName());
    }

    public static Collection<ParserRuleContext> getChildrenNodes(final ParserRuleContext node) {
        Collection<ParserRuleContext> result = new LinkedList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof ParserRuleContext) {
                result.add((ParserRuleContext) child);
            }
        }
        return result;
    }
}
