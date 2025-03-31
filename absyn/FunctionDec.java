package absyn;

import java.util.ArrayList;

public class FunctionDec extends Dec {
  public class MutableInt {
    private int value = 0;

    public MutableInt(int value) {
      this.value = value;
    }

    public void set(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }
  }

  public NameTy result;
  public String func;
  public VarDecList params;
  public Exp body;
  public MutableInt funAddr = new MutableInt(0);
  
  public ArrayList<Integer> backpatchLocs = new ArrayList<Integer>();

  // Note: body can be NilExp
  public FunctionDec( int row, int col, NameTy result, String func, VarDecList params, Exp body ) {
    this.row = row;
    this.col = col;
    this.result = result;
    this.func = func;
    this.params = params;
    this.body = body;
  }
  
  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}
