JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=cup
# JFLEX=~/Projects/jflex/bin/jflex
# CLASSPATH=-cp ".;H:/CUP/CUP-master/lib/java-cup-11b-runtime.jar"
# CLASSPATH=-cp /home/nick/DevelopmentHome/Tools/java-cup-11b-runtime.jar:/home/nick/DevelopmentHome/Tools/java-cup-11b.jar:.
# CUP=$(JAVA) $(CLASSPATH) java_cup.Main

all: CM.class

CM.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java SemanticAnalyzer.java CodeGenerator.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 20 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~

# rule to clean on windows (normal clean doesn't work)
clean-win: 
	del *.class parser.java Lexer.java sym.java
	del .\absyn\*.class

clean-gen:
	rm -f tests/*.abs tests/*.tm tests/*.sym

clean-gen-win:
	del .\tests\*.abs .\tests\*.tm .\tests\*.sym
