package absyn;

public class SimpleDec extends VarDec {
  public NameTy typ;
  public String name;

  public SimpleDec( int row, int col, NameTy typ, String name ) {
    this.row = row;
    this.col = col;
    this.typ = typ;
    this.name = name;
  }
  
  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }

  public String type() {
    return typ.name();
  }
}
