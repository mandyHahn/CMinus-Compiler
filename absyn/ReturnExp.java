package absyn;

public class ReturnExp extends Exp {
  public Exp exp;

  // Note: exp can be NilExp
  public ReturnExp( int row, int col, Exp exp ) {
    this.row = row;
    this.col = col;
    this.exp = exp;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}
