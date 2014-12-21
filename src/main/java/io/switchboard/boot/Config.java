package io.switchboard.boot;

import com.google.common.collect.Maps;
import joptsimple.internal.Strings;
import org.apache.commons.cli.CommandLine;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Christoph Grotz on 20.12.14.
 */
public class Config {
  public static final String SWITCHBOARD_HOST_INTERFACE = "SWITCHBOARD_HOST_INTERFACE";
  public static final String SWITCHBOARD_HOST_PORT = "SWITCHBOARD_HOST_PORT";
  public static final String SWITCHBOARD_METADATA_BROKER_LIST = "SWITCHBOARD_METADATA_BROKER_LIST";
  public static final String SWITCHBOARD_ZOOKEEPER_CONNECT = "SWITCHBOARD_ZOOKEEPER_CONNECT";
  public static final java.lang.String SWITCHBOARD_EXPRESSION = "SWITCHBOARD_EXPRESSION";
  public static final java.lang.String SWITCHBOARD_GROUP_ID = "SWITCHBOARD_GROUP_ID";
  public static final java.lang.String SWITCHBOARD_MONGO_URI = "SWITCHBOARD_MONGO_URI";

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

  public Optional<String> extractPropertyValue(CommandLine cmd, String opt, String env) {
    Optional<String> property;
    if (cmd.hasOption(opt)) {
      property = Optional.of(cmd.getOptionValue(opt));
      put(env, property.get());
    } else if (!Strings.isNullOrEmpty(System.getenv(env))) {
      property = Optional.of(System.getenv(env));
      put(env, property.get());
    } else {
      property = Optional.empty();
    }
    return property;
  }

  public String extractPropertyValue(CommandLine cmd, String opt, String env, String def) {
    String property;
    if (cmd.hasOption(opt)) {
      property = cmd.getOptionValue(opt);
    } else if (!Strings.isNullOrEmpty(System.getenv(env))) {
      property = System.getenv(env);
    } else {
      property = def;
    }
    put(env, property);
    return property;
  }
}
