import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor {

  private class NodeType {
    public String name;
    public Dec def;
    public int level;
  
    public NodeType(String name, Dec def, int level) {
      this.name = name;
      this.def = def;
      this.level = level;
    }
  }

  final static int SPACES = 4;
  final static SimpleDec dummyInt = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "");
  final static SimpleDec dummyBool = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOL), "");
  final static SimpleDec dummyVoid = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.VOID), "");

  private static HashMap<String, ArrayList<NodeType>> symbolTable;
  private static FunctionDec currentFunction;
  private static int currentFuncScope;
  private static boolean hasReturn;

  // info to dictate what scope type to print
  public final static int GENERIC = 0;
  public final static int FUNCTION = 1;
  public final static int IF  = 2;
  public final static int ELSE = 3;
  public final static int WHILE = 4;
  private static Stack<Integer> scopeType = new Stack<Integer>();

  private String getScopeString(String prefix) {
    switch (scopeType.peek()) {
      case GENERIC:
        return prefix + " scope block";
      case FUNCTION:
        return prefix + " scope for function " + currentFunction.func;
      case IF:
        return prefix + " scope for if statement";
      case ELSE:
        return prefix + " scope for else statement";
      case WHILE:
        return prefix + " scope for while statement";
    }

    return " INVALID BLOCK";
  }

  // Intialize the SemanticAnalyzer with a new symbol table, and add the input and output functions at depth 0
  // Which will mean they never get printed but are accessible to all functions
  SemanticAnalyzer() {
    symbolTable = new HashMap<String, ArrayList<NodeType>>();
    FunctionDec input = new FunctionDec(0, 0, new NameTy(0, 0, NameTy.INT), "input", null, null);
    FunctionDec output = new FunctionDec(0, 0, new NameTy(0, 0, NameTy.VOID), "output", new VarDecList(new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "input"), null), null);
    prependToSymbolTable("input", input, -1);
    prependToSymbolTable("output", output, -1);
  }

  private void indent(int level) {
    for (int i = 0; i < level * SPACES; i++)
      System.out.print(" ");
  }

  // Helper function to print a message at a given level
  private void printAtLevel(String message, int level) {
    indent(level);
    System.out.println(message);
  }

  // Helper function to prepend a given def to the symbol table 
  private void prependToSymbolTable(String name, Dec def, int level) {
    symbolTable.putIfAbsent(name, new ArrayList<NodeType>());
    ArrayList<NodeType> nodes = symbolTable.get(name);
    for (int i = 0; i < nodes.size(); i++) {
      NodeType node = nodes.get(i);
      if (node.level == level && node.name.equals(name) && !(node.def instanceof FunctionDec)) {
        reportError(def, "Variable already defined", "Variable \"" + name + "\" already has a definition in the scope and can not be redefined.");
        return;
      }
    }
    nodes.add(0, new NodeType(name, def, level));
  }

  private void printNodeType(NodeType node, int level) {
    if (node.def instanceof FunctionDec) {
      FunctionDec function = (FunctionDec) node.def;
      indent(level);
      System.out.print(function.func + "(");
      if (function.params != null) {
        VarDecList params = function.params;
        while (params != null) {
          System.out.print(params.head.type());
          params = params.tail;
          if (params != null) {
            System.out.print(", ");
          }
        }
      }
      System.out.println(") -> " + function.result.name());
    } else if (node.def instanceof SimpleDec) {
      SimpleDec simple = (SimpleDec) node.def;
      printAtLevel(node.name + " -> " + simple.type(), level);
    } else if (node.def instanceof ArrayDec) {
      ArrayDec array = (ArrayDec) node.def;
      printAtLevel(node.name + " -> " + array.type(), level);
    }
  }

  private void deleteSymbolTableLevelAndPrint(int level) {
    // Iterate through every key 
    for (String key : symbolTable.keySet()) {
      ArrayList<NodeType> nodes = symbolTable.get(key);

      // Get every value, pop and print for each value at the current level. 
      for (int i = 0; i < nodes.size(); i++) {
        // Not at the level we're popping
        // Since we're prepending, the second this happens, break the loop
        if (!(nodes.get(i).level == level)) {
          break;
        }
        NodeType node = nodes.remove(i);

        printNodeType(node, level);
        i--;
      }
    }
  }

  private int getParamListLength(VarDecList list) {
    int length = 0;
    while (list != null) {
      length++;
      list = list.tail;
    }
    return length;
  }

  // Can be made public later if needed elsewhere
  private void reportError(Absyn node, String reason, String message) { 
    if (node != null && node.row >= 0 && node.col >= 0) {
      String errorHeader = "Error on line " + (node.row + 1) + ", column " + (node.col + 1) + ": " + reason + "\n"; 
      System.err.println(errorHeader + message + "\n");
    } else {
      System.err.println("Error: " + reason + "\n" + message + "\n");
    }
  }

  // Tries to add a function to the symbol table. If it's able to do so successfully, returns true.
  private boolean addFunctionToSymbolTable(FunctionDec function, int level) {
    ArrayList<NodeType> existing = symbolTable.get(function.func);
    if (existing == null) {
      prependToSymbolTable(function.func, function, level);
      return true;
    }

    if (function.func.equals("output") || function.func.equals("input")) {
      reportError(function, "Invalid function redeclaration", "Cannot redefine \"" + function.func + "\", overrides built in function.");
      return false;
    }

    for (int i = 0; i < existing.size(); i++) {
      NodeType node = existing.get(i);
      // Verify they have the same name to avoid potential hm conflicts (skip if they don't have the same name / type)
      if (!(node.def instanceof FunctionDec) || !((FunctionDec) node.def).func.equals(function.func)) {
        continue;
      }

      FunctionDec existingFunction = (FunctionDec) node.def;

      VarDecList existingParams = existingFunction.params;
      VarDecList newParams = function.params;
      int paramNum = 1;

      // Verify the types of the parameters match
      while (existingParams != null && newParams != null) {
        if (!existingParams.head.type().equals(newParams.head.type())) {
          reportError(function, "Conflicting parameter types", "Function \"" + function.func + "\" parameter " + paramNum + 
            " has a conflicting type with existing function with the same name.\n" +
            "  Existing type for param " + paramNum + ": " + existingParams.head.type() + "\n" +
            "  New type for param " + paramNum + ": " + newParams.head.type());
          return false;
        }
        paramNum++;
        existingParams = existingParams.tail;
        newParams = newParams.tail;
      }

      // We've iterated through one list but the other still isn't empty; conflicting lengths
      if (existingParams != null || newParams != null) {
        reportError(function, "Conflicting parameter count", "Function \"" + function.func + 
          "\" has a conflicting number of parameters with existing function with the same name.\n" +
          "  Existing number of parameters: " + getParamListLength(existingFunction.params) + "\n" +
          "  New number of parameters: " + getParamListLength(function.params));
        return false;
      }

      // Verify the return types match
      if (!existingFunction.result.name().equals(function.result.name())) {
        reportError(function, "Conflicting function return types", "Function \"" + function.func + 
          "\" has a conflicting return type with existing function with the same name.\n" +
          "  Existing return type: " + existingFunction.result.name() + "\n" +
          "  New return type: " + function.result.name());
        return false;
      }
      
      // So far valid! Now just check for redeclarations
      // If the existing definition doesn't have a body defined, but the new one does
      // just add the body to the old one
      if ((existingFunction.body instanceof NilExp) && !(function.body instanceof NilExp)) {
        existingFunction.body = function.body;
        return true;
      }
      // otherwise if we're redeclaring the body, throw specific error
      else if (!(existingFunction.body instanceof NilExp) && !(function.body instanceof NilExp)) {
        reportError(function, "Function already defined", "Function \"" + function.func + 
        "\" already has a definition in the scope and can not be redefined.");
        return false;
      }
      // otherwise, generic redeclaration error
      else {
        reportError(function, "Function already declared", "Function \"" + function.func + 
        "\" already has a declaration scope and can not be redeclared.");
        return false;
      }
    }

    // Made it through every node without returning; either it's a prototype, the first instance of the body, or a new function
    prependToSymbolTable(function.func, function, level);
    return true;
  }


