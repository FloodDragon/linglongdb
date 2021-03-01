package com.linglong.engine.core;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public class SortReverseScanner extends SortScanner {
    public SortReverseScanner(LocalDatabase db) {
        super(db);
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return super.getComparator().reversed();
    }

    @Override
    protected void doStep(TreeCursor c) throws IOException {
        c.deletePrevious();
    }

    @Override
    protected void initPosition(TreeCursor c) throws IOException {
        c.last();
    }
}
