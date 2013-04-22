package org.pinion.example;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;
import org.pinion.dropwizard.PinionConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExampleConfiguration extends Configuration {

  @NotEmpty
  @JsonProperty
  private String defaultName = "PinionExample";

  @Valid
  @NotNull
  @JsonProperty
  private PinionConfiguration pinion = new PinionConfiguration();

  public PinionConfiguration getPinionConfiguration() {
    return pinion;
  }

  public String getDefaultName() {
    return defaultName;
  }
}

