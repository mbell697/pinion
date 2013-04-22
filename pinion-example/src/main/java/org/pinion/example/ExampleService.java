package org.pinion.example;


import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.pinion.dropwizard.PinionBundle;
import org.pinion.dropwizard.PinionConfiguration;

public class ExampleService extends Service<ExampleConfiguration> {

  private final PinionBundle<ExampleConfiguration> pinionBundle = new PinionBundle<ExampleConfiguration>("assets/") {
    @Override
    public PinionConfiguration getPinionConfiguration(ExampleConfiguration configuration) {
      return configuration.getPinionConfiguration();
    }
  };


  public static void main(String[] args) throws Exception {
    new ExampleService().run(args);
  }

  @Override
  public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.addBundle(pinionBundle);
  }

  @Override
  public void run(ExampleConfiguration configuration,
                  Environment environment) {
    environment.addResource(new ExampleResource());
  }

}