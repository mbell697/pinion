package org.pinion.core.cache;

import org.pinion.core.asset.Asset;

public class Cache {

  protected void put(String key, String value) {
    throw new UnsupportedOperationException();
  }

  protected String get(String key) {
    throw new UnsupportedOperationException();
  }

  protected String getKey(final Asset asset) {
    return asset.fsPath + asset.logicalPath + asset.digest.toString();
  }

  public void put(final Asset a, final String value) {
    put(getKey(a), value);
  }

  public String get(final Asset a) {
    return get(getKey(a));
  }
}
