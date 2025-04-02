import absyn.*;

public class CodeGenerator implements AbsynVisitor {
	final int ac = 0;
	final int ac1 = 1;
	final int fp = 5;
	final int gp = 6;
	final int pc = 7;
	
	final int ofpFO = 0;
	final int retFO = -1;
	final int initFO = -2;
	
	int mainEntry, globalOffset, frameOffset;
	int emitLoc, highEmitLoc;
	int outputEntry, inputEntry;

	// used ONLY for SimpleVar and IndexVar, in order to generate unique behaviour in each visit
	VarDec reference = null;

	// add constructor and all emitting routines
	CodeGenerator() { }
	
	private void emitRO( String op, int r, int s, int t, String comment ) {
		System.out.print(String.format("%3d: %5s %d, %d, %d", emitLoc, op, r, s, t));
		System.out.println("\t" + comment);
		emitLoc++;
		if ( highEmitLoc < emitLoc ) {
			highEmitLoc = emitLoc;
		}
	} 

	private void emitRM( String op, int r, int d, int s, String comment ) {
		System.out.print(String.format("%3d: %5s %d, %d(%d)", emitLoc, op, r, d, s));
		System.out.println("\t" + comment);
		emitLoc++;
		if ( highEmitLoc < emitLoc ) {
			highEmitLoc = emitLoc;
		}
	} 

	private void emitRM_Abs( String op, int r, int a, String comment ) {
		System.out.print(String.format("%3d: %5s %d, %d(%d)", emitLoc, op, r, a - (emitLoc + 1), pc));
		System.out.println("\t" + comment);
		emitLoc++;
		if ( highEmitLoc < emitLoc ) {
			highEmitLoc = emitLoc;
		}
	} 

	private int emitSkip( int distance ) {
		int i = emitLoc;
		emitLoc += distance;
		if (highEmitLoc < emitLoc ) {
			highEmitLoc = emitLoc;
		}

		return i;
	}

	private void emitComment( String comment ) {
		System.out.println("* " + comment);
	}

	private void emitBackup( int loc ) {
		if (loc > highEmitLoc ) {
			emitComment( "BUG in emitBackup" );
		}
		emitLoc = loc; 
	}

	private void emitRestore() {
		emitLoc = highEmitLoc;
	}

	public void visit(Absyn trees) {
		// generate prelude
		emitComment("prelude");
		emitRM("LD", gp, 0, 0, "load gp with maxaddr");
		emitRM("LDA", fp, 0, gp, "copy gp to fp");
		emitRM("ST", ac, 0, 0, "clear content at loc 0");

		// prep backpatching for i/o routines
		int savedLoc = emitSkip(1);

		// generate i/o routines
		emitComment("code for input routine");
		inputEntry = emitLoc;
		emitRM("ST", ac, retFO, fp, "store return");
		emitRO("IN", 0, 0, 0, "input");
		emitRM("LD", pc, retFO, fp, "return to caller");

		emitComment("code for output routine");
		outputEntry = emitLoc;
		emitRM("ST", ac, retFO, fp, "store return");
		emitRM("LD", ac, initFO, fp, "load output value");
		emitRO("OUT", ac, 0, 0, "output");
		emitRM("LD", pc, retFO, fp, "return to caller");

		// emit backpatching for i/o routines
		int savedLoc2 = emitSkip(0);
		emitBackup(savedLoc);
		emitRM_Abs("LDA", pc, savedLoc2, "jump around i/o code");
		emitRestore();

		// make a request to the visit method for DecList
		emitComment("start code generation");
		trees.accept(this, 0, false);

		// generate finale
		emitComment("finale");
		emitRM("ST", fp, globalOffset + ofpFO, fp, "push ofp");
		emitRM("LDA", fp, globalOffset, fp, "push frame");
		emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
		emitRM_Abs("LDA", pc, mainEntry, "jump to main loc");
		emitRM("LD", fp, ofpFO, fp, "pop frame");
		emitRO("HALT", 0, 0, 0, "stop execution");
	}


	///////////////////////////////////
	// 		START VISIT METHODS		 //
	///////////////////////////////////
	
	@Override
	public void visit(ExpList expList, int offset, boolean flag) {
		while (expList != null) {
			expList.head.accept(this, offset, flag);
			expList = expList.tail;
		}
	}

