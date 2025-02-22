JAVA=java
JAVAC=javac
JFLEX=jflex
# JFLEX=/home/nick/DevelopmentHome/Tools/jflex-1.9.1/bin/jflex
# CLASSPATH=-cp ".;H:/CUP/CUP-master/lib/java-cup-11b-runtime.jar"
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=cup

all: Main.class

Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 3 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~

# rule to clean on windows (normal clean doesn't work)
clean-win: 
	del *.class parser.java Lexer.java sym.java
	del .\absyn\*.class
