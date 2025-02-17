// import java.util.HashSet;
// import java.util.Arrays;
%%

%class Lexer
%type Token
%line
%column

%{
    // private static final HashSet<String> keywords = new HashSet<>(
    //     Arrays.asList("bool", "else", "if", "int", "return", "void", "while", "false", "true");
    // );
    public boolean yywrap() {
        return true;
    }

%};

%eofval{
    System.out.println("Reaching end of file!");
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
{whitespace}       { /* Get outta here */ }
{truth_statement}  { return new Token(Token.TRUTH, yytext(), yyline, yycolumn); }
"while"            { return new Token(Token.WHILE, yytext(), yyline, yycolumn); }
"void"             { return new Token(Token.VOID, yytext(), yyline, yycolumn); }
"return"           { return new Token(Token.RETURN, yytext(), yyline, yycolumn); }
"int"              { return new Token(Token.INT, yytext(), yyline, yycolumn); }
"if"               { return new Token(Token.IF, yytext(), yyline, yycolumn); }
"else"             { return new Token(Token.ELSE, yytext(), yyline, yycolumn); }
"bool"             { return new Token(Token.BOOL, yytext(), yyline, yycolumn); }
"+"                { return new Token(Token.PLUS, yytext(), yyline, yycolumn); }
"-"                { return new Token(Token.MINUS, yytext(), yyline, yycolumn); }
"*"                { return new Token(Token.MULT, yytext(), yyline, yycolumn); }
"/"                { return new Token(Token.DIV, yytext(), yyline, yycolumn); }
"<="               { return new Token(Token.LTE, yytext(), yyline, yycolumn); }
"<"                { return new Token(Token.LT, yytext(), yyline, yycolumn); }
">="               { return new Token(Token.GTE, yytext(), yyline, yycolumn); }
">"                { return new Token(Token.GT, yytext(), yyline, yycolumn); }
"=="               { return new Token(Token.EQ, yytext(), yyline, yycolumn); }
"!="               { return new Token(Token.NEQ, yytext(), yyline, yycolumn); }
"||"               { return new Token(Token.OR, yytext(), yyline, yycolumn); }
"&&"               { return new Token(Token.AND, yytext(), yyline, yycolumn); }
"="                { return new Token(Token.ASS, yytext(), yyline, yycolumn); }
";"                { return new Token(Token.SEMICOLON, yytext(), yyline, yycolumn); }
","                { return new Token(Token.COMMA, yytext(), yyline, yycolumn); }
"("                { return new Token(Token.OPENPAREN, yytext(), yyline, yycolumn); }
")"                { return new Token(Token.CLOSEPAREN, yytext(), yyline, yycolumn); }
"["                { return new Token(Token.OPENSQUARE, yytext(), yyline, yycolumn); }
"]"                { return new Token(Token.CLOSESQUARE, yytext(), yyline, yycolumn); }
"{"                { return new Token(Token.OPENANGLED, yytext(), yyline, yycolumn); }
"}"                { return new Token(Token.CLOSEANGLED, yytext(), yyline, yycolumn); }
"return"           { return new Token(Token.RETURN, yytext(), yyline, yycolumn); }
{identifier}       { return new Token(Token.ID, yytext(), yyline, yycolumn); }
{number}           { return new Token(Token.NUM, yytext(), yyline, yycolumn); }
.                  { return new Token(Token.ERROR, yytext(), yyline, yycolumn); }
