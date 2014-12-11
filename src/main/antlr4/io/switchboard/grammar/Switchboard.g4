grammar Switchboard;

statement : expr command* ;

expr : expr ('AND' | 'OR' | 'NOT') expr # expopexp
 | expr expr # expexp
 | predicate # predicexpr
 | text # textexpr
 | '(' expr ')' # exprgroup
 ;

predicate : text ('=' | '!=' | '>=' | '<=' | '>' | '<') text ;

command : '| show' text* # showcmd
 | '| show' text (',' text)* # showcsv
 ;

text : NUMBER # numbertxt
 | QTEXT # quotedtxt
 | UQTEXT # unquotedtxt
 ;

AND : 'AND' ;
OR : 'OR' ;
NOT : 'NOT' ;
EQUALS : '=' ;
NOTEQUALS : '!=' ;
GREQUALS : '>=' ;
LSEQUALS : '<=' ;
GREATERTHAN : '>' ;
LESSTHAN : '<' ;

NUMBER : DIGIT+
 | DIGIT+ '.' DIGIT+
 | '.' DIGIT+
 ;
QTEXT : '"' (ESC|.)*? '"' ;
UQTEXT : ~[ ()=,<>!\r\n]+ ;

fragment
DIGIT : [0-9] ;
fragment
ESC : '\\"' | '\\\\' ;

WS : [ \t\r\n]+ -> skip ;