	@Override
	public void visit(AssignExp exp, int offset, boolean flag) {
		exp.lhs.accept(this, offset - 1, true);
		exp.rhs.accept(this, offset - 2, false);

		emitRM("LD", ac, offset - 1, fp, "load the assignment address into ac");
		emitRM("LD", ac1, offset - 2, fp, "load the result of the rhs into ac1");
		emitRM("ST", ac1, 0, ac, "store the value in ac1 in the address in ac");
		emitRM("ST", ac1, offset, fp, "store the value in ac1 to the local temporary");
	}

	@Override
	public void visit(IntExp exp, int offset, boolean flag) {
		emitRM("LDC", ac, exp.value, 0, "load the literal int in ac");
		emitRM("ST", ac, offset, fp, "store the literal int from ac into local temporary");
	}


	@Override
	public void visit(BoolExp exp, int offset, boolean flag) {
		emitRM("LDC", ac, (exp.value ? 1 : 0), 0, "load the literal bool in ac");
		emitRM("ST", ac, offset, fp, "store the literal bool from ac into local temporary");
	}


	@Override
	public void visit(OpExp exp, int offset, boolean flag) {
		if (exp.left == null || exp.left instanceof NilExp) {
			emitComment("no lhs, treat as 0");
			emitRM("LDC", ac, 0, 0, "load the literal 0 in ac");
			emitRM("ST", ac, offset, fp, "store the literal int from ac into local temporary");
		}
		else {
			exp.left.accept(this, offset - 1, flag);
		}

		exp.right.accept(this, offset - 2, flag);

		emitRM("LD", ac, offset - 1, fp, "store lhs in ac");
		emitRM("LD", ac1, offset - 2, fp, "store rhs in ac1");

		switch (exp.op) {
			case OpExp.PLUS:
				emitRO("ADD", ac, ac, ac1, "add ac and ac1, store result in ac");
				break;

			case OpExp.UMINUS:
			case OpExp.MINUS:
				emitRO("SUB", ac, ac, ac1, "subtract ac and ac1, store result in ac");
				break;

			case OpExp.MUL:
				emitRO("MUL", ac, ac, ac1, "multiply ac and ac1, store result in ac");
				break;

			case OpExp.DIV:
				emitRO("DIV", ac, ac, ac1, "divide ac and ac1, store result in ac");
				break;
				
				
			// relational operators
			case OpExp.LT:
				emitRO("SUB", ac, ac, ac1, "LT: sub ac and ac1 to determine which is larger");
				emitRM("JGE", ac, 2, pc, "LT: jump to else part if lhs >= rhs");

				// if lhs < rhs
				emitRM("LDC", ac, 1, 0, "lhs < rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for <");
				
				// if lhs >= rhs
				emitRM("LDC", ac, 0, 0, "lhs < rhs is false, set ac 0");
				break;

			case OpExp.GT:
				emitRO("SUB", ac, ac, ac1, "GT: sub ac and ac1 to determine which is larger");
				emitRM("JLE", ac, 2, pc, "GT: jump to else part if lhs <= rhs");

				// if lhs > rhs
				emitRM("LDC", ac, 1, 0, "lhs > rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for >");
				
				// if lhs <= rhs
				emitRM("LDC", ac, 0, 0, "lhs > rhs is false, set ac 0");
				break;

			case OpExp.LE:
				emitRO("SUB", ac, ac, ac1, "LE: sub ac and ac1 to determine which is larger");
				emitRM("JGT", ac, 2, pc, "LE: jump to else part if lhs > rhs");

				// if lhs <= rhs
				emitRM("LDC", ac, 1, 0, "lhs <= rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for <=");
				
				// if lhs > rhs
				emitRM("LDC", ac, 0, 0, "lhs <= rhs is false, set ac 0");
				break;

			case OpExp.GE:
				emitRO("SUB", ac, ac, ac1, "GE: sub ac and ac1 to determine which is larger");
				emitRM("JLT", ac, 2, pc, "GE: jump to else part if lhs < rhs");

				// if lhs >= rhs
				emitRM("LDC", ac, 1, 0, "lhs >= rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for >=");
				
				// if lhs < rhs
				emitRM("LDC", ac, 0, 0, "lhs >= rhs is false, set ac 0");
				break;

			case OpExp.EQ:
				emitRO("SUB", ac, ac, ac1, "EQ: sub ac and ac1 to determine which is larger");
				emitRM("JNE", ac, 2, pc, "EQ: jump to else part if lhs != rhs");

				// if lhs == rhs
				emitRM("LDC", ac, 1, 0, "lhs == rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for ==");
				
				// if lhs != rhs
				emitRM("LDC", ac, 0, 0, "lhs == rhs is false, set ac 0");
				break;

			case OpExp.NE:
				emitRO("SUB", ac, ac, ac1, "NE: sub ac and ac1 to determine which is larger");
				emitRM("JEQ", ac, 2, pc, "NE: jump to else part if lhs == rhs");

				// if lhs != rhs
				emitRM("LDC", ac, 1, 0, "lhs != rhs is true, set ac 1");
				emitRM("LDA", pc, 1, pc, "jump past else branch for !=");
				
				// if lhs == rhs
				emitRM("LDC", ac, 0, 0, "lhs != rhs is false, set ac 0");
				break;
				
			case OpExp.AND:
				// AND is the same thing as a * b
				emitRO("MUL", ac, ac, ac1, "AND ac and ac1, store result in ac");
				break;
				
			case OpExp.OR:
				// OR is the same thing as |a| + |b| (use a^2 and b^2 instead of abs)
				emitRO("MUL", ac, ac, ac, "square the lhs to ensure positive (OR using add)");
				emitRO("MUL", ac1, ac1, ac1, "square the rhs to ensure positive (OR using add)");
				emitRO("ADD", ac, ac, ac1, "OR ac and ac1, store result in ac");
				break;

			case OpExp.NOT:
				// Note: the value to NOT is loaded into ac1, and result is expected in ac
				emitRM("JEQ", ac1, 2, pc, "jump to else if ac1 == 0");

				// if 1, change to 0
				emitRM("LDC", ac, 0, 0, "then branch: if ac1 != 0, set ac 0");
				emitRM("LDA", pc, 1, pc, "jump past else branch for NOT operation");
				
				// if 0, change to 1
				emitRM("LDC", ac, 1, 0, "else branch: if ac1 == 0, set ac 1");
				break;
		}
			
		emitRM("ST", ac, offset, fp, "store result from ac into local temporary");
	}
		
