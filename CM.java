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
  public final static boolean SHOW_TREE = true;
  public static boolean OUTPUT_ABS = false;
  public static boolean OUTPUT_SYM = false;
  public static boolean OUTPUT_TM = false;
  
  static private void usage() {
    System.out.println("Usage: java -cp <path to cup runtime> CM [ -a | -s | -c ] file.cm");
    System.out.println("  -a : perform syntactic analysis and output an abstract syntax tree (.abs)");
    System.out.println("  -s : perform type checking and output symbol tables (.sym) -- NOT IMPLEMENTED");
    System.out.println("  -c : compile and output TM assembly language code (.tm) -- NOT IMPLEMENTED");
  }

  static public void main(String argv[]) {    
    int numArgs = argv.length;

    // // TODO: UNCOMMENT FOR C2
    // if (numArgs < 2) {
    //   usage();
    //   return;
    // } else
    if (numArgs > 2) {
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
      
    String fileNameBase = argv[numArgs-1].substring(0, argv[numArgs-1].length()-3);
    PrintStream originalOut = System.out;

    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[numArgs-1])));
      Absyn result = (Absyn)(p.parse().value);     

      if (SHOW_TREE && result != null) {
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0); 
      }
        
      if (OUTPUT_ABS && result != null) {
        System.setOut(new PrintStream(new FileOutputStream(fileNameBase + ".abs"), true));
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0); 
        System.setOut(originalOut);
      }

      // TODO: implement for C2
      if (OUTPUT_SYM) {
        System.out.println("-s not yet implemented");
      }
      
      // TODO: implement for C3
      if (OUTPUT_TM) {
        System.out.println("-c not yet implemented");
      }

    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
