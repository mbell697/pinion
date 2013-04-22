package org.pinion.core.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.pinion.core.asset.Asset;

import java.io.*;

public class CoffeeScriptCompiler implements Engine {
  private Scriptable scope;
  private Options options;

  public CoffeeScriptCompiler(final Options options) {
    if (options == null) {
      this.options = new Options();
    } else {
      this.options = options;
    }
    buildJSEngine();
  }

  private void buildJSEngine() {
    try {
      try (InputStream inputStream = getClass().getResourceAsStream("../../../../support/coffee-script.js")) {
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
          final Context context = Context.enter();
          context.setOptimizationLevel(9);
          scope = context.initStandardObjects();
          context.evaluateReader(scope, reader, "coffee-script.js", 0, null);
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
  public String run(String input, Asset asset) {
    Context context = Context.enter();
    try {
      final Scriptable compileScope = context.newObject(scope);
      compileScope.setParentScope(scope);
      compileScope.put("coffeeScriptSource", compileScope, input);
      try {
        return (String) context.evaluateString(compileScope,
            String.format("CoffeeScript.compile(coffeeScriptSource, %s);",
                options.toJson()),
            "CoffeeScriptCompiler", 0, null);
      } catch (JavaScriptException e) {
        throw new IllegalStateException("Failed to compile CS: " + e.getMessage());
      }
    } finally {
      Context.exit();
    }
  }

  public class Options {

    public String toJson() {
      final ObjectMapper mapper = new ObjectMapper();
      try {
        return mapper.writeValueAsString(this);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException("Failed to convert to Options JSON");
      }
    }
  }
}
