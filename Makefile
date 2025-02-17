JAVAC=javac
JFLEX=jflex
# JFLEX=/home/nick/DevelopmentHome/Tools/jflex-1.9.1/bin/jflex

all: Token.class Lexer.class Scanner.class

%.class: %.java
	$(JAVAC) $^

Lexer.java: tiny.flex
	$(JFLEX) tiny.flex

clean:
	rm -f Lexer.java *.class *~
