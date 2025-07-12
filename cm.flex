/*
  File Name: cm.flex

  This file was repurposed from the SampleScanner given to start Checkpoint One
  Contains the scanner definition for the C Minus language
*/
   
import java_cup.runtime.*;


%%

%class Lexer

%line
%column
%cup

%{
    public boolean yywrap() {
        return true;
    }

    /* To create a new java_cup.runtime.Symbol with information about
    the current token, the token will have no value in this
    case. */
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%};

%eofval{
    return null;
%eofval};

// Define all of the useful regex that will be used for the scanner
LineTerminator = \r|\n|\r\n
whitespace = {LineTerminator} | [ \t\f]
digit = [0-9]
number = {digit}+
identifier = [_a-zA-Z][_a-zA-Z0-9]*
truth_statement = "false" | "true"
comment = "/*"~"*/" // ~ = match anything lazily (as little as possible) meaning that the comment will only go as far as it needs to

%%
    
// Use the regex defined above to parse the tokens
{comment}          { /* Get outta here */ }
{whitespace}+      { /* Get outta here */ }
{truth_statement}  { return symbol(sym.TRUTH, yytext()); }
"while"            { return symbol(sym.WHILE); }
"void"             { return symbol(sym.VOID); }
"return"           { return symbol(sym.RETURN); }
"int"              { return symbol(sym.INTEGER); }
"if"               { return symbol(sym.IF); }
"else"             { return symbol(sym.ELSE); }
"bool"             { return symbol(sym.BOOL); }
"+"                { return symbol(sym.PLUS); }
"-"                { return symbol(sym.MINUS); }
"*"                { return symbol(sym.TIMES); }
"/"                { return symbol(sym.OVER); }
"<="               { return symbol(sym.LTEQUAL); }
"<"                { return symbol(sym.LT); }
">="               { return symbol(sym.GTEQUAL); }
">"                { return symbol(sym.GT); }
"=="               { return symbol(sym.EQUAL); }
"!="               { return symbol(sym.NOTEQUAL); }
"~"                { return symbol(sym.NOT); }
"||"               { return symbol(sym.OR); }
"&&"               { return symbol(sym.AND); }
"="                { return symbol(sym.ASSIGN); }
";"                { return symbol(sym.SEMI); }
","                { return symbol(sym.COMMA); }
"("                { return symbol(sym.LPAREN); }
")"                { return symbol(sym.RPAREN); }
"["                { return symbol(sym.LSQUARE); }
"]"                { return symbol(sym.RSQUARE); }
"{"                { return symbol(sym.LCURLY); }
"}"                { return symbol(sym.RCURLY); }
{identifier}       { return symbol(sym.ID, yytext()); }
{number}           { return symbol(sym.NUM, yytext()); }
.                  { return symbol(sym.ERROR); }
