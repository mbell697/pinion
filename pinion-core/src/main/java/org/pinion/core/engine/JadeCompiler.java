package org.pinion.core.engine;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;
import org.pinion.core.asset.Asset;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JadeCompiler implements Engine {
  JadeConfiguration config = new JadeConfiguration();

  public JadeCompiler() {
    config.setTemplateLoader(new J4JLoader());
  }

  @Override
  public String run(String input, Asset asset) {
    try {
      return config.renderTemplate(config.getTemplate(input),null);
    } catch (IOException e) {
      throw new Error("This is impossible");
    }
  }

  public class J4JLoader implements TemplateLoader {
    @Override
    public long getLastModified(String s) throws IOException {
      return -1;
    }

    @Override
    public Reader getReader(String s) throws IOException {
      return new StringReader(s);
    }
  }
}
