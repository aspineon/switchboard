package io.switchboard;

import akka.actor.ActorSystem;
import io.switchboard.api.BasicApi;
import io.switchboard.grammar.SwitchboardLexer;
import io.switchboard.grammar.SwitchboardParser;
import org.antlr.v4.runtime.*;

/**
 * Main class for switchboard
 *
 * Created by Christoph Grotz on 18.11.14.
 */
public class Boot{

  public static void main(String ... args) throws Exception {
    ActorSystem actorSystem = ActorSystem.create();
    BasicApi.apply(actorSystem).bindRoute("0.0.0.0", 8080);

    //System.out.println(parse("Abishek AND (country=India OR city=NY) LOGIN 404 | show name city").expr().getChild(0).getChild(0));
  }

  private static SwitchboardParser.StatementContext parse(String input) {
    SwitchboardLexer l = new SwitchboardLexer(new ANTLRInputStream(input));
    SwitchboardParser p = new SwitchboardParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });
    return p.statement();
  }

}
