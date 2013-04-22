package org.pinion.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.pinion.core.Configuration;

import javax.validation.Valid;

public class PinionConfiguration {

  @JsonProperty
  private String assetPath = null;

  @JsonProperty
  private String jsCompressor = null;

  @NotEmpty
  @JsonProperty
  private String appVersion = null;

  @Valid
  @JsonProperty
  private CoffeeConfiguration coffee = null;

  @Valid
  @JsonProperty
  private LessConfiguration less = null;

  @Valid
  @JsonProperty
  private ClosureConfiguration closure = null;

  @Valid
  @JsonProperty
  private UglifierConfiguration uglifier = null;

  @Valid
  @JsonProperty
  private CssminConfiguration cssmin = null;

  public PinionConfiguration () {
    assetPath = "assets/";
  }

  public Configuration buildPinionConfig() {
    return null; //TODO
  }

  public String getAssetPath() {
    return assetPath;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public CoffeeConfiguration getCoffee() {
    return coffee;
  }

  public LessConfiguration getLess() {
    return less;
  }

  public ClosureConfiguration getClosure() {
    return closure;
  }

  public UglifierConfiguration getUglifier() {
    return uglifier;
  }

  public CssminConfiguration getCssmin() {
    return cssmin;
  }

  public String getJsCompressor() {
    return jsCompressor;
  }
}