boolean isInteger(Exp node) {
  if (node == null || node instanceof NilExp) {
    return false;
  }

  return node.dtype.type().equals("int");
}

boolean isBoolean(Exp node) {
  if (node == null || node instanceof NilExp) {
    return false;
  }

  return node.dtype.type().equals("bool") || node.dtype.type().equals("int");
}

boolean isInteger(Dec node) {
  NameTy type = null;
  if (node instanceof FunctionDec) {
    type = ((FunctionDec)node).result;
  }
  else if (node instanceof ArrayDec) {
    type = ((ArrayDec)node).typ;
  }
  else {
    type = ((SimpleDec)node).typ;
  }

  return type.typ == NameTy.INT;
}

boolean isBoolean(Dec node) {
  NameTy type = null;
  if (node instanceof FunctionDec) {
    type = ((FunctionDec)node).result;
  }
  else if (node instanceof ArrayDec) {
    type = ((ArrayDec)node).typ;
  }
  else {
    type = ((SimpleDec)node).typ;
  }

  return type.typ == NameTy.INT || type.typ == NameTy.BOOL;
}


Dec getFromTable(String name) {
  ArrayList<NodeType> nodes = symbolTable.get(name);  
  if (nodes == null) {
    return null;
  }


  for (int i = 0; i < nodes.size(); i++) {
    NodeType node = nodes.get(i);
    if (!node.name.equals(name)) {
      continue;
    }

    return node.def; 
  }

  return null;
}


  /* 
    ------------------------------------------------
      BEGIN VISITOR VISIT FUNCTION IMPLEMENTATIONS 
    ------------------------------------------------
  */
  public void visit(ExpList expList, int level) {
    while (expList != null) {
      expList.head.accept(this, level);
      expList = expList.tail;
    }
  }

  public void visit(AssignExp exp, int level) {
    exp.lhs.accept(this, level);
    exp.rhs.accept(this, level);

    if (exp.lhs.dtype instanceof ArrayDec) {
      reportError(exp, "Invalid assignment", "Cannot assign an expression to an array");
    }
    else if (exp.rhs.dtype instanceof ArrayDec) {
      reportError(exp.rhs, "Invalid assignment", "Cannot assign an array to a variable"); // ???
    }

    // can't just check for equivalence, since int is a subset of bool (and this accounts for RHS being void)
    else if ((isInteger(exp.lhs.dtype) && !isInteger(exp.rhs.dtype)) || (isBoolean(exp.lhs.dtype) && !isBoolean(exp.rhs.dtype))) {
      reportError(exp, "Invalid assignment", "Cannot assign type " + exp.rhs.dtype.type() + " to variable \"" + exp.lhs.variable.name + "\" (type " + exp.lhs.dtype.type() + ")");
    }

    exp.dtype = exp.lhs.dtype;
  }

  public void visit(IfExp exp, int level) {
    exp.test.accept(this, level); // nothing should be declared or added to symbol table here

    scopeType.push(IF);
    exp.thenpart.accept(this, level);
    
    if (exp.elsepart != null && !(exp.elsepart instanceof NilExp) ) {
      scopeType.push(ELSE);
      exp.elsepart.accept(this, level);      
    }

    // isBoolean checks if integer or boolean (it is subset of bool)
    if ( !isBoolean(exp.test) ) {
      reportError(exp, "Invalid test condition", "Test condition in if statement is " + exp.test.dtype.type() + " where int or bool is expected");
    }
    exp.dtype = exp.test.dtype; // is this even needed?
  }

  public void visit(IntExp exp, int level) {
    exp.dtype = dummyInt;
  }

  public void visit(OpExp exp, int level) {
    if (exp.left != null)
      exp.left.accept(this, level);
    exp.right.accept(this, level);

    // if (exp.left != null && exp.left.dtype instanceof ArrayDec) {
    //   reportError(exp.left, "Invalid operand", "Cannot use array \"" + ((ArrayDec)exp.left.dtype).name + "\" as variable");
    // }
    
    // if (exp.right.dtype instanceof ArrayDec) {
    //   reportError(exp.right, "Invalid operand", "Cannot use array \"" + ((ArrayDec)exp.right.dtype).name + "\" as variable");
    // }

    switch( exp.op ) {
      // arithmetic operators
      case OpExp.PLUS:
      case OpExp.MINUS:
      case OpExp.MUL:
      case OpExp.DIV:
        if( !isInteger(exp.left) ) {
          reportError(exp.left, "Invalid operand", "Left operand is type " + exp.left.dtype.type() + " where int is expected");
        }
        
        // fallthrough
      case OpExp.UMINUS:
        if( !isInteger(exp.right) ) {
          reportError(exp.right, "Invalid operand", "Right operand is type " + exp.right.dtype.type() + " where int is expected");
        }
        
        exp.dtype = dummyInt;
        break;
        
      // relational operators
      case OpExp.LT:
      case OpExp.GT:
      case OpExp.LE:
      case OpExp.GE:
      case OpExp.EQ:
      case OpExp.NE:
        if( !isInteger(exp.left) ) {
          reportError(exp.left, "Invalid operand", "Left operand is type " + exp.left.dtype.type() + " where int is expected");
        }
        else if( !isInteger(exp.right) ) {
          reportError(exp.right, "Invalid operand", "Right operand is type " + exp.right.dtype.type() + " where int is expected");
        }
        
        exp.dtype = dummyBool;
        break;
        
        
      // boolean operators
      case OpExp.AND:
      case OpExp.OR:
        if( !isBoolean(exp.left) ) {
          reportError(exp.left, "Invalid operand", "Left operand is type " + exp.left.dtype.type() + " where bool or int is expected");
        }

        // fallthrough
      case OpExp.NOT:
        if( !isBoolean(exp.right) ) {
          reportError(exp.right, "Invalid operand", "Right operand is type " + exp.right.dtype.type() + " where bool or int is expected");
        }
        
        exp.dtype = dummyBool;
        break;
    }
  }

  public void visit(ReturnExp exp, int level) {
    exp.exp.accept(this, level);

    if (level == currentFuncScope) {
      hasReturn = true;
    }

    if (exp.exp.dtype instanceof ArrayDec) {
      reportError(exp.exp, "Invalid return type", "Cannot return an array");
    }
    else if((isInteger(currentFunction) && !isInteger(exp.exp)) || (isBoolean(currentFunction) && !isBoolean(exp.exp)) || (!isBoolean(currentFunction) && isBoolean(exp.exp))) {
      reportError(exp.exp, "Invalid return type", "Cannot return type " + exp.exp.dtype.type() + " from function \"" + currentFunction.func + "\" (return type " + currentFunction.result.name() + ")");
    }

    exp.dtype = (isInteger(currentFunction) ? dummyInt : dummyBool);
  }

  public void visit(VarExp exp, int level) {
    exp.variable.accept(this, level);
    String name = (exp.variable instanceof IndexVar) ? ((IndexVar)exp.variable).name : ((SimpleVar)exp.variable).name;

    Dec type = getFromTable(name);

    if (type == null) {
      reportError(exp, "Missing declaration", "No declaration found in scope (or parent scopes) for variable \"" + name + "\"\n" + "Assuming type to be int.");
      exp.dtype = dummyInt; // is this correct?
      return;
    }

    if (type instanceof FunctionDec) {
      reportError(exp, "Invalid access", "Cannot use function \"" + name + "\" as a variable");
      exp.dtype = dummyInt;
      return;
    }
    else if (exp.variable instanceof SimpleVar && !(type instanceof SimpleDec)){
      // might not be an error, but make sure to set dtype specially so higher scopes can check
      exp.dtype = (VarDec)type;
      return;
    }
    else if (exp.variable instanceof IndexVar && !(type instanceof ArrayDec)) {
      reportError(exp, "Invalid index access", "Cannot access index of non-array variable \"" + name + "\"");
    }

    // must be int or bool, void not possible for non function types (handled in symbol table)
    if (isInteger(type)) {
      exp.dtype = dummyInt;
    }
    else {
      exp.dtype = dummyBool;
    }
  }

  @Override
  public void visit(BoolExp exp, int level) {
    exp.dtype = dummyBool;
  }

  @Override
  public void visit(ArrayDec exp, int level) {
    if (exp.typ.typ == NameTy.VOID) {
      reportError(exp, "Invalid declaration", "Array " + exp.name + " cannot be of type void[]. Changing to int[].");
      exp.typ.typ = NameTy.INT;
    }
    prependToSymbolTable(exp.name, exp, level);
    exp.typ.accept(this, level);
  }

  @Override
  public void visit(CallExp exp, int level) {
    if (exp.args != null) {
      exp.args.accept(this, level);
    }

    Dec type = getFromTable(exp.func);
    
    if (type == null) {
      reportError(exp, "Missing function declaration", "No function declaration found for \"" + exp.func + "\"");
      exp.dtype = dummyInt;
      return;
    }

    if (type instanceof SimpleDec) {
      reportError(exp, "Invalid call", "Called variable \"" + exp.func + "\" is not a function (type " + ((SimpleDec)type).typ.name() + ")");
    }
    else if (type instanceof ArrayDec) {
      reportError(exp, "Invalid call", "Called variable \"" + exp.func + "\" is not a function (type " + ((ArrayDec)type).typ.name() + "[])");
    }
    else {
      FunctionDec function = (FunctionDec)type;
      VarDecList functionParams = function.params;
      ExpList callExps = exp.args;
      int paramNum = 1;
      boolean foundError = false;
  
      // Verify the types of the parameters match
      while (functionParams != null && callExps != null) {
        if (!functionParams.head.type().equals(callExps.head.dtype.type())) {
          reportError(callExps.head, "Invalid parameter", "Call for function \"" + function.func + "\" parameter " + paramNum + 
            " has an unexpected type.\n" +
            "  Expected: " + functionParams.head.type() + "\n" +
            "  Recieved: " + callExps.head.dtype.type());
          foundError = true;
          break;
        }
        paramNum++;
        functionParams = functionParams.tail;
        callExps = callExps.tail;
      }

      if (!foundError && (functionParams != null || callExps != null)) {
        reportError(exp, "Conflicting argument count", "Call expression for \"" + function.func + 
          "\" does not have the correct number of arguments.");
      }
    }


    if (isInteger(type)) {
      exp.dtype = dummyInt;
    }
    else if (isBoolean(type)) {
      exp.dtype = dummyBool;
    }
    else {
      // void
      exp.dtype = dummyVoid;
    }
  }

  @Override
  public void visit(CompoundExp exp, int level) {
    // System.err.println("ENTER " + scopeType.size() + " level: " + level + " type: " + scopeType);
    
    if (scopeType.size() < level) {
      scopeType.push(GENERIC);
    }
    
    printAtLevel(getScopeString("Entering"), level);
    if (exp.decs != null) {
      exp.decs.accept(this, level+1);
    }
    if (exp.exps != null) {
      exp.exps.accept(this, level+1);
    }
    // System.err.println("EXIT " + scopeType.size() + " level: " + level + " type: " + scopeType);
    deleteSymbolTableLevelAndPrint(level+1);
    printAtLevel(getScopeString("Exiting"), level);
    scopeType.pop();

  }

  @Override
  public void visit(DecList exp, int level) {
    printAtLevel("Entering the global scope", level);
    while (exp != null) {
      exp.head.accept(this, level+1);
      
      if (exp.tail == null) {
        // check to make sure the last thing declaired
        // TODO: is it last function? Or last thing declared? that must be main
      }

      exp = exp.tail;
    }

    Dec main = getFromTable("main");
    if (main == null || !(main instanceof FunctionDec)) {
      // just using a dummyInt to print 0 0 line and col
      reportError(null, "Missing main", "Function \"main\" must be declared in the global scope.");
    }

    deleteSymbolTableLevelAndPrint(level+1);
    printAtLevel("Exiting the global scope", level);
  }

  @Override
  public void visit(FunctionDec exp, int level) {
    boolean success = addFunctionToSymbolTable(exp, level);

    // TODO: remove NilExp check if scope messages shouold be output for prototypes
    if (success && !(exp.body instanceof NilExp)) {
      currentFunction = exp;
      hasReturn = false;
      currentFuncScope = level+1;
      scopeType.push(FUNCTION);

      exp.result.accept(this, level);
      if (exp.params != null) {
        exp.params.accept(this, level + 1); // need to set level to one deeper for params
      }
      exp.body.accept(this, level);
      
      // if there is no return and the function is of return type bool or int
      if (hasReturn == false && isBoolean(exp)) {
        reportError(exp, "Function missing return", "Function \"" + exp.func + "\" is non-void, and so must have a return statement at it's top level scope.");
      }
    }
  }

  @Override
  public void visit(IndexVar exp, int level) {
    exp.index.accept(this, level);

    if (!isInteger(exp.index)) {
      reportError(exp, "Invalid index", "Index type is " + exp.index.dtype.type() + " where int is expected");
    }
  }

  @Override
  public void visit(NameTy exp, int level) {
  }

  @Override
  public void visit(SimpleDec exp, int level) {
    if (exp.typ.typ == NameTy.VOID) {
      reportError(exp, "Invalid declaration", "Variable " + exp.name + " cannot be of type void. Changing to int.");
      exp.typ.typ = NameTy.INT;
    }
    prependToSymbolTable(exp.name, exp, level);
    exp.typ.accept(this, level);
  }

  @Override
  public void visit(SimpleVar exp, int level) {
  }

  @Override
  public void visit(VarDecList exp, int level) {
    while (exp != null && exp.head != null) {
      exp.head.accept(this, level);
      exp = exp.tail;
    }
  }

  @Override
  public void visit(WhileExp exp, int level) {
    scopeType.push(WHILE);
    exp.test.accept(this, level);
    exp.body.accept(this, level);

    // isBoolean checks if integer or boolean (it is subset of bool)
    if ( !isBoolean(exp.test) ) {
      reportError(exp, "Invalid test condition", "Test condition in while statement is " + exp.test.dtype.type() + " where int or bool is expected");
    }
    exp.dtype = exp.test.dtype; // is this even needed?
  }

  @Override
  public void visit(NilExp exp, int level) {
    exp.dtype = dummyVoid;
  }

}
