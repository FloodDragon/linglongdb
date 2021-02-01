package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.ClosedIndexException;
import com.glodon.linglong.base.exception.UnpositionedCursorException;
import com.glodon.linglong.engine.core.frame.Scanner;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public class SortScanner implements Scanner {
    private final LocalDatabase mDatabase;
    private TreeCursor mCursor;
    private Supplier mSupplier;

    public SortScanner(LocalDatabase db) {
        mDatabase = db;
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return cursor().getComparator();
    }

    @Override
    public byte[] key() {
        return cursor().key();
    }

    @Override
    public byte[] value() {
        return cursor().value();
    }

    @Override
    public boolean step() throws IOException {
        TreeCursor c = cursor();
        try {
            doStep(c);
            if (c.key() != null) {
                return true;
            }
            mDatabase.quickDeleteTemporaryTree(c.mTree);
            return false;
        } catch (UnpositionedCursorException e) {
            return false;
        } catch (Throwable e) {
            throw ViewUtils.fail(this, e);
        }
    }

    protected void doStep(TreeCursor c) throws IOException {
        c.deleteNext();
    }

    @Override
    public void close() throws IOException {
        try {
            TreeCursor c = mCursor;
            if (c != null) {
                mCursor = null;
                mDatabase.deleteIndex(c.mTree).run();
            } else if (mSupplier != null) {
                mSupplier.close();
                mSupplier = null;
            }
        } catch (ClosedIndexException e) {
            // Ignore.
        } catch (IOException e) {
            if (!mDatabase.isClosed()) {
                throw e;
            }
        }
    }

    void ready(Tree tree) throws IOException {
        TreeCursor c = new TreeCursor(tree, Transaction.BOGUS);
        initPosition(c);
        mCursor = c;
    }

    protected void initPosition(TreeCursor c) throws IOException {
        c.first();
    }

    interface Supplier {
        Tree get() throws IOException;

        void close() throws IOException;
    }

    void notReady(Supplier supplier) {
        mSupplier = supplier;
    }

    private TreeCursor cursor() {
        TreeCursor c = mCursor;
        return c == null ? openCursor() : c;
    }

    private TreeCursor openCursor() {
        try {
            Tree tree;
            if (mSupplier == null) {
                tree = mDatabase.newTemporaryIndex();
            } else {
                tree = mSupplier.get();
                mSupplier = null;
            }
            ready(tree);
            return mCursor;
        } catch (IOException e) {
            throw Utils.rethrow(e);
        }
    }
}
