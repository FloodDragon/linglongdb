package com.linglong.replication.real;

import java.io.File;

/**
 * @author Stereo on 2021/3/15.
 */
public class CleanNodoFile {

    public static void clean(File baseFile) {
        String prefix = baseFile.getName();
        baseFile.getParentFile().listFiles(file -> {
            if (file.getName().startsWith(prefix)) {
                file.delete();
            }
            return false;
        });
    }
}
