package com.linglong.engine.core.temp;

import com.linglong.base.common.CauseCloseable;
import com.linglong.base.common.Utils;
import com.linglong.base.io.FileFactory;
import com.linglong.base.common.ShutdownHook;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stereo
 */
public final class TempFileManager implements CauseCloseable, ShutdownHook {
    private File mBaseFile;
    private final FileFactory mFileFactory;
    private long mCount;
    private Map<File, Closeable> mFiles;

    private Throwable mCause;

    public TempFileManager(File baseFile, FileFactory factory) throws IOException {
        mBaseFile = baseFile;
        mFileFactory = factory;

        Utils.deleteNumberedFiles(baseFile, ".temp.");
    }

    public File createTempFile() throws IOException {
        while (true) {
            File file;
            synchronized (this) {
                if (mBaseFile == null) {
                    throw new IOException("Shutting down", mCause);
                }
                file = new File(mBaseFile.getPath() + ".temp." + (mCount++));
                if (mFiles == null) {
                    mFiles = new HashMap<>(4);
                }
                if (mFiles.containsKey(file)) {
                    continue;
                }
                mFiles.put(file, null);
            }

            if (mFileFactory == null) {
                if (file.createNewFile()) {
                    return file;
                }
            } else if (mFileFactory.createFile(file)) {
                return file;
            }

            synchronized (this) {
                mFiles.remove(file);
            }
        }
    }

    public synchronized void register(File file, Closeable c) throws IOException {
        if (mFiles == null || !mFiles.containsKey(file)) {
            if (mBaseFile == null) {
                throw new IOException("Shutting down", mCause);
            }
            return;
        }
        if (mFiles.get(file) != null) {
            throw new IllegalStateException();
        }
        mFiles.put(file, c);
    }

    public void deleteTempFile(File file) {
        Closeable c;
        synchronized (this) {
            if (mFiles == null || !mFiles.containsKey(file)) {
                return;
            }
            c = mFiles.remove(file);
        }
        Utils.closeQuietly(c);
        file.delete();
    }

    @Override
    public void close() {
        close(null);
    }

    @Override
    public void close(Throwable cause) {
        Map<File, Closeable> files;
        synchronized (this) {
            mBaseFile = null;
            if (cause != null) {
                mCause = cause;
            }
            if (mFiles == null) {
                files = null;
            } else {
                files = new HashMap<>(mFiles);
                mFiles = null;
            }
        }

        if (files != null) {
            for (Closeable c : files.values()) {
                Utils.closeQuietly(c, cause);
            }
            for (File f : files.keySet()) {
                f.delete();
            }
        }
    }

    @Override
    public void shutdown() {
        close();
    }
}
