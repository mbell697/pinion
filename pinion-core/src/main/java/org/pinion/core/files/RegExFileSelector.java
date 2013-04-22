package org.pinion.core.files;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExFileSelector implements FileSelector {
  private Pattern pattern;

  public RegExFileSelector(final String regex) {
    super();
    this.pattern = Pattern.compile(regex);
  }

  @Override
  public boolean includeFile(final FileSelectInfo fileSelectInfo) throws Exception {
    if (fileSelectInfo.getFile().getType() == FileType.FOLDER)
      return false;
    else {
      String path = fileSelectInfo.getFile().getName().getPath();
      path = path.replace(fileSelectInfo.getBaseFolder().getName().getPath() + "/", "");
      return pattern.matcher(path).matches();
    }
  }

  @Override
  public boolean traverseDescendents(final FileSelectInfo fileSelectInfo) throws Exception {
    return true;
  }
}