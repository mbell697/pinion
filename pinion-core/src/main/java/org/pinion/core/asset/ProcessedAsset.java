package org.pinion.core.asset;

import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.Pinion;
import org.pinion.core.engine.Engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessedAsset extends Asset {
  public ProcessedAsset(final String path, final Pinion env) throws IOException {
    super(path, env);
    compressor = env.compressorFor(contentType);
    final String[] engineExts = env.engineExtensions(path);
    for (String ext : engineExts) {
      Optional<Engine> engine = env.engineFor(ext);
      if (engine.isPresent())
        engines.add(engine.get());
    }
    preprocessor = env.preprocessorFor(contentType);

    //Build dependency tree here so we can track changes to dependencies before source is ever called.
    //TODO bit of a hack??
    if (preprocessor.isPresent()) preprocessor.get().run(rawContents(), this);
    updateDependentAssets(env);
  }

  @Override
  public InputStream source(final Pinion env) throws IOException {

    //read in this asset's file
    String fContent = rawContents();

    //run preprocessor
    if (preprocessor.isPresent())
      fContent = preprocessor.get().run(fContent, this);

    updateDependentAssets(env);

    //run engines
    for (Engine e : engines) {
      fContent = e.run(fContent, this);
    }

    //run compressor
    if (compressor.isPresent())
      fContent = compressor.get().run(fContent, this);

    //merge dependencies
    //TODO dependencies should be checked for freshness and their results cached
    StringBuilder b = new StringBuilder();
    for (Asset asset : requiredAssets) {
      try (InputStream data = asset.source(env)) { b.append(CharStreams.toString(new InputStreamReader(data))); }
    }
    b.append(fContent);
    fContent = b.toString();

    return new ByteArrayInputStream(fContent.getBytes("UTF-8"));
  }

  //TODO wasteful to completely rebuild??
  private void updateDependentAssets(Pinion env) throws IOException {
    requiredAssets.clear();
    for (String path: dependencyPaths) {
      requiredAssets.add(env.buildAsset(path));
    }
  }
}
