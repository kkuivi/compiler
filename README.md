# compiler

## Overview
A part of a final project for a class, I created a simple compiler for a custom mini language implemented in Java.

## Components of the Compiler
Components of the compiler that were implemented are:
* `Scanner` - Takes in the program as a text file and tokenizes it.
* `Parser` - Takes in the output of the `Scanner` to check for syntax errors.
* `Postfix`- Takes in the output of the `Scanner` after the program has been checked for syntax errors, and arranges the tokens in order of execution. 
* `Symbol Table` - Holds the identifiers declared in the program.
* `Executor` -Executes the program.

## Documentation
The documentation for this program can be found at `Documentation.pdf`
