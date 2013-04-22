package org.pinion.dropwizard;

import com.yammer.dropwizard.config.Configuration;

public interface ConfigurationStrategy <T extends Configuration> {
  PinionConfiguration getPinionConfiguration(T configuration);
}