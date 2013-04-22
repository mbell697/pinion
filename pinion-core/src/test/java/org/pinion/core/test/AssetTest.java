package org.pinion.core.test;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.pinion.core.Pinion;
import org.pinion.core.asset.Asset;
import org.pinion.core.files.FileResolver;

import java.io.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


@RunWith(JUnit4.class)
public class AssetTest extends PinionTest{

  Pinion pinion = Pinion.init();

  @After
  public void init() throws FileSystemException {
    FileObject testAssetDirectory = FileResolver.find(fixturePath(),"assets");
    testAssetDirectory.delete(new AllFileSelector());
  }

  @Test
  public void assetIsStaleWhenContentsChange() throws IOException, InterruptedException {
    FileObject testFile = FileResolver.find(fixturePath(),"assets/test.js");
    testFile.createFile();
    testFile.close();

    Asset asset = pinion.findAsset("test.js");

    assertTrue(pinion.fresh(asset));

    FileObject file = FileResolver.find(fixturePath(),"assets/test.js");
    print(file, "blah;");
    //Hack around the fact that HFS+ only has 1sec resolution on file time stamps
    file.getContent().setLastModifiedTime(file.getContent().getLastModifiedTime() + 2000);
    file.close();

    assertFalse(pinion.fresh(asset));
  }

  @Test
  public void assetIsStaleWhenFileRemoved() throws IOException {
    FileObject testFile = FileResolver.find(fixturePath(),"assets/test.js");
    testFile.createFile();
    testFile.close();

    Asset asset = pinion.findAsset("test.js");

    assertTrue(pinion.fresh(asset));

    FileObject file = FileResolver.find(fixturePath(),"assets/test.js");
    file.delete();

    assertFalse(pinion.fresh(asset));
  }

  @Test
  public void assetIsStaleWhenDependencyIsModified() throws IOException, InterruptedException {
    FileObject mainFile = FileResolver.find(fixturePath(),"assets/main-test.js");
    mainFile.createFile();
    print(mainFile, "//= require dep-test.js\n" );
    mainFile.close();

    FileObject depFile = FileResolver.find(fixturePath(),"assets/dep-test.js");
    depFile.createFile();
    depFile.close();

    Asset asset = pinion.findAsset("main-test.js");

    assertTrue(pinion.fresh(asset));
    FileObject file = FileResolver.find(fixturePath(),"assets/dep-test.js");
    print(file,"blah;");
    //Hack around the fact that HFS+ only has 1sec resolution on file time stamps
    file.getContent().setLastModifiedTime(file.getContent().getLastModifiedTime() + 2000);
    file.close();

    assertFalse(pinion.fresh(asset));
  }

  @Test
  public void assetIsStaleWhenDependencyRemoved() throws IOException {
    FileObject mainFile = FileResolver.find(fixturePath(),"assets/main-test.js");
    mainFile.createFile();
    print(mainFile, "//= require dep-test.js\n" );
    mainFile.close();

    FileObject depFile = FileResolver.find(fixturePath(),"assets/dep-test.js");
    depFile.createFile();
    depFile.close();

    Asset asset = pinion.findAsset("main-test.js");

    assertTrue(pinion.fresh(asset));

    FileObject file = FileResolver.find(fixturePath(),"assets/dep-test.js");
    file.delete();

    assertFalse(pinion.fresh(asset));
  }

  @Test
  @Ignore("Now sure on implementation yet")
  public void assetIsStaleWhenDependencyIsAdded() {

  }

  @Test
  @Ignore("Now sure on implementation yet")
  public void assetIsStaleWhenDependencyIsRemoved() {

  }
}
