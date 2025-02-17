import java.io.InputStreamReader;

/**
 * Scanner class taken entirely from example given with no necessary modifications. 
 */
public class Scanner {
  private Lexer scanner = null;

  // Constructor; pass in a lexer to parse. 
  public Scanner( Lexer lexer ) {
    scanner = lexer; 
  }

  // Get the next token out of the lexer
  public Token getNextToken() throws java.io.IOException {
    return scanner.yylex();
  }

  // The start of the program; boots up a scanner, tokenizes input and prints it to stdout.
  public static void main(String argv[]) {
    try {
      Scanner scanner = new Scanner(new Lexer(new InputStreamReader(System.in)));
      Token tok = null;
      while( (tok=scanner.getNextToken()) != null )
        System.out.println(tok);
    }
    catch (Exception e) {
      System.out.println("Unexpected exception:");
      e.printStackTrace();
    }
  }
}
