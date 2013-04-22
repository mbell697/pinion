package org.pinion.core.test;

import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.pinion.core.Pinion;
import org.pinion.core.files.FileResolver;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(JUnit4.class)
public class FileNameParsingTest extends PinionTest{

  private Pinion pinion = Pinion.init();

  @Before
  public void init() {

  }

  @Test
  public void extensionsTest() {
    assertThat(FileResolver.extensions("empty"), is(new String[0]));
    assertThat(FileResolver.extensions("application.css"), is(new String[]{".css"}));
    assertThat(FileResolver.extensions("test.min.js.coffee"), is(new String[]{".min", ".js", ".coffee"}));
  }

  @Test
  public void formatExtensionTest() {
    Optional<String> absent = Optional.absent();

    assertThat(pinion.formatExtension("empty"), is(absent));
    assertThat(pinion.formatExtension("application.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("application.js.coffee"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("application.js.coffee.erb"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("application.css"), is(Optional.of(".css")));
    assertThat(pinion.formatExtension("empty.foo"), is(absent));
    assertThat(pinion.formatExtension("empty.erb"), is(absent));
    assertThat(pinion.formatExtension("jquery.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("jquery.min.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("jquery.tmpl.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("jquery.tmpl.min.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("jquery.csv.js"), is(Optional.of(".js")));
    assertThat(pinion.formatExtension("jquery.csv.min.js"), is(Optional.of(".js")));
  }

  @Test
  public void engineExtensions() {
    assertThat(pinion.engineExtensions("empty"), is(new String[0]));
    assertThat(pinion.engineExtensions("application.js"), is(new String[0]));
    assertThat(pinion.engineExtensions("application.js.coffee"), is(new String[]{".coffee"}));
    assertThat(pinion.engineExtensions("application.js.coffee.less"), is(new String[]{".less",".coffee"}));
    assertThat(pinion.engineExtensions("application.css.less"), is(new String[]{".less"}));
    assertThat(pinion.engineExtensions("application.less"), is(new String[]{".less"}));
    assertThat(pinion.engineExtensions("jquery.js"), is(new String[0]));
    assertThat(pinion.engineExtensions("jquery.min.js"), is(new String[0]));
    assertThat(pinion.engineExtensions("jquery.tmpl.min.js"), is(new String[0]));
  }

  @Test
  public void contentTypeTest() {
    assertThat(pinion.findServedMediaType("empty"), is(MediaType.OCTET_STREAM));
    assertThat(pinion.findServedMediaType("application.js"), is(MediaType.JAVASCRIPT_UTF_8));
    assertThat(pinion.findServedMediaType("application.js.coffee"), is(MediaType.JAVASCRIPT_UTF_8));
    assertThat(pinion.findServedMediaType("application.js.coffee.erb"), is(MediaType.JAVASCRIPT_UTF_8));
    assertThat(pinion.findServedMediaType("gallery.css.less"), is(MediaType.CSS_UTF_8));
    assertThat(pinion.findServedMediaType("jquery.tmpl.min.js"), is(MediaType.JAVASCRIPT_UTF_8));
  }

  @Test
  public void globResolutionTest() throws FileSystemException {
    //create a file structure to play with
    FileResolver.find(globTestRoot(),"test.js").createFile();
    FileResolver.find(globTestRoot(),"test.min.js").createFile();
    FileResolver.find(globTestRoot(),"test.js.coffee").createFile();
    FileResolver.find(globTestRoot(),"test.css").createFile();
    FileResolver.find(globTestRoot(),"test.min.css").createFile();
    FileResolver.find(globTestRoot(),"test.css.less").createFile();

    FileResolver.find(globTestRoot(),"level1/test.js").createFile();
    FileResolver.find(globTestRoot(),"level1/test.min.js").createFile();
    FileResolver.find(globTestRoot(),"level1/test.js.coffee").createFile();
    FileResolver.find(globTestRoot(),"level1/test.css").createFile();
    FileResolver.find(globTestRoot(),"level1/test.min.css").createFile();
    FileResolver.find(globTestRoot(),"level1/test.css.less").createFile();

    FileResolver.find(globTestRoot(),"level1/level2/test.js").createFile();
    FileResolver.find(globTestRoot(),"level1/level2/test.min.js").createFile();
    FileResolver.find(globTestRoot(),"level1/level2/test.js.coffee").createFile();
    FileResolver.find(globTestRoot(),"level1/level2/test.css").createFile();
    FileResolver.find(globTestRoot(),"level1/level2/test.min.css").createFile();
    FileResolver.find(globTestRoot(),"level1/level2/test.css.less").createFile();

    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.js*"), hasSize(3));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.js"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "**.js"), hasSize(6));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/*.js"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/**.js"), hasSize(4));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.js"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.js*"), hasSize(3));

    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.css*"), hasSize(3));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.css"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "**.css"), hasSize(6));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/*.css"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/**.css"), hasSize(4));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.css"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.css*"), hasSize(3));

    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.coffee*"), hasSize(1));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "*.coffee"), hasSize(1));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "**.coffee"), hasSize(3));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/*.coffee"), hasSize(1));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/**.coffee"), hasSize(2));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.coffee"), hasSize(1));
    assertThat(FileResolver.resolveGlob(globTestRoot(), "level1/level2/*.coffee*"), hasSize(1));

    //clean up our mess
    FileObject globTestDirectory = FileResolver.find(globTestRoot(),"");
    globTestDirectory.delete(new AllFileSelector());
  }

  private String globTestRoot() {
    return fixturePath() + "glob_test/";
  }
}
