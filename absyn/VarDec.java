package absyn;

abstract public class VarDec extends Dec {
  public int offset;
  public int nestLevel;
  
  public abstract String type();
}