	@Override
	public void visit(ReturnExp exp, int offset, boolean flag) {
		if (exp.exp != null && !(exp.exp instanceof NilExp)) {
			exp.exp.accept(this, offset, flag);
		}

		emitRM("LD", ac, offset, fp, "save return value to ac");
		emitRM("LD", pc, retFO, fp, "return to caller from return statement");
	}

	@Override
	public void visit(CallExp exp, int offset, boolean flag) {

		int localOffset = initFO;
		emitComment("start calling procedure for " + exp.func);

		if (exp.args != null) {
			// instead of calling into ExpList, need custom behaviour???
			// this seems wrong but I have no clue how to do this otherwise
			ExpList args = exp.args;
			
			while (args != null) {
				args.head.accept(this, offset + localOffset, flag);
				args = args.tail;

				// all possible Exp statements here should end with a "result value" in ac
				// this might not be necessary? 
				emitRM("ST", ac, offset + localOffset, fp, "store result of parameter from ac into stack");

				localOffset -= 1;
			}
		}

				
		emitRM("ST", fp, offset + ofpFO, fp, "store current fp");
		emitRM("LDA", fp, offset, fp, "push new frame (update fp to new frame)");
		emitRM("LDA", ac, 1, pc, "save address to return from function in ac");
		
		int funcAddr = exp.reference.funAddr.intValue();

		if (exp.func.equals("output")) {
			funcAddr = outputEntry;
		}
		else if (exp.func.equals("input")) {
			funcAddr = inputEntry;
		}
		
		if (funcAddr == 0) {
			// if the function hasn't yet been defined
			int bLoc = emitSkip(1);
			emitComment("skipping jump to function " + exp.func + " (not yet defined)");
			exp.reference.backpatchLocs.add(bLoc);
		} else {
			emitRM_Abs("LDA", pc, funcAddr, "jump to function entry");
		}

		// This runs after returning from function
		emitRM("LD", fp, ofpFO, fp, "pop the called functions frame");

		// if non-void, save return value to caller's frame
		if ( exp.reference.result.typ != NameTy.VOID ) {
			emitRM("ST", ac, offset, fp, "save return value to the caller's stack frame");
		}

	}


	@Override
	public void visit(CompoundExp exp, int offset, boolean flag) {
		// this and the line at the end *should* deal with nested scope??
		int oldFrameOffset = frameOffset; 

		// declarations will increase frameOffset for each allocated variable
		if (exp.decs != null) {
			exp.decs.accept(this, frameOffset, flag);
		}

		// since frameOffset was increased during declarations, use it as the beginning of local temps
		if (exp.exps != null) {
			exp.exps.accept(this, frameOffset, flag);
		}

		frameOffset = oldFrameOffset;
	}


