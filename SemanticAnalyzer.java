import java.util.ArrayList;
import java.util.HashMap;

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
  private static HashMap<String, ArrayList<NodeType>> symbolTable;

  // Intialize the SemanticAnalyzer with a new symbol table, and add the input and output functions at depth 0
  // Which will mean they never get printed but are accessible to all functions
  SemanticAnalyzer() {
    symbolTable = new HashMap<String, ArrayList<NodeType>>();
    FunctionDec input = new FunctionDec(0, 0, new NameTy(0, 0, NameTy.VOID), "input", new VarDecList(null, null), null);
    FunctionDec output = new FunctionDec(0, 0, new NameTy(0, 0, NameTy.VOID), "output", new VarDecList(null, null), null);
    prependToSymbolTable("input", input, 0);
    prependToSymbolTable("output", output, 0);
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
    symbolTable.get(name).add(0, new NodeType(name, def, level));
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

  

  /* 
    --------------------------------
      BEGIN VISITOR VISIT FUNCTION IMPLEMENTATIONS 
    --------------------------------
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
  }

  public void visit(IfExp exp, int level) {
    printAtLevel("Entering scope for if statement", level);
    exp.test.accept(this, level + 1);
    exp.thenpart.accept(this, level + 1);
    deleteSymbolTableLevelAndPrint(level + 1);
    printAtLevel("Leaving the if statement scope", level);

    if (exp.elsepart != null) {
      printAtLevel("Entering scope for else statement", level);
      exp.elsepart.accept(this, level + 1);
      deleteSymbolTableLevelAndPrint(level + 1);
      printAtLevel("Exiting the scope for else statement", level);
    }
  }

  public void visit(IntExp exp, int level) {
  }

  public void visit(OpExp exp, int level) {
    if (exp.left != null)
      exp.left.accept(this, level);
    exp.right.accept(this, level);
  }

  public void visit(ReturnExp exp, int level) {
    exp.exp.accept(this, level);
  }

  public void visit(VarExp exp, int level) {
    exp.variable.accept(this, level);
  }

  @Override
  public void visit(BoolExp exp, int level) {
  }

  @Override
  public void visit(ArrayDec exp, int level) {
    if (exp.typ.typ == NameTy.VOID) {
      System.err.println("Error: Variable " + exp.name + " cannot be of type void. Changing to int.");
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
  }

  @Override
  public void visit(CompoundExp exp, int level) {
    if (exp.decs != null) {
      exp.decs.accept(this, level);
    }
    if (exp.exps != null) {
      exp.exps.accept(this, level);
    }
  }

  @Override
  public void visit(DecList exp, int level) {
    printAtLevel("Entering the global scope", level);
    while (exp != null) {
      exp.head.accept(this, level + 1);
      exp = exp.tail;
    }
    deleteSymbolTableLevelAndPrint(level + 1);
    printAtLevel("Exiting the global scope", level);
  }

  @Override
  public void visit(FunctionDec exp, int level) {
    printAtLevel("Entering the scope for function " + exp.func, level);
    prependToSymbolTable(exp.func, exp, level);
    exp.result.accept(this, level + 1);
    if (exp.params != null) {
      exp.params.accept(this, level + 1);
    }
    exp.body.accept(this, level + 1);
    deleteSymbolTableLevelAndPrint(level + 1);
    printAtLevel("Leaving the function scope", level);
  }

  @Override
  public void visit(IndexVar exp, int level) {
    exp.index.accept(this, level);
  }

  @Override
  public void visit(NameTy exp, int level) {
  }

  @Override
  public void visit(SimpleDec exp, int level) {
    if (exp.typ.typ == NameTy.VOID) {
      System.err.println("Error: Variable " + exp.name + " cannot be of type void. Changing to int.");
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
    printAtLevel("Entering the scope for while statement", level);
    exp.test.accept(this, level + 1);
    exp.body.accept(this, level + 1);
    deleteSymbolTableLevelAndPrint(level + 1);
    printAtLevel("Leaving the scope for while statement", level);
  }

  @Override
  public void visit(NilExp exp, int level) {
  }

}
