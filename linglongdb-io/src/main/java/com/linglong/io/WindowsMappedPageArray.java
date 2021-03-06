package com.linglong.io;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;

/**
 * windows系统文件内存映射
 *
 * @author Stereo
 */
class WindowsMappedPageArray extends MappedPageArray {
    private static final Kernel32Ex cKernel;

    static {
        cKernel = (Kernel32Ex) Native.loadLibrary
                ("kernel32", Kernel32Ex.class, W32APIOptions.UNICODE_OPTIONS);
    }

    private final File mFile;
    private final EnumSet<OpenOption> mOptions;

    private final WinNT.HANDLE mFileHandle;
    private final WinNT.HANDLE mMappingHandle;

    private final boolean mNonDurable;

    private volatile boolean mEmpty;

    WindowsMappedPageArray(int pageSize, long pageCount,
                           File file, EnumSet<OpenOption> options)
            throws IOException {
        super(pageSize, pageCount, options);

        mFile = file;
        mOptions = options;

        if (file == null) {
            long mappingPtr = cKernel.VirtualAlloc
                    (0, // lpAddress
                            pageSize * pageCount,
                            0x1000 | 0x2000, // MEM_COMMIT | MEM_RESERVE
                            0x04); // PAGE_READWRITE

            if (mappingPtr == 0) {
                int error = cKernel.GetLastError();
                throw new IOException(Kernel32Util.formatMessage(error));
            }

            setMappingPtr(mappingPtr);

            mFileHandle = null;
            mMappingHandle = null;
            mNonDurable = true;
            mEmpty = true;

            return;
        }

        mEmpty = file.length() == 0;
        mNonDurable = options.contains(OpenOption.NON_DURABLE);

        int access = WinNT.GENERIC_READ;

        if (!options.contains(OpenOption.READ_ONLY)) {
            access |= WinNT.GENERIC_WRITE;
        }

        int create = options.contains(OpenOption.CREATE) ? WinNT.OPEN_ALWAYS : WinNT.OPEN_EXISTING;

        int flags;
        if (mNonDurable) {
            flags = WinNT.FILE_ATTRIBUTE_TEMPORARY;
        } else {
            flags = WinNT.FILE_ATTRIBUTE_NORMAL;
        }

        if (options.contains(OpenOption.RANDOM_ACCESS)) {
            flags |= WinNT.FILE_FLAG_RANDOM_ACCESS;
        }

        WinNT.HANDLE hFile = cKernel.CreateFile
                (file.getPath(),
                        access,
                        0, // no sharing
                        null, // security attributes
                        create,
                        flags,
                        null // template file
                );

        if (hFile == null || hFile == WinNT.INVALID_HANDLE_VALUE) {
            int error = cKernel.GetLastError();
            throw new FileNotFoundException(Kernel32Util.formatMessage(error));
        }

        long mappingSize = pageSize * pageCount;

        WinNT.HANDLE hMapping = cKernel.CreateFileMapping
                (hFile,
                        null, // security attributes
                        WinNT.PAGE_READWRITE,
                        (int) (mappingSize >>> 32),
                        (int) mappingSize,
                        null // no name
                );

        if (hMapping == null || hMapping == WinNT.INVALID_HANDLE_VALUE) {
            int error = cKernel.GetLastError();
            closeHandle(hFile);
            throw toException(error);
        }

        access = options.contains(OpenOption.READ_ONLY)
                ? WinNT.SECTION_MAP_READ : WinNT.SECTION_MAP_WRITE;

        Pointer ptr = cKernel.MapViewOfFile
                (hMapping,
                        access,
                        0, // offset high
                        0, // offset low
                        mappingSize
                );

        if (ptr == null) {
            int error = cKernel.GetLastError();
            closeHandle(hMapping);
            closeHandle(hFile);
            throw toException(error);
        }

        mFileHandle = hFile;
        mMappingHandle = hMapping;

        setMappingPtr(Pointer.nativeValue(ptr));
    }

    @Override
    public long getPageCount() {
        return mEmpty ? 0 : super.getPageCount();
    }

    @Override
    public void setPageCount(long count) {
        mEmpty = count == 0;
    }

    @Override
    MappedPageArray doOpen() throws IOException {
        boolean empty = mEmpty;
        WindowsMappedPageArray pa = new WindowsMappedPageArray
                (pageSize(), super.getPageCount(), mFile, mOptions);
        pa.mEmpty = empty;
        return pa;
    }

    void doSync(long mappingPtr, boolean metadata) throws IOException {
        if (!mNonDurable) {
            if (!cKernel.FlushViewOfFile(mappingPtr, super.getPageCount() * pageSize())) {
                throw toException(cKernel.GetLastError());
            }
            fsync();
        }
        mEmpty = false;
    }

    void doSyncPage(long mappingPtr, long index) throws IOException {
        if (!mNonDurable) {
            int pageSize = pageSize();
            if (!cKernel.FlushViewOfFile(mappingPtr + index * pageSize, pageSize)) {
                throw toException(cKernel.GetLastError());
            }
            fsync();
        }
        mEmpty = false;
    }

    void doClose(long mappingPtr) throws IOException {
        if (mFileHandle == null) {
            if (!cKernel.VirtualFree(mappingPtr, 0, 0x8000)) { // MEM_RELEASE
                int error = cKernel.GetLastError();
                throw new IOException(Kernel32Util.formatMessage(error));
            }
        } else {
            cKernel.UnmapViewOfFile(new Pointer(mappingPtr));
            closeHandle(mMappingHandle);
            closeHandle(mFileHandle);
        }
    }

    private static IOException toException(int error) {
        return new IOException(Kernel32Util.formatMessage(error));
    }

    private static void closeHandle(WinNT.HANDLE handle) throws IOException {
        cKernel.CloseHandle(handle);
    }

    private void fsync() throws IOException {
        if (mFileHandle != null) {
            // 注意: Win32没有刷新元数据标志。
            if (!cKernel.FlushFileBuffers(mFileHandle)) {
                throw toException(cKernel.GetLastError());
            }
        }
    }

    public static interface Kernel32Ex extends Kernel32 {
        //内核的方法仅支持32位映射大小。
        Pointer MapViewOfFile(HANDLE hFileMappingObject,
                              int dwDesiredAccess,
                              int dwFileOffsetHigh,
                              int dwFileOffsetLow,
                              long dwNumberOfBytesToMap);

        boolean FlushViewOfFile(long baseAddress, long numberOfBytesToFlush);

        long VirtualAlloc(long lpAddress, long dwSize, int flAllocationType, int flProtect);

        boolean VirtualFree(long lpAddress, long dwSize, int dwFreeType);
    }
}
