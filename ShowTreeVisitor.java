import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );

    // TODO: can be NilExp -- how does this need to change
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.MUL:
        System.out.println( " * " );
        break;
      case OpExp.DIV:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " == " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.UMINUS:
        System.out.println( " - " );
        break;
      case OpExp.LE:
        System.out.println( " <= " );
        break;
      case OpExp.GE:
        System.out.println( " >= " );
        break;
      case OpExp.NOT:
        System.out.println( " ~ " );
        break;
      case OpExp.NE:
        System.out.println( " != " );
        break;
      case OpExp.AND:
        System.out.println( " && " );
        break;
      case OpExp.OR:
        System.out.println( " || " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
      exp.left.accept( this, level );

    exp.right.accept( this, level );
  }

  public void visit( ReturnExp exp, int level ) {
    indent( level );
    System.out.println( "ReturnExp:" );
    level++;
    exp.exp.accept( this, level );
  }

  public void visit( VarExp exp, int level ) {
    // indent( level );
    // System.out.println( "VarExp:" );
    // level++;
    exp.variable.accept( this, level);;
  }


  @Override
  public void visit(BoolExp exp, int level) {
    indent( level );
    System.out.println( "VarExp: " + exp.value );
  }

  @Override
  public void visit(ArrayDec exp, int level) {
    indent( level );
    System.out.print( "ArrayDec: " + exp.name + " " + exp.size + " ");
    // level++;
    exp.typ.accept( this, level ); // prints on same line then adds newline
  }

  @Override
  public void visit(CallExp exp, int level) {
    indent( level );
    System.out.println( "CallExp: " + exp.func );
    level++;
    if (exp.args != null) {
      exp.args.accept(this, level);
    }
  }

  @Override
  public void visit(CompoundExp exp, int level) {
    // indent( level );
    // System.out.println( "CompoundExp:" );
    // level++;
    if (exp.decs != null) {
      exp.decs.accept(this, level);
    }
    if (exp.exps != null) {
      exp.exps.accept(this, level);
    }
  }

  @Override
  public void visit(DecList exp, int level) {
    while( exp != null ) {
      exp.head.accept( this, level );
      exp = exp.tail;
    } 
  }

  @Override
  public void visit(FunctionDec exp, int level) {
    indent( level );
    System.out.print( "FunctionDec: " + exp.func + " ");
    exp.result.accept(this, level); // prints on same line then adds newline
    level++;

    if (exp.params != null) {
      exp.params.accept(this, level); 
    }
    exp.body.accept(this, level);
  }

  @Override
  public void visit(IndexVar exp, int level) {
    indent( level );
    System.out.println( "IndexVar: " + exp.name );
    level++;
    exp.index.accept(this, level);
  }

  @Override
  public void visit(NameTy exp, int level) {
    // indent( level );
    // System.out.print( "NameTy: ");
    switch( exp.typ ) {
      case NameTy.BOOL:
        System.out.println( "(type = bool)" );
        break;
      case NameTy.INT:
        System.out.println( "(type = int)" );
        break;
      case NameTy.VOID:
        System.out.println( "(type = void)" );
        break;
      default:
        System.out.println( "Unrecognized type at line " + exp.row + " and column " + exp.col);
    }
  }

  @Override
  public void visit(SimpleDec exp, int level) {
    indent( level );
    System.out.print( "SimpleDec: " + exp.name + " ");
    // level++;
    exp.typ.accept(this, level); // prints on same line then adds newline
  }

  @Override
  public void visit(SimpleVar exp, int level) {
    indent( level );
    System.out.println( "SimpleVar: " + exp.name );
  }

  @Override
  public void visit(VarDecList exp, int level) {
    while( exp != null && exp.head != null ) {
      exp.head.accept( this, level );
      exp = exp.tail;
    } 
  }

  @Override
  public void visit(WhileExp exp, int level) {
    indent( level );
    System.out.println( "WhileExp:" );
    level++;
    exp.test.accept(this, level);
    exp.body.accept(this, level);
  }

  @Override
  public void visit(NilExp exp, int level) {
    // indent( level );
    // System.out.println( "nil" );
  }

}
