package org.pinion.core.engine;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.asual.lesscss.LessOptions;
import org.pinion.core.asset.Asset;

public class LESSCompiler implements Engine {

  LessOptions options;
  LessEngine engine;

  public LESSCompiler(final LessOptions options) {
    if (options == null) {
      this.options = new LessOptions();
    } else {
      this.options = options;
    }
    engine = new LessEngine(this.options);
  }

  @Override
  public String run(String input, Asset asset) {
    try {
      return engine.compile(input);
    } catch (LessException e) {
      throw new IllegalStateException("Less Compilation Failed: " + e.getMessage());
    }
  }
}
