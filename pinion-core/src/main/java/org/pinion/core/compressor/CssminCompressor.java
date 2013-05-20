package org.pinion.core.compressor;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.pinion.core.asset.Asset;

import java.io.*;

public class CssminCompressor implements Compressor {
  private Scriptable scope;
  private Options options;

  public CssminCompressor(final Options options) {
    if (options == null) {
      this.options = new Options();
    } else {
      this.options = options;
    }
    buildJSEngine();
  }

  private void buildJSEngine() {
    try {
      try (InputStream inputStream = getClass().getResourceAsStream("/support/cssmin.js")) {
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
          final Context context = Context.enter();
          context.setOptimizationLevel(9);
          scope = context.initStandardObjects();
          context.evaluateReader(scope, reader, "cssmin.js", 0, null);
        } finally {
          Context.exit();
        }
      } catch (UnsupportedEncodingException e) {
        throw new Error(e);
      }
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Override
  public String run(final String input, final Asset asset) {
    final Context context = Context.enter();
    try {
      final Scriptable compileScope = context.newObject(scope);
      compileScope.setParentScope(scope);
      compileScope.put("cssSource", compileScope, input);
      try {
        return (String) context.evaluateString(compileScope,
            "cssmin(cssSource);",
            "CssMinifier", 0, null);
      } catch (JavaScriptException e) {
        throw new IllegalStateException("Failed to compress css: " + e.getMessage());
      }
    } finally {
      Context.exit();
    }
  }

  public class Options {
    public int lineBreakPos = 0;
  }
}
