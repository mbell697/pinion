package org.pinion.core.asset;

import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.Pinion;
import org.pinion.core.compressor.Compressor;
import org.pinion.core.engine.Engine;
import org.pinion.core.files.FileResolver;
import org.pinion.core.preprocessor.Preprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Asset {
  public String fsPath;
  public String logicalPath;
  public MediaType contentType;
  public Long modifiedTime;
  public HashCode digest;

  public final List<String> dependencyPaths = new ArrayList<>();
  public final List<Asset> requiredAssets = new ArrayList<>();
  List<Engine> engines = new ArrayList<>();
  Optional<Preprocessor> preprocessor = Optional.absent();
  Optional<Compressor> compressor = Optional.absent();

  public Asset(String path, Pinion env) throws FileSystemException {
    fsPath = env.fsPath;
    logicalPath = path;
    contentType = env.findServedMediaType(path);

    FileObject f = FileResolver.find(fsPath,logicalPath);
    try {
      digest = env.fileDigest(f);
      modifiedTime = f.getContent().getLastModifiedTime();
    } finally {
      f.close();
    }
  }

  protected String rawContents() throws IOException {
    FileObject file = FileResolver.find(fsPath,logicalPath);
    String contents = CharStreams.toString(new InputStreamReader(file.getContent().getInputStream()));
    file.close();
    return contents;
  }

  public InputStream source(Pinion env) throws IOException {
    throw new UnsupportedOperationException("Must be implemented in subclass");
  }

  @Override
  public boolean equals(Object obj) {
    return this.getClass() == obj.getClass() &&
           this.logicalPath.equals(((Asset) obj).logicalPath) &&
           this.modifiedTime.equals(((Asset) obj).modifiedTime) &&
           this.digest.equals(((Asset) obj).digest);
  }

  public List<String> getDependencyPaths() {
    return dependencyPaths;
  }
}
