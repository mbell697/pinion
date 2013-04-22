package org.pinion.core;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.pinion.core.asset.Asset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class PinionServlet extends HttpServlet {

  public PinionServlet() {

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    //extract path
    String path = req.getPathInfo().replaceFirst("/", "");

    //fail on paths with ".." the servlet should do this but just in case
    if (path.contains("..")) {
      resp.setStatus(401);
      return;
    }

    //lookup asset
    try {
      Asset asset = Pinion.get().findAsset(path);
      resp.setStatus(200);
      resp.setContentType(asset.contentType.toString());

      //check if they asset is fresh, if so try to grab it's content from the cache
      String source = null;
      if (Pinion.get().fresh(asset)) { //content fresh, check in cache
        source = Pinion.get().cache.get(asset);
      }
      if (source == null) { //not in cache
        try (InputStream data = asset.source(Pinion.get())) {
          ByteStreams.copy(data, resp.getOutputStream());
          Reader r = new InputStreamReader(data);
          Pinion.get().cache.put(asset, CharStreams.toString(r));
          r.close();
          //TODO flush cache of old versions of this file???
        }
      } else //content found in cache
        resp.getWriter().write(source);
    } catch (IOException e) {
      resp.setStatus(404);
    }

    //TODO handling caching headers on both request and response
  }
}
