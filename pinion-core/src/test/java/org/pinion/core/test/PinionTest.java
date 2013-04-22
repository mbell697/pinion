package org.pinion.core.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.PrintStream;

public class PinionTest {

  public String fixturePath() {
    return this.getClass().getClassLoader().getResource(".").toString();
  }

  public void print(FileObject file, String text) throws FileSystemException {
    try (PrintStream writer = new PrintStream(file.getContent().getOutputStream())) {
      writer.print(text);
    }
  }

}
