package org.pinion.core.cache;

import org.pinion.core.asset.Asset;

public class Cache {

  protected void put(String key, byte[] value) {
    throw new UnsupportedOperationException();
  }

  protected byte[] get(String key) {
    throw new UnsupportedOperationException();
  }

  protected String getKey(final Asset asset) {
    return asset.fsPath + asset.logicalPath + asset.digest.toString();
  }

  public void put(final Asset a, final byte[] value) {
    put(getKey(a), value);
  }

  public byte[] get(final Asset a) {
    return get(getKey(a));
  }
}
