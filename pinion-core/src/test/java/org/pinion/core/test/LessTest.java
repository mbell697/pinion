package org.pinion.core.test;

import com.google.common.io.CharStreams;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.pinion.core.Pinion;
import org.pinion.core.asset.Asset;
import org.pinion.core.asset.ProcessedAsset;
import org.pinion.core.engine.LESSCompiler;
import org.pinion.core.files.FileResolver;

import java.io.IOException;
import java.io.InputStreamReader;

@RunWith(JUnit4.class)
public class LessTest extends PinionTest{

  Pinion pinion = Pinion.init();

  @Before
  public void before() throws FileSystemException {
    FileObject testSources = FileResolver.find(fixturePath(),"less");
    FileObject testAssetDirectory = FileResolver.find(fixturePath(),"assets");
    testAssetDirectory.copyFrom(testSources, new AllFileSelector());
  }


  @After
  public void after() throws FileSystemException {
    FileObject testAssetDirectory = FileResolver.find(fixturePath(),"assets");
    testAssetDirectory.delete(new AllFileSelector());
  }


  @Test
  public void itCompilesBootstrap() throws IOException {
    FileObject bootstrap = FileResolver.find(fixturePath(), "assets/bootstrap/less/bootstrap.less");
    String src = CharStreams.toString(new InputStreamReader(bootstrap.getContent().getInputStream()));
    Asset asset = new ProcessedAsset("bootstrap/less/bootstrap.less", pinion);
    LESSCompiler compiler = new LESSCompiler();

    compiler.run(src, asset);
  }
}
