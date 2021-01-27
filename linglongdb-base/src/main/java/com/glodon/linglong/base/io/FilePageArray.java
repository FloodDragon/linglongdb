package com.glodon.linglong.base.io;

import com.glodon.linglong.base.common.Utils;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

/**
 * 文件访问基本实现
 *
 * @author Stereo
 */
public class FilePageArray extends PageArray {
    final FileIO mFio;

    public FilePageArray(int pageSize, File file, EnumSet<OpenOption> options) throws IOException {
        this(pageSize, file, null, options);
    }

    public FilePageArray(int pageSize, File file, FileFactory factory,
                         EnumSet<OpenOption> options)
            throws IOException {
        super(pageSize);

        if (factory != null
                && options.contains(OpenOption.CREATE)
                && !options.contains(OpenOption.NON_DURABLE)
                && !options.contains(OpenOption.READ_ONLY)) {
            factory.createFile(file);
        }

        mFio = FileIO.open(file, options);
    }

    @Override
    public boolean isDirectIO() {
        return mFio.isDirectIO();
    }

    @Override
    public boolean isReadOnly() {
        return mFio.isReadOnly();
    }

    @Override
    public boolean isEmpty() throws IOException {
        return mFio.length() == 0;
    }

    @Override
    public long getPageCount() throws IOException {
        return mFio.length() / mPageSize;
    }

    @Override
    public void setPageCount(long count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException(String.valueOf(count));
        }
        if (isReadOnly()) {
            return;
        }
        mFio.setLength(count * mPageSize);
    }

    @Override
    public void readPage(long index, byte[] dst, int offset, int length) throws IOException {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        mFio.read(index * mPageSize, dst, offset, length);
    }

    @Override
    public void readPage(long index, long dstPtr, int offset, int length) throws IOException {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        mFio.read(index * mPageSize, dstPtr, offset, length);
    }

    @Override
    public void writePage(long index, byte[] src, int offset) throws IOException {
        int pageSize = mPageSize;
        mFio.write(index * pageSize, src, offset, pageSize);
    }

    @Override
    public void writePage(long index, long srcPtr, int offset) throws IOException {
        int pageSize = mPageSize;
        mFio.write(index * pageSize, srcPtr, offset, pageSize);
    }

    @Override
    public void sync(boolean metadata) throws IOException {
        mFio.sync(metadata);
        mFio.remap();
    }

    @Override
    public void close(Throwable cause) throws IOException {
        Utils.close(mFio, cause);
    }
}
