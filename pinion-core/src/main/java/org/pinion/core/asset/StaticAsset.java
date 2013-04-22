package org.pinion.core.asset;

import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.Pinion;
import org.pinion.core.files.FileResolver;

import java.io.IOException;
import java.io.InputStream;

//Assets that do not require processing
public class StaticAsset extends Asset {

  public StaticAsset(final String path, final Pinion env) throws FileSystemException {
    super(path, env);
  }

  @Override
  public InputStream source(final Pinion env) throws IOException {
    return FileResolver.find(fsPath, logicalPath).getContent().getInputStream();
  }

}
