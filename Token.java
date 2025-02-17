/**
 * Class taken from example given in handout document.
 * Holds all tokens 
 */
class Token {

  // Define values for different tags
  public final static int NUM = 1;
  public final static int ID = 2;
  public final static int TRUTH = 3;
  public final static int WHILE = 4;
  public final static int VOID = 5;
  public final static int RETURN = 6;
  public final static int INT = 7;
  public final static int IF = 8;
  public final static int ELSE = 9;
  public final static int BOOL = 10;
  public final static int PLUS = 11;
  public final static int MINUS = 12;
  public final static int MULT = 13;
  public final static int DIV = 14;
  public final static int LT = 15;
  public final static int LTE = 16;
  public final static int GT = 17;
  public final static int GTE = 18;
  public final static int EQ = 19;
  public final static int NEQ = 20;
  public final static int OR = 21;
  public final static int AND = 22;
  public final static int ASS = 23;
  public final static int SEMICOLON = 24;
  public final static int COMMA = 25;
  public final static int OPENPAREN = 26;
  public final static int CLOSEPAREN = 27;
  public final static int OPENSQUARE = 28;
  public final static int CLOSESQUARE = 29;
  public final static int OPENANGLED = 30;
  public final static int CLOSEANGLED = 31;
  public final static int ERROR = 32;

  public int m_type;
  public String m_value;
  public int m_line;
  public int m_column;
  
  // Constructor for token (which is the datatype used in tiny.flex)
  Token (int type, String value, int line, int column) {
    m_type = type;
    m_value = value;
    m_line = line;
    m_column = column;
  }

  // When the token gets printed, the .toString method will be called.
  // Parse the tokens into expected format
  public String toString() {
    switch (m_type) {
      case NUM:
        return "NUM(" + m_value + ")";
      case ID:
        return "ID(" + m_value + ")";
      case TRUTH:
        return "TRUTH(" + m_value + ")";
      case WHILE:
        return "WHILE";
      case VOID:
        return "VOID";
      case RETURN:
        return "RETURN";
      case INT:
        return "INT";
      case IF:
        return "IF";
      case ELSE:
        return "ELSE";
      case BOOL:
        return "BOOL";
      case PLUS:
        return "PLUS";
      case MINUS:
        return "MINUS";
      case MULT:
        return "MULT";
      case DIV:
        return "DIV";
      case LT:
        return "LT";
      case LTE:
        return "LTE";
      case GT:
        return "GT";
      case GTE:
        return "GTE";
      case EQ:
        return "EQ";
      case NEQ:
        return "NEQ";
      case OR:
        return "OR";
      case AND:
        return "AND";
      case ASS:
        return "ASS";
      case SEMICOLON:
        return "SEMICOLON";
      case COMMA:
        return "COMMA";
      case OPENPAREN:
        return "OPENPAREN";
      case CLOSEPAREN:
        return "CLOSEPAREN";
      case OPENSQUARE:
        return "OPENSQUARE";
      case CLOSESQUARE:
        return "CLOSESQUARE";
      case OPENANGLED:
        return "OPENANGLED";
      case CLOSEANGLED:
        return "CLOSEANGLED";
      case ERROR:
        System.err.println("Was not able to find matching token for " + m_value.toUpperCase());
        return "ERROR";
      default:
        return "UNKNOWN(" + m_value + ")";
    }
  }
}