	@Override
	public void visit(DecList exp, int offset, boolean flag) {
		while (exp != null) {

			if (exp.head instanceof SimpleDec) {
				SimpleDec var = (SimpleDec)(exp.head);
				var.offset = globalOffset;
				var.nestLevel = 0;
				globalOffset -= 1;
			}
			else if (exp.head instanceof ArrayDec) {
				ArrayDec var  = (ArrayDec)(exp.head);
				var.offset = globalOffset - var.size + 1;
				var.nestLevel = 0;
				
				// This case should never happen; throw an error and exit if it does.
				if (var.size == 0) {
					System.err.println("Error: array " + var.name + " has size 0, cannot be declared");
					System.exit(1);
				}
				emitRM("LDC", ac, var.size, 0, "Load the size of the array into data register");
				emitRM("ST", ac, globalOffset - var.size, gp, "Load the size of the array into the proper spot in memory");
				globalOffset -= (var.size + 1);
			}
			else {
				// prep backpatching for functions
				int savedLoc = emitSkip(1);
	
				exp.head.accept(this, offset, flag);
				
				// emit backpatching for functions
				int savedLoc2 = emitSkip(0);
				emitBackup(savedLoc);
				emitRM_Abs("LDA", pc, savedLoc2, "jump around function");
				emitRestore();
			}

			exp = exp.tail;
		}
	}


	@Override
	public void visit(FunctionDec exp, int offset, boolean flag) {
		if (exp.body == null || exp.body instanceof NilExp ) {
			System.err.println("Error: function " + exp.func + " has no definition for the prototype.");
			System.exit(1);
			return;
		}
		else if (exp.funAddr.intValue() != 0) {
			emitComment("skipping function redelcaration for " + exp.func);
			// do nothing if already defined (from prototype)
			return;
		}	

		int oldFrameOffset = frameOffset;
		frameOffset = initFO;

		emitComment("");	// for extra space
		emitComment("code for " + exp.func);
		if (exp.func.equals("main")) {
			mainEntry = emitLoc;
		}
		
		exp.funAddr.set(emitLoc);

		for (int loc : exp.backpatchLocs) {
			int savedLoc = emitSkip(0);
			emitBackup(loc);
			emitRM_Abs("LDA", pc, savedLoc, "jump to function entry");
			emitRestore();
		}
		
		emitRM("ST", ac, retFO, fp, "store return");

		if (exp.params != null) {
			exp.params.accept(this, frameOffset, flag); 
		}

		exp.body.accept(this, frameOffset, flag);
		emitRM("LD", pc, retFO, fp, "return to caller");

		frameOffset = oldFrameOffset;
	}

	
	@Override
	public void visit(SimpleDec exp, int offset, boolean flag) {
		// only used for local declarations, declare global variables in DecList

		emitComment("allocated space for " + exp.name + " (" + frameOffset + ")");
		exp.nestLevel = 1;
		exp.offset = frameOffset;
		
		frameOffset -= 1;
	}
		
	@Override
	public void visit(ArrayDec exp, int offset, boolean flag) {		
		// Local
		exp.nestLevel = 1;
		
		if (exp.size != 0) {
			exp.offset = frameOffset - exp.size + 1;
			emitRM("LDC", ac, exp.size, 0, "Load the size of the array into data register");
			emitRM("ST", ac, frameOffset - exp.size, fp, "Load the size of the array into the proper spot in memory");
		} else {
			exp.offset = frameOffset;
		}
		
		frameOffset -= (exp.size + 1);
	}

	@Override
	public void visit(VarExp exp, int offset, boolean flag) {
		// set up the reference for the child SimpleVar or IndexVar
		reference = (VarDec)exp.reference;
		exp.variable.accept(this, offset, flag);
	}
	
	@Override
	public void visit(SimpleVar exp, int offset, boolean flag) {
		int from = (reference.nestLevel == 0) ? gp : fp;
		
		if (flag || (reference instanceof ArrayDec && ((ArrayDec)reference).size > 0)) {
			emitRM("LDA", ac, reference.offset, from, "load address of var in ac");
			emitRM("ST", ac, offset, fp, "store address of var from ac into temporary");
			return;
		}
		
		emitRM("LD", ac, reference.offset, from, "load value of var in ac");
		emitRM("ST", ac, offset, fp, "store value of var from ac into temporary");
	}
	
