package org.pinion.core.engine;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.pinion.core.asset.Asset;

import java.net.MalformedURLException;
import java.net.URL;

public class LESSCompiler implements Engine {

  LessCompiler lessCompiler = new LessCompiler();

  public LESSCompiler() {

  }

  @Override
  public String run(String input, Asset asset) {
    try {
      return lessCompiler.compile(input);
    } catch (LessException e) {
      throw new IllegalStateException("Less Compilation Failed: " + e.getMessage());
    }
  }
}
