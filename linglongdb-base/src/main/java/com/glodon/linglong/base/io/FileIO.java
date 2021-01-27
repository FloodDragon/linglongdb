package com.glodon.linglong.base.io;

import com.glodon.linglong.base.common.CauseCloseable;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

/**
 * 低级I/O接口
 *
 * @author Stereo
 */
public abstract class FileIO implements CauseCloseable {
    private static final String USE_JNA = FileIO.class.getName() + ".useJNA";
    private static final int IO_TYPE; // 0: platform independent, 1: POSIX
    private static final boolean NEEDS_DIR_SYNC;

    static {
        int type = 0;

        String jnaProp = System.getProperty(USE_JNA, null);
        if (jnaProp == null || Boolean.parseBoolean(jnaProp)) {
            try {
                if (Native.SIZE_T_SIZE >= 8 && !Platform.isWindows()) {
                    type = 1;
                }
            } catch (Throwable e) {
                if (jnaProp != null) {
                    throw e;
                }
            }
        }

        IO_TYPE = type;

        NEEDS_DIR_SYNC = !System.getProperty("os.name").startsWith("Windows");
    }

    public static FileIO open(File file, EnumSet<OpenOption> options)
            throws IOException {
        return open(file, options, 32);
    }

    public static FileIO open(File file, EnumSet<OpenOption> options, int openFileCount)
            throws IOException {
        if (options == null) {
            options = EnumSet.noneOf(OpenOption.class);
        }
        if (IO_TYPE == 1 && (!options.contains(OpenOption.MAPPED) || DirectAccess.isSupported())) {
            return new PosixFileIO(file, options);
        }
        return new JavaFileIO(file, options, openFileCount);
    }

    public abstract boolean isDirectIO();

    public abstract boolean isReadOnly();

    public abstract long length() throws IOException;

    public void setLength(long length) throws IOException {
        setLength(length, LengthOption.PREALLOCATE_NEVER);
    }

    public abstract void setLength(long length, LengthOption option) throws IOException;

    /**
     * @param pos    zero-based position in file
     * @param buf    receives read data
     * @param offset offset into data buffer
     * @param length amount of data to read
     * @throws IllegalArgumentException
     */
    public abstract void read(long pos, byte[] buf, int offset, int length) throws IOException;

    /**
     * @param pos zero-based position in file
     * @param bb  receives read data
     * @throws IllegalArgumentException
     */
    public abstract void read(long pos, ByteBuffer bb) throws IOException;

    public void read(long pos, long ptr, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @param pos    zero-based position in file
     * @param buf    data to write
     * @param offset offset into data buffer
     * @param length amount of data
     * @throws IllegalArgumentException
     */
    public abstract void write(long pos, byte[] buf, int offset, int length) throws IOException;

    /**
     * @param pos zero-based position in file
     * @param bb  data to write
     * @throws IllegalArgumentException
     */
    public abstract void write(long pos, ByteBuffer bb) throws IOException;

    public void write(long pos, long ptr, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public abstract void map() throws IOException;

    public abstract void remap() throws IOException;

    public abstract void unmap() throws IOException;

    public abstract void sync(boolean metadata) throws IOException;

    @Override
    public void close() throws IOException {
        close(null);
    }

    public static void dirSync(File file) throws IOException {
        if (!NEEDS_DIR_SYNC) {
            return;
        }

        while (!file.isDirectory()) {
            file = file.getParentFile();
            if (file == null) {
                return;
            }
        }

        if (IO_TYPE == 1) {
            int fd = PosixFileIO.openFd(file, EnumSet.of(OpenOption.READ_ONLY));
            try {
                PosixFileIO.fsyncFd(fd);
            } finally {
                PosixFileIO.closeFd(fd);
            }
        } else {
            FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            try {
                fc.force(true);
            } finally {
                fc.close();
            }
        }
    }
}
