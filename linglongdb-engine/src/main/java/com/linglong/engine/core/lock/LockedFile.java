package com.linglong.engine.core.lock;

import com.linglong.base.exception.DatabaseException;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * 锁文件
 *
 * @author Stereo
 */
public final class LockedFile implements Closeable {
    private final RandomAccessFile mRaf;
    private final FileLock mLock;

    public LockedFile(File file, boolean readOnly) throws IOException {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
        }

        RandomAccessFile raf;
        FileLock lock;

        try {
            raf = new RandomAccessFile(file, readOnly ? "r" : "rw");
            lock = raf.getChannel().tryLock(0, Long.MAX_VALUE, readOnly);
            if (lock == null) {
                throw new DatabaseException("Database is open and locked by another process");
            }
        } catch (FileNotFoundException e) {
            if (readOnly) {
                raf = null;
                lock = null;
            } else {
                throw e;
            }
        } catch (OverlappingFileLockException e) {
            throw new DatabaseException("Database is already open by current process");
        }

        mRaf = raf;
        mLock = lock;
    }

    @Override
    public void close() throws IOException {
        if (mLock != null) {
            mLock.close();
        }
        if (mRaf != null) {
            mRaf.close();
        }
    }
}
