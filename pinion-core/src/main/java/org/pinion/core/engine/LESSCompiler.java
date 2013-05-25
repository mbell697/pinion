package org.pinion.core.engine;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.pinion.core.asset.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class LESSCompiler implements Engine {
  private final Logger LOG = LoggerFactory.getLogger(LESSCompiler.class);
  LessCompiler lessCompiler = new LessCompiler();

  public LESSCompiler() {

  }

  @Override
  public String run(String input, Asset asset) {
    try {
      URL res = getClass().getResource("/assets/" + asset.logicalPath); //TODO need to get to the path some other way without hardcoding assets here
      File f = new File(res.toURI());
      Long start = System.currentTimeMillis();
      String result = lessCompiler.compile(f);
      Long time = System.currentTimeMillis() - start;
      LOG.info("[LESS] " + asset.logicalPath + " " + time );
      return result;

    } catch (LessException | IOException | URISyntaxException e) {
      throw new IllegalStateException("Less Compilation Failed: " + e.getMessage());
    }
  }
}
