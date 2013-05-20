package org.pinion.core;

import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.asset.Asset;
import org.pinion.core.asset.ProcessedAsset;
import org.pinion.core.asset.StaticAsset;
import org.pinion.core.cache.Cache;
import org.pinion.core.cache.MemoryCache;
import org.pinion.core.compressor.ClosureCompressor;
import org.pinion.core.compressor.Compressor;
import org.pinion.core.compressor.CssminCompressor;
import org.pinion.core.compressor.UglifyCompressor;
import org.pinion.core.engine.CoffeeScriptCompiler;
import org.pinion.core.engine.Engine;
import org.pinion.core.engine.JadeCompiler;
import org.pinion.core.engine.LESSCompiler;
import org.pinion.core.files.FileResolver;
import org.pinion.core.preprocessor.DirectiveProcessor;
import org.pinion.core.preprocessor.Preprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class Pinion {

  public String basePath;
  public String version;
  public String fsPath;
  public String appVersion;

  private static Pinion pinion; //singleton, not sure I like that but works for now

  final Map<String, Engine> engines = new HashMap<>();
  final Map<MediaType, Preprocessor> preprocessors = new HashMap<>();
  final Map<MediaType, Compressor> compressors = new HashMap<>();
  final Map<String, MediaType> mimeFileExtensions = new HashMap<>();

  final Set<Asset> assets = new HashSet<>();

  Cache cache;
  HashFunction digestFunction = Hashing.murmur3_128();  //TODO allow configuration

  public static Pinion get() {
    if (pinion == null) {
      throw new UnsupportedOperationException("Pinion must be initialized before use");
    }
    return pinion;
  }

  public static Pinion init( Configuration config ) {
    pinion = new Pinion( config );
    return pinion;
  }

  public static Pinion init() {
    pinion = new Pinion(new Configuration());
    return pinion;
  }

  private Pinion( Configuration config) {

    appVersion = config.getAppVersion();
    version = "0.0.1";  //TODO should pull from maven somehow

    //MimeTypes
    registerMimeExtension(".css", MediaType.CSS_UTF_8);
    registerMimeExtension(".js", MediaType.JAVASCRIPT_UTF_8);

    //Preprocessors
    registerPreprocessor(MediaType.CSS_UTF_8, new DirectiveProcessor());
    registerPreprocessor(MediaType.JAVASCRIPT_UTF_8, new DirectiveProcessor());

    //Compressors
    if (config.getCssCompress())
      registerCompressor(MediaType.CSS_UTF_8, new CssminCompressor(config.getCssmin()));

    if (config.getJsCompress()) {
      switch (config.getJsCompressor()) {
        case "uglifier":
          registerCompressor(MediaType.JAVASCRIPT_UTF_8, new UglifyCompressor(config.getUglifier()));
          break;
        case "closure":
          registerCompressor(MediaType.JAVASCRIPT_UTF_8, new ClosureCompressor(config.getClosure()));
          break;
        default:
          registerCompressor(MediaType.JAVASCRIPT_UTF_8, new UglifyCompressor(config.getUglifier()));
          break;
      }
    }

    //Engines
    registerEngine(".coffee", new CoffeeScriptCompiler(config.getCoffee()));
    registerEngine(".less", new LESSCompiler(config.getLess()));
    registerEngine(".jade", new JadeCompiler());

    //paths
    basePath = config.getAssetPath();

    //Shenanigans to deal with figuring out if we're running inside a jar file or from a real file system
    //if we're running from a file system this will have 'file:' at the beginning
    //if we're running inside a jar this will have 'jar:' at the beginning
    fsPath = this.getClass().getClassLoader().getResource(".").toString();

    //append the internal path separator that commons VFS expects if inside a jar
    if (fsPath.startsWith("jar:"))
      fsPath = fsPath + "!/";
    fsPath = fsPath + basePath;

    cache = new MemoryCache();  //TODO config

    //load config file
  }

  public Asset findAsset(String path) throws IOException {
    checkNotNull(path);
    Optional<Asset> asset = lookupAssetFromIndex(path);
    if (asset.isPresent())
      return asset.get();
    else
      return buildAsset(path);
  }

  //See if a file is stale, TODO should be moved to the asset class
  public boolean fresh(Asset a) throws FileSystemException {
    checkNotNull(a);
    FileObject f = FileResolver.find(fsPath, a.logicalPath);
    try {
      boolean fresh;
      fresh = f.exists() &&
              (a.modifiedTime >= f.getContent().getLastModifiedTime() ||
              (a.digest.equals(fileDigest(f))));
      //if this file isn't fresh, we're definitely not fresh
      if (!fresh)
        return false;

      //Also check dependencies for an indicate that we need to rebuild
      //TODO cache this somehow instead of all the md5 calculations?
      for (Asset dependency : a.requiredAssets) {
        if (!fresh(dependency))
          return false;
      }

      //all set
      return true;
    } finally {
      f.close();
    }
  }

  //create a hash of the file contents, also include the env version and pinion version
  //so that upping either one will cause assets to be rebuilt
  public HashCode fileDigest(FileObject file) {
    checkNotNull(file);
    try (InputStream is = file.getContent().getInputStream()) {
      return digestFunction.newHasher()
          .putBytes(ByteStreams.toByteArray(is))
          .putString(version)
          .putString(appVersion)
          .hash();
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not open resource " + file.getName());
    }
  }

  public MediaType findServedMediaType(String path) {
    checkNotNull(path);
    Optional<String> ext = formatExtension(path);

    if (!ext.isPresent())
      return MediaType.OCTET_STREAM;
    else
      return mimeFileExtensions.get(ext.get());
  }

  public Optional<Preprocessor> preprocessorFor(MediaType type) {
    return Optional.fromNullable(preprocessors.get(type));
  }

  public Optional<Engine> engineFor(String ext) {
    return Optional.fromNullable(engines.get(ext));
  }

  public Optional<Compressor> compressorFor(MediaType type) {
    return Optional.fromNullable(compressors.get(type));
  }

  private Optional<Asset> lookupAssetFromIndex(String path) throws FileSystemException {
    for (Asset asset : assets) {  //will be slow if a lot of assets, but not worth messing with atm.
      if (asset.logicalPath.equals(path))
        return Optional.of(asset);
    }
    return Optional.absent();
  }

  public Asset buildAsset(String path) throws IOException {
    Optional<String> formatExt = formatExtension(path);
    if (formatExt.isPresent()) {
      return new ProcessedAsset(path, this);
    } else {
      return new StaticAsset(path, this);
    }
  }

  private void registerEngine(String ext, Engine engine) {
    checkNotNull(ext);
    checkNotNull(engine);
    engines.put(ext, engine);

  }

  private void registerCompressor(MediaType type, Compressor compressor) {
    checkNotNull(type);
    checkNotNull(compressor);
    compressors.put(type, compressor);
  }

  private void registerPreprocessor(MediaType type, Preprocessor preprocessor) {
    checkNotNull(type);
    checkNotNull(preprocessor);
    preprocessors.put(type, preprocessor);

  }

  private void registerMimeExtension(String ext, MediaType type) {
    checkNotNull(ext);
    checkNotNull(type);
    mimeFileExtensions.put(ext, type);
  }

  //returned the first file extension encountered that matches a registered media type.
  public Optional<String> formatExtension(String path) {
    checkNotNull(path);
    for (String ext: FileResolver.extensions(path)) {
        MediaType type = mimeFileExtensions.get(ext);
        if (type!=null)
          return Optional.of(ext);
      }
      return Optional.absent();

  }

  //return file extensions that match a registered engine in the order they should be processed
  public String[] engineExtensions(String path) {
    checkNotNull(path);

    List<String> foundExts = new ArrayList<>();
    for (String ext: FileResolver.extensions(path)) {
      Engine engine = engines.get(ext);
      if (engine!=null)
        foundExts.add(ext);
    }
    Collections.reverse(foundExts);
    return foundExts.toArray(new String[foundExts.size()]);
  }



}
