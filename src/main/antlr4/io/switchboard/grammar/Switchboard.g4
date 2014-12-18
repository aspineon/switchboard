grammar Switchboard;

//statement : expression command* ;
statement : expression;

expression : expression (AND | OR | NOT) expression # logicalAssociatedExpression
 | predicate # expressionPredicate
 | '(' expression ')' # groupedExpression
 ;

predicate : text (EQUALS | NOTEQUALS | GREQUALS | LSEQUALS | GREATERTHAN | LESSTHAN) text ;

/*
command : '| show' text* # showcmd
 | '| show' text (',' text)* # showcsv
 ;*/

text : NUMBER # numberText | QTEXT # quotedText | UQTEXT # unquotedText ;

AND : 'AND' ;
OR : 'OR' ;
NOT : 'NOT' ;
EQUALS : '=' ;
NOTEQUALS : '!=' ;
GREQUALS : '>=' ;
LSEQUALS : '<=' ;
GREATERTHAN : '>' ;
LESSTHAN : '<' ;

NUMBER : DIGIT+ | DIGIT+ '.' DIGIT+ | '.' DIGIT+;
QTEXT : '"' (ESC|.)*? '"' ;
UQTEXT : ~[ ()=,<>!\r\n]+ ;

fragment
DIGIT : [0-9] ;
fragment
ESC : '\\"' | '\\\\' ;

WS : [ \t\r\n]+ -> skip ;