	public void checkBounds(int offset) {
		emitRM("ST", ac, offset, fp, "prep for persistent registers");

		// Below bounds checking
		emitRM("JGE", ac1, 3, pc, "jump to error if index < 0");
		// These will be jumped over if the index is greater than 0
		emitRM("LDC", ac, -1000000, 0, "load error code if below bounds");
		emitRO("OUT", ac, 0, 0, "output error code for below bounds");
		emitRO("HALT", 0, 0, 0, "halt execution");

		// Above bounds checking
		emitRM("LD", ac, -1, ac, "load the size of the array into ac");
		emitRO("SUB", ac, ac, ac1, "subtract the index from size to check if it is greater than size");
		emitRM("JGT", ac, 3, pc, "jump to error if index > size");
		// These will be jumped over if the index is not above bounds
		emitRM("LDC", ac, -2000000, 0, "load error code if above bounds");
		emitRO("OUT", ac, 0, 0, "output error code for above bounds");
		emitRO("HALT", 0, 0, 0, "halt execution");

		emitRM("LD", ac, offset, fp, "load the memory location back into ac");
	}


	@Override
	public void visit(IndexVar exp, int offset, boolean flag) {
		ArrayDec localReference = (ArrayDec)reference;
		int from = (localReference.nestLevel == 0) ? gp : fp;
		exp.index.accept(this, offset, false);
		emitRM("LD", ac1, offset, fp, "load the index into ac1");

		if (localReference.size > 0) {
			emitRM("LDA", ac, localReference.offset, from, "load the base address of the array into ac");
		} else {
			// If size is 0, it will always be from fp
			emitRM("LD", ac, localReference.offset, fp, "load the base address of the array from stack into ac");
		}

		checkBounds(offset);

		emitRO("ADD", ac, ac, ac1, "add the index to the base address to get the memory location to pull from");
		if (!flag) {
			emitRM("LD", ac, 0, ac, "load the value of the array at the index into ac1");
		}
		emitRM("ST", ac, offset, fp, "store the result in ac into the local temporary");
		emitRM("LD", ac, offset, fp, "load the value of the array at the index into ac");
	}
	
	@Override
	public void visit(VarDecList exp, int offset, boolean flag) {
		while (exp != null && exp.head != null) {
			exp.head.accept(this, frameOffset, flag);
			exp = exp.tail;
		}
	}

	@Override
	public void visit(IfExp exp, int offset, boolean flag) {
		exp.test.accept(this, offset, flag);
		emitRM("LD", ac, offset, fp, "load the result of the if expression into ac");
		int savedLoc = emitSkip(1); // Backpatching for the if statement
		exp.thenpart.accept(this, offset, flag);
		
		if (exp.elsepart != null && !(exp.elsepart instanceof NilExp)) {
			// There is an else part -- need another JNE to jump past the else part if present
			int savedLoc2 = emitSkip(1);
			int beginOfElse = emitSkip(0);
			emitBackup(savedLoc);
			emitRM_Abs("JEQ", ac, beginOfElse, "jump to else statement if test is false");
			emitRestore();
			exp.elsepart.accept(this, offset, flag);
			int endOfElse = emitSkip(0);
			emitBackup(savedLoc2);
			emitRM_Abs("LDA", pc, endOfElse, "Jump to end of else statement");
			emitRestore();
		} else {
			// There is no else part -- Jump to the end of the if statement using backpatching
			int endOfIf = emitSkip(0);
			emitBackup(savedLoc);
			emitRM_Abs("JEQ", ac, endOfIf, "jump past if statement if test is false");
			emitRestore();
		}
	}

	@Override
	public void visit(WhileExp exp, int offset, boolean flag) {
		emitComment(" ------ Begin while statement ------");
		int whileStart = emitSkip(0);
		exp.test.accept(this, offset, flag);
		emitRM("LD", ac, offset, fp, "load the result of the while expression into ac");
		int savedLoc = emitSkip(1); // Backpatching for the while statemen
		exp.body.accept(this, offset, flag);
		emitRM_Abs("LDA", pc, whileStart, "jump back to the start of the while statement unconditionally");
		int endOfWhile = emitSkip(0);
		emitBackup(savedLoc);
		emitRM_Abs("JEQ", ac, endOfWhile, "jump to end of while statement if test is false");
		emitRestore();
	}


	@Override
	public void visit(NilExp exp, int offset, boolean flag) {
		// TODO: Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visit'");
	}

		
	@Override
	public void visit(NameTy exp, int offset, boolean flag) {
		// I don't think this will ever be visited because we performed typechecking already
	}
}
