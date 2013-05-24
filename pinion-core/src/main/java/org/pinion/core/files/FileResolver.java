package org.pinion.core.files;

import org.apache.commons.vfs2.*;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileResolver {
  private static final char UNIX_SEPARATOR = '/';
  private static final char WINDOWS_SEPARATOR = '\\';
  private static final String EXT_SEPARATOR = ".";

  public static FileObject find(final String fsPath, final String logicalPath) throws FileSystemException {
    checkNotNull(fsPath);
    checkNotNull(logicalPath);
    return VFS.getManager().resolveFile(fsPath + logicalPath);
  }

  public static String fileName(final String path) {
    checkNotNull(path);
    int slashIndex = indexOfLastSeparator(path);
    return (slashIndex == -1) ? path : path.substring(slashIndex + 1);
  }

  private static int indexOfLastSeparator(final String filename) {
    checkNotNull(filename);
    int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
  }

  public static List<FileObject> resolveGlob(final String fsPath, final String glob) throws FileSystemException{
    final FileObject base = VFS.getManager().resolveFile(fsPath);
    final String regex = Globs.toUnixRegexPattern(glob);
    final FileSelector ff = new RegExFileSelector(regex);
    return Arrays.asList(base.findFiles(ff));
  }

  public static String[] extensions(String path) {
    checkNotNull(path);
    final String [] tokens = path.split("\\.");
    if (tokens.length <= 1)
      return new String[0];
    else {
      String [] exts = Arrays.copyOfRange(tokens, 1, tokens.length);
      for (int i = 0;i<exts.length;i++) { exts[i] = EXT_SEPARATOR + exts[i]; }  //normalize
      return exts;
    }

  }

  public static String relativePath(String fsPath, FileObject file) throws FileSystemException {
    FileObject root = VFS.getManager().resolveFile(fsPath);
    return root.getName().getRelativeName(file.getName());
  }


}
