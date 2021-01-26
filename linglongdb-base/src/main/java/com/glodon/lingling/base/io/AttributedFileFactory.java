package com.glodon.lingling.base.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * @author Stereo
 */
public class AttributedFileFactory implements FileFactory {
    private final FileAttribute<?>[] mFileAttrs;
    private final FileAttribute<?>[] mDirAttrs;

    public AttributedFileFactory(String posixFilePerms, String posixDirPerms) {
        mFileAttrs = toAttrs(posixFilePerms);
        mDirAttrs = toAttrs(posixDirPerms);
    }

    public AttributedFileFactory(FileAttribute<?>[] fileAttrs, FileAttribute<?>[] dirAttrs) {
        mFileAttrs = copy(fileAttrs);
        mDirAttrs = copy(dirAttrs);
    }

    @Override
    public boolean createFile(File file) throws IOException {
        if (mFileAttrs == null) {
            return file.createNewFile();
        }

        try {
            Files.createFile(file.toPath(), mFileAttrs);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public boolean createDirectory(File dir) throws IOException {
        if (mDirAttrs == null) {
            return dir.mkdir();
        }

        try {
            Files.createDirectory(dir.toPath(), mDirAttrs);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public boolean createDirectories(File dir) throws IOException {
        if (mDirAttrs == null) {
            return dir.mkdirs();
        }

        try {
            Files.createDirectories(dir.toPath(), mDirAttrs);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static FileAttribute<Set<PosixFilePermission>>[] toAttrs(String posixPerms) {
        if (posixPerms == null) {
            return null;
        }

        return new FileAttribute[] {
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(posixPerms))
        };
    }

    private static FileAttribute<?>[] copy(FileAttribute<?>[] attrs) {
        return (attrs == null || attrs.length == 0) ? null : attrs.clone();
    }
}
