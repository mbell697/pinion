package org.pinion.core.engine;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.pinion.core.asset.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LESSCompiler implements Engine {
  private final Logger LOG = LoggerFactory.getLogger(LESSCompiler.class);

  private static final String COMPILE_STRING = "function doIt(input, compress) { var result; var parser = new less.Parser(); parser.parse(input, function(e, tree) { if (e instanceof Object) { throw e; } ; result = tree.toCSS({compress: compress}); }); return result; }";

  private URL envJs = LESSCompiler.class.getClassLoader().getResource("support/less/env.rhino.js");
  private URL lessJs = LESSCompiler.class.getClassLoader().getResource("support/less/less.js");
  private List<URL> customJs = Collections.emptyList();
  private boolean compress = false;

  private Function doIt;

  private Scriptable scope;

  public LESSCompiler() {
    init();
  }

  private void init() {
    long start = System.currentTimeMillis();

    try {
      Context cx = Context.enter();
      cx.setOptimizationLevel(-1);
      cx.setLanguageVersion(Context.VERSION_1_7);

      Global global = new Global();
      global.init(cx);

      scope = cx.initStandardObjects(global);
      scope.put("logger", scope, Context.toObject(LOG, scope));

      List<URL> jsUrls = new ArrayList<URL>(2 + customJs.size());
      jsUrls.add(envJs);
      jsUrls.add(lessJs);
      jsUrls.addAll(customJs);

      for(URL url : jsUrls){
        InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
        try{
          cx.evaluateReader(scope, inputStreamReader, url.toString(), 1, null);
        }finally{
          inputStreamReader.close();
        }
      }
      doIt = cx.compileFunction(scope, COMPILE_STRING, "doIt.js", 1, null);
    }
    catch (Exception e) {
      String message = "Failed to initialize LESS compiler.";
      LOG.error(message, e);
      throw new IllegalStateException(message, e);
    }finally{
      Context.exit();
    }
    LOG.info("Finished initialization of LESS compiler in " + (System.currentTimeMillis() - start) + " ms.");
  }

  @Override
  public String run(String input, Asset asset) {
    long start = System.currentTimeMillis();

    try {
      LessSource source = new LessSource(asset.fsPath + asset.logicalPath, input);
      Context cx = Context.enter();
      Object result = doIt.call(cx, scope, null, new Object[]{source.getNormalizedContent(), compress});

      LOG.info("[LESS] " + asset.logicalPath + " " + (System.currentTimeMillis() - start) + " ms.");

      return result.toString();
    }
    catch (Exception e) {
      if (e instanceof JavaScriptException) {
        Scriptable value = (Scriptable)((JavaScriptException)e).getValue();
        if (value != null && ScriptableObject.hasProperty(value, "message")) {
          String message = ScriptableObject.getProperty(value, "message").toString();
          throw new Error(message, e);
        }
      }
      throw new Error(e);
    }finally{
      Context.exit();
    }
  }
}
