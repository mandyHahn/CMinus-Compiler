/*
  File Name: CM.java

  This file was repurposed from the SampleScanner given to start Checkpoint One
  Contains the main used to run the lexer and parser, as well as 

  To Build: 
  After the Scanner.java, tiny.flex, and tiny.cup have been processed, do:
    javac CM.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. CM test.cm

  where test.cm is a test input file for the C Minus language.
*/
   
import java.io.*;
import absyn.*;
   
class CM {
  public static boolean OUTPUT_ABS = false;
  public static boolean OUTPUT_SYM = false;
  public static boolean OUTPUT_TM = false;
  
  static private void usage() {
    System.out.println("Usage: java -cp <path to cup runtime> CM [ -a | -s | -c ] file.cm");
    System.out.println("  -a : perform syntactic analysis and output an abstract syntax tree (.abs)");
    System.out.println("  -s : perform type checking and output symbol tables (.sym)");
    System.out.println("  -c : compile and output TM assembly language code (.tm)");
  }

  static public void main(String argv[]) {    
    int numArgs = argv.length;

    if (numArgs < 2) {
      System.out.println("The file to compile must be included as the last parameter");
      usage();
      return;
    } 
    else if (numArgs > 2) {
      System.out.println("Only one command line option may be used");
      usage();
      return;
    }
    else if (numArgs == 2) {
      switch (argv[0]) {
        case "-c":
          OUTPUT_TM = true;
        case "-s":
          OUTPUT_SYM = true;
        case "-a":
          OUTPUT_ABS = true;
          break;
        default:
          usage();
        return;
      }
    }
    
    if (!new File(argv[numArgs-1]).isFile()) {
      System.out.println("File " + argv[numArgs-1] + " does not exist");
      return;
    }
    String fileNameBase = argv[numArgs-1].substring(0, argv[numArgs-1].length()-3);
    PrintStream originalOut = System.out;

    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[numArgs-1])));
      Absyn result = (Absyn)(p.parse().value);     

      // TODO: SET SHOW_TREE TO FALSE FOR C2
      // if (SHOW_TREE && result != null) {
      //   System.out.println("The abstract syntax tree is:");
      //   AbsynVisitor visitor = new ShowTreeVisitor();
      //   result.accept(visitor, 0); 
      // }
        
      if (OUTPUT_ABS && result != null) {
        System.setOut(new PrintStream(new FileOutputStream(fileNameBase + ".abs"), true));
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false); 
        System.setOut(originalOut);
      }

      if (!parser.isValid) {
        return;
      }

      if (OUTPUT_SYM && result != null) {
        System.setOut(new PrintStream(new FileOutputStream(fileNameBase + ".sym"), true));
        SemanticAnalyzer smVisitor = new SemanticAnalyzer();
        result.accept(smVisitor, 0, false); 
        System.setOut(originalOut);
      }

      if (!SemanticAnalyzer.isValid) {
        return;
      }
      
      if (OUTPUT_TM && result != null) {
        System.setOut(new PrintStream(new FileOutputStream(fileNameBase + ".tm"), true));
        CodeGenerator cgVisitor = new CodeGenerator();
        cgVisitor.visit(result);
        System.setOut(originalOut);
      }

    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
