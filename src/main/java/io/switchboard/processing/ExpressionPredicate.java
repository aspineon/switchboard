package io.switchboard.processing;

import akka.stream.javadsl.japi.Predicate;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.switchboard.grammar.SwitchboardParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;
import com.google.common.base.Strings;

/**
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
        String fieldName = Strings.nullToEmpty(ctx.text(0).getText());
        if(param.hasNonNull(fieldName)) {
          String fieldValue = param.get(fieldName).asText("");
          return fieldValue.equals(Strings.nullToEmpty(ctx.text(1).getText()));
        }
        else {
          return false;
        }
      }
      else if( ctx.NOTEQUALS() != null ) {
        String fieldName = Strings.nullToEmpty(ctx.text(0).getText());
        if(param.hasNonNull(fieldName)) {
          String fieldValue = param.get(fieldName).asText("");
          return !fieldValue.equals(Strings.nullToEmpty(ctx.text(1).getText()));
        }
        else {
          return false;
        }
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

}
