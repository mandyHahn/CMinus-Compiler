# CMinus Compiler 
Created for the Winter 2025 Compilers (CIS*4650) course by Amanda Hahn and Nicholas Waller. Written in Java, using JFlex and CUP. Compiled code is intended to be run in [Kenneth C. Louden's Tiny Machine Simulator](https://www.cs.sjsu.edu/~louden/cmptext/).

## Contact Info
Nicholas Waller (nwaller@uoguelph.ca / nicholaswaller117@gmail.com)
Amanda Hahn (ahahn01@uoguelph.ca / mandy@hahnmail.ca)

## Running the Program
The following dependencies are needed to use this compiler:
- JFlex 1.7.0 ([link](https://www.jflex.de/changelog.html))
- CUP v0.11b ([link](https://www2.cs.tum.edu/projects/cup/))

On a system containing the necessary dependencies, type `make` into the console, which will compile the code as necessary for execution. Note: you may need to update the CLASSPATH variable at the top of the Makefile (as well as in the following commands) with the correct location of your cup.jar file.

Below is listed how to run each step of the compiler. You only need to run the target final step, as all intermediate steps are performed automatically (eg for assembly code generation, syntactic and semantic analysis are performed automatically first). 

### For syntactic analysis:
- To test 1.cm, run `java -cp /usr/share/java/cup.jar:. CM -a tests/1.cm`
- To test any file generically, run `java -cp /usr/share/java/cup.jar:. CM -a {TEST_FILE_DIR}`
- This will output a file in the directory where the test file is located called `{TEST_FILE_NAME}.abs`

### For semantic analysis:
- Use the commands above, but replace -a with -s
- For example, to test 1.cm, run `java -cp /usr/share/java/cup.jar:. CM -s tests/1.cm`
- To test any file generically, run `java -cp /usr/share/java/cup.jar:. CM -s {TEST_FILE_LOCATION}`
- This will output a file in the directory where the test file is located called `{TEST_FILE_NAME}.sym`
  - For tests/1.cm, a file will be created in the `tests` directory called `1.sym`

### For assembly code generation:
- Use the commands above, but replace -s with -c
- For example, to test 1.cm, run `java -cp /usr/share/java/cup.jar:. CM -c tests/1.cm`
- To test any file generically, run `java -cp /usr/share/java/cup.jar:. CM -c {TEST_FILE_LOCATION}`
- This will output a file in the directory where the test file is located called `{TEST_FILE_NAME}.tm`
  - For tests/1.cm, a file will be created in the `tests` directory called `1.tm`

## Testing and Explanation
Running the program is simple. 

All tests are in the directory "tests", including the mandatory `1.cm`, `2.cm`, ... required for the assignment. 

### Checkpoint 3
Modifications have been made since checkpoint 2 that now allow you to run assembly code. 
This code can run, as far as we know, any valid .cm file. It will report any lexical, syntactic, or type errors before outputting the .tm file, such that the programmer will know what error has taken place. 

The .tm file can then be run using the TMSimulator provided during the class. 

### Checkpoint 2
Modifications have been made since checkpoint 1 that make it so there is now a fully fleshed out CM class that is run off of command line arguments, which outputs the .abs file or .sym file, depending on the command-line argument provided, as described in the assignment description. 
When running our CM class with -a, it will produce a .abs file, which is the tree representation of the abstract syntax tree for the program, while running with -s will produce the .sym file, which is a visual representation of our symbol table and scoping done for Milestone 2. 

As of right now, our program will run through the syntactic analyzer and check if anything failed. If anything fails at the step of syntactic analysis, the program will terminate. The program will then run through our SemanticAnalyzer and will report problems for many different problems, if present. 

For example, our program handles errors including but not limited to:
- Return types of invalid types
- Missing return for non-void function
- Function body already defined
- Different function headers (prototype vs prototype, prototype vs definition)
- Invalid test conditions (e.g. `if (1)`) 
- Variable not defined
- void arrays
For more examples of errors that can be handled, please review `2.cm`, `3.cm`, `4.cm` and `5.cm` in the `tests` directory. 
