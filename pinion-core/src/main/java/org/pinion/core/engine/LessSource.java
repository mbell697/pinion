package org.pinion.core.engine;

import com.google.common.io.CharStreams;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.files.FileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.MULTILINE;

public class LessSource {

  private final Logger LOG = LoggerFactory.getLogger(LessSource.class);

  private static final Pattern IMPORT_PATTERN = Pattern.compile("^(?!\\s*//\\s*)@import\\s+(url\\()?\\s*(\"|')(.+)\\s*(\"|')(\\))?\\s*;.*$", MULTILINE);

  private String path;
  private String normalizedContent;

  public LessSource(String path, String input) throws IOException {
    checkNotNull(path);
    this.path = path;
    this.normalizedContent = input;
    resolveImports();
  }

  public String getNormalizedContent() {
    return normalizedContent;
  }

  //This is a quality fucking mess, close your eyes
  private void resolveImports() {
    FileObject file = null;
    try {
      file = FileResolver.find("", path);
    } catch (FileSystemException e) {
      LOG.error("Failed to resolve file from " + path);
      throw new Error(e);
    }
    Matcher importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
    while (importMatcher.find()) {
      String importedFileName = importMatcher.group(3);
      importedFileName = importedFileName.matches(".*\\.(le?|c)ss$") ? importedFileName : importedFileName + ".less";
      boolean css = importedFileName.matches(".*css$");
      if (!css) {
        try {
          FileObject importedFile = file.getParent().resolveFile(importedFileName);
          String importedSource = CharStreams.toString(new InputStreamReader(importedFile.getContent().getInputStream()));
          importedFile.close();
          LessSource importedLessSource = new LessSource(importedFile.getName().toString(), importedSource);
          normalizedContent = normalizedContent.substring(0, importMatcher.start()) + importedLessSource.getNormalizedContent() + normalizedContent.substring(importMatcher.end());
          importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
        } catch (FileSystemException e) {
          LOG.error("Failed to resolve file from " + importedFileName + " from " + path);
          throw new Error(e);
        } catch (IOException e) {
          throw new Error(e);
        }

      }
    }
  }
}
