package org.pinion.core.engine;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;
import org.pinion.core.asset.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

public class JadeCompiler implements Engine {
  private final Logger LOG = LoggerFactory.getLogger(JadeCompiler.class);
  JadeConfiguration config = new JadeConfiguration();

  public JadeCompiler() {
    config.setTemplateLoader(new J4JLoader());
  }

  @Override
  public String run(String input, Asset asset) {
    try {
      Long start = System.currentTimeMillis();
      String result = config.renderTemplate(config.getTemplate(input),new HashMap<String,Object>());
      Long time = System.currentTimeMillis() - start;
      LOG.info("[Jade] " + asset.logicalPath + " " + time );
      return result;
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
