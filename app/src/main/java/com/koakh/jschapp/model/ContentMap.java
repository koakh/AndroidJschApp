package com.koakh.jschapp.model;

import android.util.Log;

import com.google.gson.Gson;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.util.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mario on 31/01/2015.
 * @see http://examples.javacodegeeks.com/java-basics/java-map-example/
 */

//public class ContentMap<K, V> implements Map<K, V> {
public class ContentMap<K, V> extends HashMap {
  /*
  @Override
  public V put(K k, V v) {
    return this.put(k, v);
  }

  @Override
  public V get(Object o) {
    return this.get(o);
  }

  @Override
  public V remove(Object o) {
    return this.remove(o);
  }

  @Override
  public void clear() {
    this.clear();
  }

  @Override
  public Collection<V> values() {
    return this.values();
  }

  @Override
  public boolean isEmpty() {
    return (this.size() > 0 ? false : true );
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return this.entrySet();
  }

  @Override
  public Set<K> keySet() {
    return this.keySet();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    this.putAll(map);
  }

  @Override
  public boolean containsKey(Object o) {
    return this.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return this.containsValue(o);
  }

  @Override
  public int size() {
    return this.size();
  }
  */

  /**
   * Write Gson File
   * @param pFileName
   * @return
   */
  public Boolean writeGson(String pFileName) {
    Gson gson = new Gson();

    String json = gson.toJson(this);
    Log.i(Singleton.getInstance().TAG, String.format("writeGson: %s", pFileName));

    try {
      return Utils.fileWrite(pFileName, json, false);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}

