package org.pinion.core.compressor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.pinion.core.asset.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class UglifyCompressor implements Compressor {
  private final Logger LOG = LoggerFactory.getLogger(UglifyCompressor.class);

  private Scriptable scope;
  private Function compile;
  public Options options;

  public UglifyCompressor(final Options options) {
    if (options == null) {
      this.options = new Options();
    } else {
      this.options = options;
    }
    buildJSEngine();
  }

  private void buildJSEngine() {
    //TODO move this to an external js file
    String script = "var compile = function(code, ops) {" +
        "var options = JSON.parse(ops);" +
        "var ast = UglifyJS.parse(code);" +
        "ast.figure_out_scope();" +
        "var compressor = UglifyJS.Compressor(options);" +
        "var ast = ast.transform(compressor);" +
        "return ast.print_to_string();};";

    try (InputStream is = getClass().getResourceAsStream("/support/uglifyjs.js")) {
      final Context cx = Context.enter();
      cx.setOptimizationLevel(9);
      final Global global = new Global();
      global.init(cx);
      scope = cx.initStandardObjects(global);
      cx.evaluateReader(scope, new InputStreamReader(is), "uglifyjs", 1, null);
      cx.evaluateString(scope, script, "compress", 1, null);
      compile = (Function) scope.get("compile", scope);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load uglify engine");
    } finally {
      Context.exit();
    }
  }

  @Override
  public String run(String input, Asset asset) {
    //TODO its fucking stupid to convert the options to JSON then parse them in JS but I've now spent 4 hours
    //fucking with Rhino in an effort to pass them as a NativeObject and failed, I give up for now.
    //Rhino's documentation is only slightly better than its performance, which is to say, terrible.
    final Object[] args = {input, options.toJson()};

    Long start = System.currentTimeMillis();
    String result = (String) Context.call(null, compile, scope, scope, args);
    Long time = System.currentTimeMillis() - start;
    LOG.info("[Uglify] " + asset.logicalPath + " " + time );
    return result;
  }

  public class Options {
    public boolean sequences = true;
    public boolean properties = true;
    public boolean dead_code = true;
    public boolean drop_debugger = true;
    public boolean unsafe = false;
    public boolean conditionals = true;
    public boolean comparisons = true;
    public boolean evaluate = true;
    public boolean booleans = true;
    public boolean loops = true;
    public boolean unused = true;
    public boolean hoist_funs = true;
    public boolean hoist_vars = false;
    public boolean if_return = true;
    public boolean join_vars = true;
    public boolean cascade = true;
    public boolean side_effects = true;
    public boolean warnings = true;
    public Map<String, Object> global_defs = new HashMap<>();

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
