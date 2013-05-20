package org.pinion.core;

import com.google.javascript.jscomp.CompilerOptions;
import org.pinion.core.compressor.CssminCompressor;
import org.pinion.core.compressor.UglifyCompressor;
import org.pinion.core.engine.CoffeeScriptCompiler;

public class Configuration {

  private String assetPath = "assets/";

  private String appVersion = "";

  private String jsCompressor = "";

  private Boolean jsCompress = true;

  private Boolean cssCompress = true;

  private CoffeeScriptCompiler.Options coffee = null;

  private CompilerOptions closure = null;

  private UglifyCompressor.Options uglifier = null;

  private CssminCompressor.Options cssmin = null;

  public String getAssetPath() {
    return assetPath;
  }

  public void setAssetPath(String assetPath) {
    this.assetPath = assetPath;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public CoffeeScriptCompiler.Options getCoffee() {
    return coffee;
  }

  public void setCoffee(CoffeeScriptCompiler.Options coffee) {
    this.coffee = coffee;
  }

  public CompilerOptions getClosure() {
    return closure;
  }

  public void setClosure(CompilerOptions closure) {
    this.closure = closure;
  }

  public UglifyCompressor.Options getUglifier() {
    return uglifier;
  }

  public void setUglifier(UglifyCompressor.Options uglifier) {
    this.uglifier = uglifier;
  }

  public CssminCompressor.Options getCssmin() {
    return cssmin;
  }

  public void setCssmin(CssminCompressor.Options cssmin) {
    this.cssmin = cssmin;
  }

  public String getJsCompressor() {
    return jsCompressor;
  }

  public void setJsCompressor(String jsCompressor) {
    this.jsCompressor = jsCompressor;
  }

  public Boolean getJsCompress() {
    return jsCompress;
  }

  public void setJsCompress(Boolean jsCompress) {
    this.jsCompress = jsCompress;
  }

  public Boolean getCssCompress() {
    return cssCompress;
  }

  public void setCssCompress(Boolean cssCompress) {
    this.cssCompress = cssCompress;
  }
}
