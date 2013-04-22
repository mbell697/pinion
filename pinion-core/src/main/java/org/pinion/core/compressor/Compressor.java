package org.pinion.core.compressor;

import org.pinion.core.asset.Asset;

import java.io.IOException;


public interface Compressor {
  public String run(String input, Asset asset) throws IOException;
}
