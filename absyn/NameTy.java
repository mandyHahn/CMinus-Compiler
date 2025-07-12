package absyn;

public class NameTy extends Absyn {
  public final static int BOOL = 0;
  public final static int INT  = 1;
  public final static int VOID = 2;

  public int typ;

  public NameTy( int row, int col, int typ ) {
    this.row = row;
    this.col = col;
    this.typ = typ;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }

  public String name() {
    switch( typ ) {
      case BOOL:
        return "bool";
      case INT:
        return "int";
      case VOID:
        return "void";
      default:
        return "Unrecognized type at line " + row + " and column " + col;
    }
  }
}
