package absyn;

public class CallExp extends Exp {
  public String func;
  public ExpList args;
  public FunctionDec reference;

  public CallExp( int row, int col, String func, ExpList args) {
    this.row = row;
    this.col = col;
    this.func = func;
    this.args = args;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}
