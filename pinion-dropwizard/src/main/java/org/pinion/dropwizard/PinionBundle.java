package org.pinion.dropwizard;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.pinion.core.Pinion;
import org.pinion.core.PinionServlet;

public abstract class PinionBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T>{
  private static final String DEFAULT_PATH = "assets/";
  private String servletPath;

  public PinionBundle() {
    this(DEFAULT_PATH);
  }

  public PinionBundle(String servletPath) {
    this.servletPath = servletPath.endsWith("/") ? servletPath : (servletPath + '/');
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // nothing doing
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
    initPinion(getPinionConfiguration(configuration));
    environment.addServlet(new PinionServlet(), servletPath + '*');
  }

  private void initPinion(PinionConfiguration config) {
    //Convert dropwizard config to internal config
    Pinion.init(config.buildPinionConfig());
  }
}
