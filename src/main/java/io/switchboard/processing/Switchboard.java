package io.switchboard.processing;

import akka.stream.FlowMaterializer;
import akka.stream.javadsl.MaterializedMap;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.switchboard.grammar.SwitchboardLexer;
import io.switchboard.grammar.SwitchboardParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Created by Christoph Grotz on 17.12.14.
 */
public class Switchboard {

  private final SwitchboardParser.StatementContext statement;

  private Switchboard(SwitchboardParser.StatementContext statement) {
    this.statement = statement;
  }

  public static Switchboard expression(String expression) {
    SwitchboardLexer l = new SwitchboardLexer(new ANTLRInputStream(expression));
    SwitchboardParser p = new SwitchboardParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });
    return new Switchboard(p.statement());
  }

  public MaterializedMap runWith( final FlowMaterializer flowMaterializer, final Source<ObjectNode> inputSource, final Sink<ObjectNode> sink ) {
    Source<ObjectNode> source = inputSource;
    source = source.filter(new ExpressionPredicate(statement.expression()));
    /*for(final SwitchboardParser.CommandContext command : statement.command()) {
      source = source.filter(new CommandPredicate(command));
    }*/
    return source.to(sink).run(flowMaterializer);
  }
}
