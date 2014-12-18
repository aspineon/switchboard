package io.switchboard.processing;

import akka.stream.javadsl.japi.Predicate;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.switchboard.grammar.SwitchboardParser;
import com.google.common.base.Strings;

import java.util.Arrays;

/**
 *
 *
 *
 * Created by Christoph Grotz on 17.12.14.
 */
public class ExpressionPredicate implements Predicate<ObjectNode> {
  private final SwitchboardParser.ExpressionContext inputExpression;

  public ExpressionPredicate(SwitchboardParser.ExpressionContext expression) {
    this.inputExpression = expression;
  }

  @Override
  public boolean test(ObjectNode param) {
    try {
      return evalExpression(inputExpression, param);
    }
    catch (Exception exp) {
      exp.printStackTrace();
      throw exp;
    }
  }

  private boolean evalExpression(Object expression, ObjectNode param) {
    if ( expression instanceof SwitchboardParser.LogicalAssociatedExpressionContext) {
      SwitchboardParser.LogicalAssociatedExpressionContext
        ctx = (SwitchboardParser.LogicalAssociatedExpressionContext)expression;
      if( ctx.AND() != null ) {
        // AND associated
        return evalExpression(ctx.expression(0), param) && evalExpression(ctx.expression(1), param);
      }
      else if( ctx.OR() != null ) {
        // OR associated
        return evalExpression(ctx.expression(0), param) || evalExpression(ctx.expression(1), param);
      }
      else if( ctx.NOT() != null ) {
        // NOT associated
        return evalExpression(ctx.expression(0), param) != evalExpression(ctx.expression(1), param);
      }
      else {
        throw new IllegalArgumentException();
      }
    }
    else if ( expression instanceof SwitchboardParser.ExpressionPredicateContext) {
      SwitchboardParser.ExpressionPredicateContext
        ctx = (SwitchboardParser.ExpressionPredicateContext)expression;
      return evalExpression(ctx.predicate(), param);
    }
    else if ( expression instanceof SwitchboardParser.GroupedExpressionContext) {
      SwitchboardParser.GroupedExpressionContext
        ctx = (SwitchboardParser.GroupedExpressionContext)expression;
      throw new IllegalArgumentException();
    }
    else if ( expression instanceof SwitchboardParser.PredicateContext) {
      SwitchboardParser.PredicateContext
        ctx = (SwitchboardParser.PredicateContext)expression;
      if( ctx.EQUALS() != null ) {
        String fieldValue = extractFieldValue(param, ctx);
        return Strings.nullToEmpty(ctx.text(1).getText()).equals(fieldValue);
      }
      else if( ctx.NOTEQUALS() != null ) {
        String fieldValue = extractFieldValue(param, ctx);
        return !Strings.nullToEmpty(ctx.text(1).getText()).equals(fieldValue);
      }
      // TODO Numeric Operations
      /*else if( ctx.GREATERTHAN() != null ) {

      }
      else if( ctx.GREQUALS() != null ) {

      }
      else if( ctx.LESSTHAN() != null ) {

      }
      else if( ctx.LSEQUALS() != null ) {

      }*/
      else {
        throw new IllegalArgumentException();
      }
    }
    else {
      throw new IllegalArgumentException();
    }
  }

  private String extractFieldValue(ObjectNode param, SwitchboardParser.PredicateContext ctx) {
    ObjectNode node = param;
    String fieldName = Strings.nullToEmpty(ctx.text(0).getText());
    String[] names = fieldName.split("\\.");
    if(names.length > 1) {
      for (String element : Arrays.copyOf(names, names.length-1)) {
        node = (ObjectNode) node.get(element);
      }
    }
    if(node.hasNonNull(names[names.length-1])) {
      return node.get(names[names.length - 1]).asText("");
    }
    else {
      return null;
    }
  }

}
