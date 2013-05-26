package org.pinion.core.engine;

import com.google.common.io.CharStreams;
import org.apache.commons.vfs2.FileObject;
import org.pinion.core.files.FileResolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.MULTILINE;

public class LessSource {

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

  private void resolveImports() throws IOException {
    FileObject file = FileResolver.find("",path);
    Matcher importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
    while (importMatcher.find()) {
      String importedFileName = importMatcher.group(3);
      importedFileName = importedFileName.matches(".*\\.(le?|c)ss$") ? importedFileName : importedFileName + ".less";
      boolean css = importedFileName.matches(".*css$");
      if (!css) {
        FileObject importedFile = file.getParent().resolveFile(importedFileName);
        String importedSource = CharStreams.toString(new InputStreamReader(importedFile.getContent().getInputStream()));
        importedFile.close();
        LessSource importedLessSource = new LessSource(importedFile.getName().getPath(), importedSource);
        normalizedContent = normalizedContent.substring(0, importMatcher.start()) + importedLessSource.getNormalizedContent() + normalizedContent.substring(importMatcher.end());
        importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
      }
    }
  }
}
