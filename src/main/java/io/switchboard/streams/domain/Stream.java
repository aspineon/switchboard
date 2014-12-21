package io.switchboard.streams.domain;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Stream {
  private String name;
  private String expression;
  private String id;

  public Stream() {
  }

  public Stream(String id, String name, String expression) {
    this.name = name;
    this.expression = expression;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Stream setName(String name) {
    this.name = name;
    return this;
  }

  public String getId() {
    return id;
  }

  public Stream setId(String id) {
    this.id = id;
    return this;
  }

  public String getExpression() {
    return expression;
  }

  public Stream setExpression(String expression) {
    this.expression = expression;
    return this;
  }

  public static Stream convert(DBObject dbObject) {
    return new Stream()
      .setId(dbObject.get("id").toString())
      .setName(dbObject.get("name").toString())
      .setExpression(dbObject.get("expression").toString());
  }

  public static DBObject convert(Stream stream) {
    BasicDBObject basicDBObject =  new BasicDBObject();
    basicDBObject.append("id", stream.getId());
    basicDBObject.append("name", stream.getName());
    basicDBObject.append("expression", stream.getExpression());
    return basicDBObject;
  }
}
