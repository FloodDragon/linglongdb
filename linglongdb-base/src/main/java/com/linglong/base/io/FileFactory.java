package com.linglong.base.io;

import java.io.File;
import java.io.IOException;

public interface FileFactory {
    boolean createFile(File file) throws IOException;

    boolean createDirectory(File dir) throws IOException;

    boolean createDirectories(File dir) throws IOException;
}
