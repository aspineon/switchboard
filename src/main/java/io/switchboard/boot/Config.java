package io.switchboard.boot;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by Christoph Grotz on 20.12.14.
 */
public class Config {
  public static final String SWITCHBOARD_HOST_INTERFACE = "SWITCHBOARD_HOST_INTERFACE";
  public static final String SWITCHBOARD_HOST_PORT = "SWITCHBOARD_HOST_PORT";
  public static final String SWITCHBOARD_METADATA_BROKER_LIST = "SWITCHBOARD_METADATA_BROKER_LIST";
  public static final String SWITCHBOARD_ZOOKEEPER_CONNECT = "SWITCHBOARD_ZOOKEEPER_CONNECT";

  private Map<String, String> properties = Maps.newHashMap();

  public String get(String key, String def) {
    return properties.getOrDefault(key, def);
  }

  public String get(String key) {
    if(!properties.containsKey(key)) {
      throw new IllegalArgumentException("key ["+key+"] not found in config");
    }
    return properties.get(key);
  }

  public void put(String key, String value) {
    properties.put(key, value);
  }

  private Config() {

  }

  private static Config instance = new Config();

  public static Config get() {
    return instance;
  }
}
