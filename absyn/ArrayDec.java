package absyn;

public class ArrayDec extends VarDec {
  public NameTy typ;
  public String name;
  public int size;

  public ArrayDec( int row, int col, NameTy typ, String name, int size ) {
    this.row = row;
    this.col = col;
    this.typ = typ;
    this.name = name;
    this.size = size;
  }
  
  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }

  public String type() {
    return typ.name() + "[]";
  }

  public String type(boolean includeType) {
    if (includeType) {
      return typ.name() + "[" + size + "]";
    }
    else {
      return typ.name() + "[]";
    }
  }
}
