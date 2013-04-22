package org.pinion.core.preprocessor;

import org.apache.commons.vfs2.FileSystemException;
import org.pinion.core.asset.Asset;

/**
 * Created with IntelliJ IDEA.
 * User: mbell697
 * Date: 3/28/13
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Preprocessor {
  public String run(String input, Asset asset) throws FileSystemException;
}
