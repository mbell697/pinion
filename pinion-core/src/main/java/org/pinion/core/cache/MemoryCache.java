package org.pinion.core.cache;


import com.google.common.io.CharStreams;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.InputStreamReader;

public class MemoryCache extends Cache {

  public MemoryCache() {
  }

  @Override
  protected void put(final String key, final String value) {
    FileObject f = null;
    try {
      FileSystemManager fs = VFS.getManager();
      f = fs.resolveFile("ram://" + key);
      if (f.exists()) {
        f.delete();
      }
      f.createFile();
      f.getContent().getOutputStream().write(value.getBytes());
    } catch (IOException e) {
      throw new Error(e);
    } finally {
      if (f != null) {
        try {
          f.close();
        } catch (FileSystemException e) {
          //pass
        }
      }
    }
  }

  @Override
  protected String get(final String key) {
    FileObject f = null;
    try {
      FileSystemManager fs = VFS.getManager();
      f = fs.resolveFile("ram://" + key);
      if (!f.exists()) {
        return null;
      }
      return CharStreams.toString(new InputStreamReader(f.getContent().getInputStream()));
    } catch (IOException e) {
      throw new Error(e);
    } finally {
      if (f != null) {
        try {
          f.close();
        } catch (FileSystemException e) {
          //pass
        }
      }
    }
  }
}
