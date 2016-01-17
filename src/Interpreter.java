import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Interpreter {
	/* Global variables */
	
	static int[] postfix = new int[300];
	static int PFPtr = 0; //postfix array pointer
	
	/*
	 * Will store the values of each set
	 * For this project, we can have a maximum of 200 sets,
	 * and each set can have a maximum size of 10
	 */
	public static int[][] set_values = new int[200][10]; //acts as an address for variables declared as set and stores their corresponding set elements
	public static int free_set_location = 0;
	public static final String FILE_EXTENSION = ".txt";
	public static final String OUTPUT_FILE_EXTENSION = "-output.txt";
	public static boolean new_session = true; //will be used by the output_token method to determine if this is the first time the compiler is running
	
	static String[] strTokens= new String[200]; //used to store actual program tokens as they are found
	static int tokCounter = -1; 
	
	
	static int[] tokens = new int[200];	//used to store integer value of tokens as they are found by scanner
	static int current_token = 0;
	static int next_token = current_token + 1;
	
	//stores list of keywords
	 static String[] kwTable = { "if", "else", "read", "write", "declare",
	  "random", "for", "to", "then", "loop", "fi", "endloop", "set", "integer",
	  "in" };

	// static String[] symbTable = {"##", "<=", ">=", "--", "..", ":=", "'",
	// "+", "=", "*", "/", "(", ")", "{", "}", ">", "<", ",", ";"};

	static final int error = 100;
	static final int eof = 0; // end of file
	static final int identifier = 1; //integer value for an identifier
	static final int constant = 2; //integer value for a constant
	static final int set_element = 50; //integer value for a set element
	static final int word_element = 128; //integer value denoting an element that is part of a word
	
	static final int dollar = 99; //integer value denoting the end of declare statements. In this
								 //language all variables have to be declared at the beginning of the program
	
	// symbol values
	static final int assign = 3; // :=
	static final int endProg = 4; // ##, denotes the end of a program
	static final int greaterEq = 5; // >+
	static final int greater = 6; // >
	static final int lessEq = 7; // <=
	static final int less = 8; // <
	static final int cont = 9; // ..
	static final int comment = 10; // --
	static final int minus = 11; // -
	static final int plus = 12; // +
	static final int mult = 13; // *
	static final int div = 14; // /
	static final int lpar = 15; // (
	static final int rpar = 16; // )
	static final int lbrac = 17; // {
	static final int rbrac = 18; // }
	static final int equal = 19; // =
	static final int comma = 20; // ,
	static final int semi = 21; // ;
	static final int quote = 22; // '
	
	static final int kw_if = 23; // if
	static final int kw_else = 24; // else
	static final int kw_read = 25; // read
	static final int kw_write = 26; // write
	static final int kw_declare = 27; // declare
	static final int kw_random = 28; // random
	static final int kw_for = 29; // for
	static final int kw_to = 30; // to
	static final int kw_then = 31; // then
	static final int kw_loop = 32; // loop
	static final int kw_fi = 33; // fi
	static final int kw_endloop = 34; // endloop
	static final int kw_set = 35; // set
	static final int kw_integer = 36; // integer
	static final int kw_in = 37; // in

	//branch operation values
	static final int BR = 38;
	static final int BMZ = 39;
	static final int BM = 40;
	static final int BPZ = 41;
	static final int BP = 42;
	static final int BZ = 43;
	static final int BNZ = 44;
	
	static final int uminus = 45; //unary minus
	static final int read  = 46;
	static final int write = 47;
	
	static final int not = 48;
	
	static final int numKeywords = 15; //number of keywords in kwTable
	static final int numSymbols = 23; //will be used together with the index of the corresponding keyword in kwTable 
									 //its integer value
	
	/*variables needed for scanner*/
	static char ch; //stores current character being read by the CharBufferedReader
	static int tok; //stores integer value of character being read
	static CharBufferedReader reader; //reads input file
	
	static SymbolTable symbolTable; //acts as an address that stores declared variables and their corresponding values
	static Execute codeGenerator;  //goes through postfix array and executes code

	public static void main(String args[])  throws IOException {
		
		/*Scanner Part*/
		//Start Reading char by char
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter file to read (without the .txt extension):");
		String filename = scan.next();
		
		reader = new CharBufferedReader(filename, FILE_EXTENSION); //will check if the filename exists, gets the correct filename 
		symbolTable = new SymbolTable();
		codeGenerator = new Execute();
		ch = reader.GetChar(); //try to start in scanner
		
		System.out.print("IntVal Tokens:\t");
		while(ch!=reader.eofReached ) //while there are more characters to read
		{
			int tok = Scanner(); //store token value of current character being read
			System.out.print(tok + "\t\t");
			output_token(tok, reader.getFilename()); //write token value to output text file, 
													//reader.getFilename() is used because it contains the correct filename (without the .txt extension)
		}
		System.out.println();
		
		/*Print String Tokens*/
		System.out.print("String Tokens:\t");
		for(int i=0; i<strTokens.length && strTokens[i]!=null; i++ ){ //goes through the String values of the tokens read and prints them
			System.out.print(strTokens[i]+"\t\t");
		}
		System.out.println();
		System.out.println();
		
		
		/*Parser Part*/
		System.out.println("Parser Notes:");
		File file = new File(reader.getFilename() + OUTPUT_FILE_EXTENSION);
		readOutputFile(file);
		if (program()) { //checks if program is syntactically correct
			System.out.println("----Parser: SUCCESS----");
			/*Print symbol table */
			System.out.println("Symbol Table:");
			for(int i=1; i<100 && symbolTable.symTable[i].name!=null; i++)
			{
				System.out.println(symbolTable.symTable[i]);
			}
			System.out.println();
			
			/*Print postfix array */
			System.out.println("Postfix Array:");
			for(int i=0; i<postfix.length ; i++)
			{
				System.out.print(postfix[i] + "  |  ");
			}
			System.out.println();
					
			codeGenerator.RunProg(postfix);
			System.out.println();
			System.out.println("----Executed: Symbol Table----");
			/*Print symbol table again after executing sample program*/
			System.out.println("Symbol Table:");
			for(int i=1; i<100 && symbolTable.symTable[i].name!=null; i++)
			{
				System.out.println(symbolTable.symTable[i]);
			}
			System.out.println();
		} else {
			System.out.println("----Parser: FAIL----");
		}
		System.out.println();
	}

	//reads the token values in the output file and stores them in the tokens array
	public static void readOutputFile(File file) throws FileNotFoundException {
		Scanner filereader = new Scanner(file);
		int index = 0;
		while (filereader.hasNextInt()) {
			tokens[index] = filereader.nextInt();
			index++;
		}
	}

	//moves pointer for the token array to next token 
	public static void next_token() {
		if (current_token < 200) { //the length of the token array is 200
			current_token = current_token + 1;
		} else {
			System.out.println("error: no more tokens");
		}
	}

	//moves pointer for the token array to previous token
	public static void prev_token() {
		if (current_token >= 0) {
			current_token = current_token - 1;
		} else {
			System.out.println("error: no more tokens");
		}
	}

	//Prints out an error statement depending on the String errorname
	public static void error(String errorname) {
		if (errorname.equalsIgnoreCase("read")) {
			System.out
					.println("Invalid read statement: Missing the 'read' keyword");
		}

		else if (errorname.equalsIgnoreCase("output_list")) {
			System.out.println("Invalid output list statement");
		}

		else if (errorname.equals("write")) {
			System.out
					.println("Invalid write statement: Missing the 'write' keyword ");
		}

		else if (errorname.equalsIgnoreCase("st")) {
			System.out.println("Invalid statement structure");
		}

		else if (errorname.equalsIgnoreCase("st_group")) {
			System.out.println("Inavlid statement group structure");
		}

		else if (errorname.equalsIgnoreCase("identifier_list")) {
			System.out.println("Invalid identifier list structure");
		}

		else if (errorname.equalsIgnoreCase("decl")) {
			System.out.println("Invalid declare statement: Missing the "
					+ "'declare' keyword");
		}

		else if (errorname.equalsIgnoreCase("decl_part")) {
			System.out.println("Invalid declare part statement");
		}

		else if (errorname.equalsIgnoreCase("identifier")) {
			System.out.println("Valid Identifier expected");
		}

		else if (errorname.equalsIgnoreCase("Missing end quote")) {
			System.out.println("Missing end quote after word");
		}

		else if (errorname.equalsIgnoreCase("Missing beginning quote")) {
			System.out.println("Missing beginning quote before word");
		}

		else if (errorname.equalsIgnoreCase("constant")) {
			System.out.println("Valid Constant expected");
		}

		else if (errorname.equalsIgnoreCase("Word expected after quote")) {
			System.out.println("Word expected after quote");
		}

		else if (errorname
				.equalsIgnoreCase("Missing term after keyword 'random")) {
			System.out.println("Missing term after keyword 'random");
		}

		else if (errorname.equalsIgnoreCase("Missing term or keyword 'random")) {
			System.out.println("Missing term or keyword 'random");
		}

		else if (errorname.equalsIgnoreCase("adding_operator")) {
			System.out.println("Missing '+' or '-' symbol");
		}

		else if (errorname.equalsIgnoreCase("term")) {
			System.out.println("Invalid term structure: Missing factor");
		}

		else if (errorname.equalsIgnoreCase("multiplying_operator")) {
			System.out.println("Missing '*' or '/' symbol");
		}

		else if (errorname.equalsIgnoreCase("Invalid token after '-'")) {
			System.out.println("Invalid token after '-'");
		}

		else if (errorname.equalsIgnoreCase("factor")) {
			System.out.println("Invalid factor structure");
		}

		else if (errorname.equalsIgnoreCase("Missing ')'")) {
			System.out.println("Missing ')'");
		}

		else if (errorname.equalsIgnoreCase("Missing expression after '('")) {
			System.out.println("Missing expression after '('");
		}

		else if (errorname.equalsIgnoreCase("factor2")) {
			System.out.println("Invalid factor2 structure");
		}

		else if (errorname.equalsIgnoreCase("Missing '}'")) {
			System.out.println("Missing '}'");
		}

		else if (errorname
				.equalsIgnoreCase("Missing element list after '{' or "
						+ "missing '}' after empty element list")) {
			System.out.println("Missing element list after '{' or "
					+ "missing '}' after empty element list");
		}

		else if (errorname.equalsIgnoreCase("set")) {
			System.out.println("Missing '{'");
		}

		else if (errorname.equalsIgnoreCase("Missing expression after ':='")) {
			System.out.println("Missing expression after ':='");
		}

		else if (errorname.equalsIgnoreCase("Missing ':='")) {
			System.out.println("Missing ':='");
		}

		else if (errorname.equalsIgnoreCase("asgn")) {
			System.out.println("Invalid asgn structure: Missing identifier");
		}

		else if (errorname.equalsIgnoreCase("element expected after ','")) {
			System.out.println("element expected after ','");
		}

		else if (errorname.equalsIgnoreCase("element_list()")) {
			System.out
					.println("Invalid element list structure: Missing element");
		}

		else if (errorname.equalsIgnoreCase("Invalid element structure: "
				+ "constant expected after '..'")) {
			System.out
					.println("Invalid element structure: constant expected after '..'");
		}

		else if (errorname.equalsIgnoreCase("element")) {
			System.out.println("Invalid element structure: Missing constant");
		}

		else if (errorname.equalsIgnoreCase("Invalid term structure: "
				+ "factor expected after '*' or '/'")) {
			System.out.println("Invalid term structure: "
					+ "factor expected after '*' or '/'");
		}

		else if (errorname.equalsIgnoreCase("Invalid expression structure: "
				+ "term expected after '+' or '-'")) {
			System.out.println("Invalid expression structure: "
					+ "term expected after '+' or '-'");
		}

		else if (errorname
				.equalsIgnoreCase("Missisng '##' at the end of program")) {
			System.out.println("Missisng '##' at the end of program");
		}

		else if (errorname
				.equalsIgnoreCase("Statement group expected after '..'")) {
			System.out.println("Statement group expected after '..'");
		}

		else if (errorname.equalsIgnoreCase("Missing '..' after declare part")) {
			System.out.println("Missing '..' after declare part");
		}

		else if (errorname.equalsIgnoreCase("program")) {
			System.out
					.println("Invalid program structure: Missing statement group"
							+ " or declare part");
		}

		else if (errorname
				.equalsIgnoreCase("Invalid statement group structure: "
						+ "Missing statement after ';'")) {
			System.out.println("Invalid statement group structure: "
					+ "Missing statement after ';'");
		}

		else if (errorname.equalsIgnoreCase("Invalid loop structure: "
				+ "Stament group and 'endloop' keyword expected "
				+ "after 'loop' keyword")) {
			System.out.println("Invalid loop structure: "
					+ "Stament group and 'endloop' keyword expected "
					+ "after 'loop' keyword");
		}

		else if (errorname.equalsIgnoreCase("loop")) {
			System.out.println("Invalid loop structure: "
					+ "loop part and 'loop' keyword expected");
		}

		else if (errorname.equalsIgnoreCase("Invalid loop part structure: "
				+ "Missing expression after 'to' keyword")) {
			System.out.println("Invalid loop part structure: "
					+ "Missing expression after 'to' keyword");
		}

		else if (errorname.equalsIgnoreCase("Invalid loop part structure: "
				+ "Missing identifier after  'for' keyword")) {
			System.out.println("Invalid loop part structure: "
					+ "Missing identifier after  'for' keyword");
		}

		else if (errorname.equalsIgnoreCase("loop part")) {
			System.out.println("Invalid loop part structure: "
					+ "Missing 'to' or 'for' keyword");
		}

		else if (errorname.equalsIgnoreCase("Non statement group follwed "
				+ "the else statement")) {
			System.out
					.println("Non statement group follwed the else statement");
		}

		else if (errorname
				.equalsIgnoreCase("no end 'fi' marker to if statement")) {
			System.out.println("no end 'fi' marker to if statement");
		}

		else if (errorname.equalsIgnoreCase("no statement group "
				+ "followed by keyword 'then'")) {
			System.out.println("no statement group followed by keyword 'then'");
		}

		else if (errorname
				.equalsIgnoreCase("no 'then' keyword followed expression")) {
			System.out.println("no 'then' keyword followed expression");
		}

		else if (errorname.equalsIgnoreCase("no expression "
				+ "followed the relation token")) {
			System.out.println("no expression followed the relation token");
		}

		else if (errorname
				.equalsIgnoreCase("no relation token followed expression")) {
			System.out.println("no relation token followed expression");
		}

		else if (errorname.equalsIgnoreCase("cond")) {
			System.out.println("no expression follows if keyword");
		}

		else if (errorname.equalsIgnoreCase("Invalid declare part statement: "
				+ "Missing declare statement after ';'")) {
			System.out
					.println("Invalid declare part statement: Missing declare "
							+ "statement after ';'");
		}

		else if (errorname.equalsIgnoreCase("Invalid declare statement: "
				+ "Missing 'integer' or 'set' keyword")) {
			System.out.println("Invalid declare statement: Missing 'integer' "
					+ "or 'set' keyword");
		}

		else if (errorname.equalsIgnoreCase("Identifier expected after  ','")) {
			System.out.println("Identifier expected after  ','");
		}

		else if (errorname
				.equalsIgnoreCase("Invalid declare statement structure: "
						+ "Valid identifier list expected after constant")) {
			System.out.println("Invalid declare statement structure: "
					+ "Valid identifier list expected after constant");
		}

		else if (errorname.equalsIgnoreCase("Invalid set structure: "
				+ "Missing '}' after element list")) {
			System.out
					.println("Invalid set structure: Missing '}' after element list");
		}

		else if (errorname.equalsIgnoreCase("Invalid word structure")) {
			System.out.println("Invalid word structure");
		}

		else if (errorname.equalsIgnoreCase("Invalid output list structure: "
				+ "An expression or quote must follow ','")) {
			System.out
					.println("Invalid output list structure: An expression or quote must follow ','");
		}
		else if(errorname.equalsIgnoreCase("Relation token expected expression in the if statement")){
			System.out.println("=, <, <=, >, >= , #, or in, expected after expression");
		}
		else if(errorname.equalsIgnoreCase("keyword loop expected after expression")){
			System.out.println("keyword loop expected after expression");
		}
		else if(errorname.equalsIgnoreCase("Missing statement group after 'loop' keyword")){
			System.out.println("Missing statement group after 'loop' keyword");
		}
		else if(errorname.equalsIgnoreCase("'endloop' keyword expected after statement group")){
			System.out.println("'endloop' keyword expected after statement group");
		}
		else if(errorname.equalsIgnoreCase("identifier expected 'for' keyword")){
			System.out.println("identifier expected 'for' keyword");
		}
		else if(errorname.equalsIgnoreCase("keyword 'in' expected after identifier")){
			System.out.println("keyword 'in' expected after identifier");
		}
		else if(errorname.equalsIgnoreCase("expression expected after 'in' keyword")){
			System.out.println("expression expected after 'in' keyword");
		}
		else if(errorname.equalsIgnoreCase("keyword 'loop' expected after expression")){
			System.out.println("keyword 'loop' expected after expression");
		}
		else if(errorname.equalsIgnoreCase("statement group expected after 'loop' keyword")){
			System.out.println("statement group expected after 'loop' keyword");
		}
		else if(errorname.equalsIgnoreCase("keyword 'endloop' expected after statement group")){
			System.out.println("keyword 'endloop' expected after statement group");
		}
		else if(errorname.equalsIgnoreCase("Must have letter or number between quotes")){
			System.out.println("Must have letter or number between quotes");
		}
	}

	//checks if program is syntactically correct
	public static boolean program() { //modified
		if ((tokens[current_token] == identifier)
				|| (tokens[current_token] == kw_read)
				|| (tokens[current_token] == kw_write)
				|| (tokens[current_token] == kw_if)
				|| (tokens[current_token] == kw_to)
				|| (tokens[current_token] == kw_for)) {
			if (st_group()) {
				if (tokens[current_token] == endProg) {
					return true;
				} else {
					error("Missisng '##' at the end of program");
					return false;
				}
			} else {
				return false;
			}

		} else if (tokens[current_token] == kw_declare) {
			if (decl_part()) {
				if (tokens[current_token] == dollar) {
					next_token();
					if (st_group()) {
						if (tokens[current_token] == endProg) {
							return true;
						} else {
							error("Missisng '##' at the end of program");
							return false;
						}
					} else {
						return false;
					}
				} else {
					next_token();
					error("Missing '$' after declare part");
					return false;
				}
			} else {
				return false;
			}
		} else {
			error("program");
			return false;
		}
	}
	
	//checks if syntax for the decl_part rule of the program is correct
	public static boolean decl_part() {
		if (decl()) {
			while (tokens[current_token] == semi) {
				next_token();
				if (!decl()) {
					error("Invalid declare part statement: Missing declare "
							+ "statement after ';'");
					return false;
				}
			}
			return true;
		} else {
			error("decl_part");
			return false;
		}
	}

	//checks if syntax for the declaration of variables is correct
	public static boolean decl() {	
		if (tokens[current_token] == kw_declare) {
			next_token();  
			if (tokens[current_token] == kw_integer) { //for identifiers declared as integers
				next_token();
				int startIndex =current_token;  //start of identifiers declared as integers
				if(!identifer_list())
				{
					return false;
				}
				int endIndex=current_token; //end of identifiers declared of type integer
				
				for(int i=startIndex; i<endIndex; i+=2)
				{
					symbolTable.addElement(kw_integer, strTokens[i]); //add identifiers(type & name) to the symbolTable
				}
				return true;
			} else if (tokens[current_token] == kw_set) { //for identifiers declared as sets
				next_token();
				int max_sizeIndex = current_token; //gets the max size of the set declared
				if (constant()) {
					int startIndex =current_token;  //start of identifiers declared of type set
					if (identifer_list()) {
						int endIndex=current_token; //end of identifiers declared of type set
						
						for(int i=startIndex; i<endIndex; i+=2)
						{
							int max_size = Integer.parseInt(strTokens[max_sizeIndex]);
							symbolTable.addSetElement(kw_set, strTokens[i], max_size); //add identifiers(type & name) to the symbolTable
						}
						return true;
					} else {
						error("Invalid declare statement structure: "
								+ "Valid identifier list expected after constant");
						return false;
					}
				} else {
					System.out
							.println("Invalid declare statement: Missing constant "
									+ "after 'declare set'");
					return false;
				}
			} else {
				next_token();
				System.out
						.println("Invalid declare statement: Missing 'integer' "
								+ "or 'set' keyword");
				return false;
			}
		
		} else {
			error("decl");
			next_token();
			return false;
		}
		

	}

	//checks if the current token is a constant
	public static boolean constant() {
		if (tokens[current_token] == constant) {
			next_token();
			return true;
		} else {
			error("constant");
			next_token();
			return false;
		}
	}

	//checks if the syntax for a list of identifiers is correct or not
	public static boolean identifer_list() {
		if (identifier()) {
			while (tokens[current_token] == comma) {
				next_token();
				if (!identifier()) {
					error("Identifier expected after  ','");
					return false;
				}
			}
			return true;
		} else {
			error("identifier_list");
			return false;
		}
	}
	

	//checks if the current token  is an identifier
	public static boolean identifier() {
		if (tokens[current_token] == identifier) {
			next_token();
			return true;
		} else {
			error("identifier");
			next_token();
			return false;
		}
	}
	
	/*checks if the syntax for a list of identifiers is correct or not, but also leaves space in the
	 * postfix array to store the operations on each identifier*/
	public static boolean identifer_list(int operation) {
		if (tokens[current_token] == identifier) {
			postfix[PFPtr++] = identifier;
			if(symbolTable.find(strTokens[current_token]) == -1000) //if the identifier cannot be found in the symbol table
				return false;
			postfix[PFPtr++] = symbolTable.find(strTokens[current_token]); //returns index of identifier in the symbolTable array
			PFPtr++; //leave a space in the postfix array to store the read or write operation
			next_token();
			while (tokens[current_token] == comma) {
				next_token();
				if (tokens[current_token] != identifier) {
					error("Identifier expected after  ','");
					return false;
				}
				postfix[PFPtr++] = identifier;
				if(symbolTable.find(strTokens[current_token]) == -1000) //if the identifier cannot be found in the symbol table
					return false;
				postfix[PFPtr++] = symbolTable.find(strTokens[current_token]);//returns index of identifier in the symbolTable array
				PFPtr++; //leave a space in the postfix array to store the read or write operation
				next_token();
			}
			return true;
		} else {
			error("identifier_list");
			return false;
		}
	}

	//checks if the syntax for a group of statements is correct
	public static boolean st_group() {	
		if (st()) {
			while (tokens[current_token] == semi) {//while the current token is a semi-colon then check if there is a statement in the following tokens
				next_token();
				if (!st()) {
					error("Invalid statement group structure: "
							+ "Missing statement after ';'");
					return false;
				}
			}
			return true;
		} else {
			error("st_group");
			return false;
		}
		
	}

	//checks if the syntax for a statement is correct
	public static boolean st() {	
		if (tokens[current_token] == identifier) {
			postfix[PFPtr++] = tokens[current_token];
			if(symbolTable.find(strTokens[current_token]) == -1000) //if identifier cannot be found in the symbolTable array
				return false;
			postfix[PFPtr++] = symbolTable.find(strTokens[current_token]); //add the index of the identifier in symbolTable to the postfix array 
			next_token(); 
			if (asgn()) {
				return true;
			} else {
				return false;
			}
		}

		else if (tokens[current_token] == kw_read) {
			next_token(); 
			if (read()) {
				return true;
			} else {
				return false;
			}
		}

		else if (tokens[current_token] == kw_write) {
			next_token(); 
			if (write()) {
				return true;
			} else {
				return false;
			}
		}

		else if (tokens[current_token] == kw_if) {
			next_token();
			if (cond()) {
				return true;
			} else {
				return false;
			}
		}

		else if ((tokens[current_token] == kw_to)
				|| (tokens[current_token] == kw_for)) {
			next_token();
			if (loop()) {
				return true;
			} else {
				return false;
			}
		}

		else {
			error("st");
			next_token();
			return false;
		}
	}
	
	//checks if the syntax for a loop is correct
	public static boolean loop()
	{
		if (tokens[current_token-1] == kw_to) { 
			int save1, save2, save3, posTemp;
			symbolTable.addTempElement(kw_integer, "temp", 0); //add temp identifier to symbol table
			posTemp = symbolTable.find("temp");
			
			save1 = PFPtr;
			postfix[PFPtr++] = identifier;
			postfix[PFPtr++] = posTemp;
			save2 = posTemp;
			
			if (!expression()) {
				return false;
			} 
			if (tokens[current_token] != kw_loop) {
				error("keyword loop expected after expression");
				return false;
			}
			
			postfix[PFPtr++] = minus;
			save3 = PFPtr;
			PFPtr+=2; //
			postfix[PFPtr++] = BZ;
			
			next_token();
			if (!st_group()){
				error("Missing statement group after 'loop' keyword");
				return false;
			}
			
			postfix[PFPtr++] = identifier;
			postfix[PFPtr++] = save2;
			postfix[PFPtr++] = identifier;
			postfix[PFPtr++] = save2;
			postfix[PFPtr++] = constant;
			postfix[PFPtr++] = 1;
			postfix[PFPtr++] = plus;
			postfix[PFPtr++] = assign;
			postfix[PFPtr++] = constant;//
			postfix[PFPtr++] = save1;
			postfix[PFPtr++] = BR;
			postfix[save3] = constant;//
			postfix[save3+1] = PFPtr;
			
			if(tokens[current_token] != kw_endloop) {
				error("'endloop' keyword expected after statement group");
				return false;
			}
			next_token();
			return true;
			
		}
			
		else if (tokens[current_token-1] == kw_for) {
			
			if (tokens[current_token] != identifier) {
				error("identifier expected 'for' keyword");
				return false;
			}
			next_token();
			if (tokens[current_token] != kw_in) {
				error("keyword 'in' expected after identifier");
				return false;
			}
			next_token();
			if (!expression()) {
				error("expression expected after 'in' keyword");
				return false;
			}
			if (tokens[current_token] != kw_loop) {
				error("keyword 'loop' expected after expression");
				return false;
			}
			next_token();
			if (!st_group()){
				error("statement group expected after 'loop' keyword");
				return false;
			}
			if(tokens[current_token] != kw_endloop) {
				error("keyword 'endloop' expected after statement group");
				return false;
			}
			next_token();
			return true;	
		} 
		else {
			next_token();
			error("loop part");
			return false;
		}
	}
	
	//checks if the syntax for a conditional statement is correct
	public static boolean cond() {
		int code = 0;
		int save1 = 0;
		int save2 = 0;
		// assumed keyword 'if' was already recognized
		if (expression()) // expression took care of getting next token
		{
			if (tokens[current_token] != equal
					&& tokens[current_token] != greater
					&& tokens[current_token] != less
					&& tokens[current_token] != greaterEq
					&& tokens[current_token] != lessEq
					&& tokens[current_token] != not) {
				error("Relation token expected expression in the if statement");
				return false;
			}
			if (tokens[current_token] == equal) {
				code = BNZ;
			}
			if (tokens[current_token] == greater) {
				code = BMZ;
			}
			if (tokens[current_token] == less) {
				code = BPZ;
			}
			if (tokens[current_token] == greaterEq) {
				code = BM;
			}
			if (tokens[current_token] == lessEq) {
				code = BP;
			}
			if (tokens[current_token] == not) {
				code = BZ;
			}

			next_token(); // increment the token index
			if (expression()) {
				postfix[PFPtr++] = minus;
				postfix[PFPtr++] = constant;
				save1 = PFPtr;
				PFPtr++;
				postfix[PFPtr++] = code;

				if (tokens[current_token] == kw_then) {
					next_token();
					if (st_group()) {
						if (tokens[current_token] == kw_else) {
							next_token();

							postfix[PFPtr++] = constant;
							save2 = PFPtr;
							PFPtr++;
							postfix[PFPtr++] = BR;
							postfix[save1] = PFPtr;

							if (!(st_group())) {
								// error: non st group followed the else
								// statement
								error("Non statement group follwed the else statement");
								return false;
							}

							if (tokens[current_token] == kw_fi) {
								postfix[save2] = PFPtr;
								next_token();
								return true;
							}
						} // no error follows this if statement because its
							// optional in the grammar
						if (tokens[current_token] == kw_fi) {
							postfix[save1] = PFPtr;
							next_token();
							return true;
						} else {

							next_token(); // before error message still get
											// next token for following
											// methods
							// error: no end 'fi' marker to if statement
							error("no end 'fi' marker to if statement");
							return false;
						}
					} else {
						// error: no st_group followed kw_then
						error("no statement group followed by keyword 'then'");
						return false;
					}
				} else {
					// error: no kw_then followed expression
					error("no 'then' keyword followed expression");
					return false;
				}
			} else {
				// error: no expression followed the relation token
				error("no expression followed the relation token");
				return false;
			}
		} else {
			// don't need to call next_token because expression() took care of
			// that
			// error: no expression follows if keyword
			error("cond");
			return false;
		}
	}

	//checks if the syntax for a write statement is correct
	public static boolean write() {
		if (output_list()) {
			return true;
		} 
		else
		{
			return false;
		}
	}

	//checks if the syntax for an output list statement is correct
	public static boolean output_list() {
		if ((tokens[current_token] == identifier)
				|| (tokens[current_token] == constant)
				|| (tokens[current_token] == lbrac)
				|| (tokens[current_token] == lpar)
				|| (tokens[current_token] == minus)) {
			if (expression()) {
				postfix[PFPtr++] = kw_write;
				while (tokens[current_token] == comma) {
					next_token();
					if ((tokens[current_token] == identifier)
							|| (tokens[current_token] == constant)
							|| (tokens[current_token] == lbrac)
							|| (tokens[current_token] == lpar)
							|| (tokens[current_token] == minus)) {
						if (!expression()) {
							return false;
						}
						postfix[PFPtr++] = kw_write;
					} else if (tokens[current_token] == quote) {
						if (!quote()) {
							return false;
						}
						postfix[PFPtr++] = kw_write;
					} else {
						error("Invalid output list structure: An expression or quote must follow ','");
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		else if (tokens[current_token] == quote) {
			if (quote()) {
				postfix[PFPtr++] = kw_write;
				while (tokens[current_token] == comma) {
					next_token();
					if ((tokens[current_token] == identifier)
							|| (tokens[current_token] == constant)
							|| (tokens[current_token] == lbrac)
							|| (tokens[current_token] == lpar)
							|| (tokens[current_token] == minus)) {
						if (!expression()) {
							return false;
						}
						postfix[PFPtr++] = kw_write;
					} else if (tokens[current_token] == quote) {
						if (!quote()) {
							return false;
						}
						postfix[PFPtr++] = kw_write;
					} else {
						error("Invalid output list structure: An expression or quote must follow ','");
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		else {
			error("output_list");
			next_token();
			return false;
		}

	}

	//checks if the syntax for a quote is correct
	public static boolean quote() {
		if (tokens[current_token] == quote) {
			next_token();
			if (word()) {
				if (tokens[current_token] == quote) {
					next_token();
					return true;
				} else {
					error("Missing end quote");
					return false;
				}
			} else {
				error("Word expected after quote");
				return false;
			}
		} else {
			error("Missing beginning quote");
			next_token();
			return false;
		}
	}

	//checks if the syntax for a word is correct
	public static boolean word() {
		if (tokens[current_token] == identifier || tokens[current_token] == constant) {
			String word = strTokens[current_token];
			addCharValuesToPostFix(word);
			next_token();
			while(tokens[current_token] != quote){ //while the ending quote has not been encountered add the next digits or letters to the postfix array 
				if(tokens[current_token] == identifier || tokens[current_token] == constant){
					word = strTokens[current_token];
					addCharValuesToPostFix(word);
				}
				else{
					error("Must have letter or number between quotes");
					next_token();
					return false;
				}
				next_token(); 
			}
			return true; //no next_token method is called before the return statement because it is already called before the while loop and in the while loop
		}
		
		else {
			next_token();
			error("Invalid word structure");
			return false;
		}
	}

	//converts characters to their integer values and adds them to the postfix array as word_elements
	private static void addCharValuesToPostFix(String word) {
		for(int i = 0; i < word.length(); i ++){
			postfix[PFPtr++] = word_element;
			postfix[PFPtr++] = (int) word.charAt(i); //store integer value of character in postfix array
		}
	}

	//checks if the syntax for an expression is correct
	public static boolean expression() {
		if (tokens[current_token] == kw_random) {
			next_token();
			if (term()) {
				while ((tokens[current_token] == plus)
						|| (tokens[current_token] == minus)) {
					next_token();
					if (!term()) {
						error("Invalid expression structure: "
								+ "term expected after '+' or '-'");
						return false;
					}
				}
				return true;
			} else {
				error("Missing term after keyword 'random");
				return false;
			}
		} else if (term()) {
			while ((tokens[current_token] == plus)
					|| (tokens[current_token] == minus)) {
				int oper = tokens[current_token];
				next_token();
				if (!term()) {
					error("Invalid expression structure: "
							+ "term expected after '+' or '-'");
					return false;
				}
				postfix[PFPtr++] = oper;			
			}
			return true;
		}

		else {
			error("Missing term or keyword 'random");
			return false;
		}
		
	}

	//checks if the current token is an adding operator
	public static boolean adding_operator() {
		if (tokens[current_token] == plus) {
			next_token();
			return true;
		} else if (tokens[current_token] == minus) {
			next_token();
			return true;
		} else {
			error("adding_operator");
			return false;
		}
	}

	//checks if the syntax for a term is correct
	public static boolean term() {
		if (factor()) {
			while ((tokens[current_token] == mult)
					|| (tokens[current_token] == div)) {
				int oper = tokens[current_token]; 
				next_token();
				if (!factor()) {
					error("Invalid term structure: factor expected after '*' or '/'");
					return false;
				}
				postfix[PFPtr++] = oper;
			}
			return true;
		} else {
			error("term");
			return false;
		}
	}

	//checks if the current token is a multiplying operator
	public static boolean multiplying_operator() {
		if (tokens[current_token] == mult) {
			next_token();
			return true;
		} else if (tokens[current_token] == div) {
			next_token();
			return true;
		} else {
			error("multiplying_operator");
			return false;
		}
	}

	//checks if the syntax for a read statement is correct
	public static boolean read() { 
		int start, end;
		start= PFPtr;
		
		if (identifer_list(kw_read)) {
			end = PFPtr;
			for(int i=start+2; i<=end; i+=3)
			{
				postfix[i] = kw_read;
			}
			return true;
		} else {
			return false;
		}
	}

	//checks if the syntax for an assign statement is correct
	public static boolean asgn() { 
		
		if (tokens[current_token] == assign) {
			next_token();
			if (expression()) {
				postfix[PFPtr++] = assign;
				return true;
			} else {
				error("Missing expression after ':='");
				return false;
			}
		} else {
			error("Missing ':='");
			return false;
		}
			
	}

	//checks if the syntax for a factor is correct
	public static boolean factor() {
		if (tokens[current_token] == minus) {
			next_token();
			if (factor2()) {
				
				postfix[PFPtr++] = uminus;
				
				return true;
			} else {
				error("Invalid token after '-'");
				return false;
			}
		} else if (factor2()) {
			return true;
		}

		else {
			error("factor");
			//next_token();
			return false;
		}
	}

	//checks if the syntax for a factor2 is correct
	public static boolean factor2() {	//modified
		if (tokens[current_token] == identifier
				|| tokens[current_token] == constant) {
			int operand = tokens[current_token];
			if(operand==identifier)
			{
				postfix[PFPtr++] = identifier;
				if(symbolTable.find(strTokens[current_token]) == -1000) //if the variable is not in the symbolTable, then it was not initialized
					return false;
				else
					postfix[PFPtr++] = symbolTable.find(strTokens[current_token]); //add the index of the identifier in symbolTable to the postfix array
			}
			else
			{
				postfix[PFPtr++] = constant;
				postfix[PFPtr++] = Integer.parseInt(strTokens[current_token]);
			}
			next_token();
			return true;
		}

		else if (tokens[current_token] == lpar) {
			next_token();
			if (expression()) {
				if (tokens[current_token] == rpar) {
					next_token();
					return true;
				} else {
					error("Missing ')'");
					return false;
				}
			} else {
				error("Missing expression after '('");
				return false;
			}
		}

		else if (tokens[current_token] == lbrac) {
			next_token(); 
			if (set()) {
				return true;
			} else
				return false;
		}

		else {
			error("factor2");
			next_token();
			return false;
		}
	}
	
	//checks if the syntax for a set is correct
	public static boolean set() {
		if (tokens[current_token] == rbrac) {
			next_token();
			return true;
		} else if (element_list()) {
			if (tokens[current_token] == rbrac) {
				next_token();
				return true;
			} else {
				error("Invalid set structure: Missing '}' after element list");
				return false;
			}
		} else {
			error("Missing element list after '{' or "
					+ "missing '}' after empty element list");
			return false;
		}
			
	}

	//checks if the syntax for a list of set elements is correct
	public static boolean element_list() {
		if (element()) {
			while (tokens[current_token] == comma) {
				next_token();
				if (!element()) {
					error("element expected after ','");
					return false;
				}
			}
			return true;
		} else {
			error("element_list()");
			return false;
		}
	}

	//checks if the syntax for a set element is correct
	public static boolean element() {
		if (tokens[current_token] == constant) {
			int startNumber = Integer.parseInt(strTokens[current_token]);
			int currentNumber = startNumber;
			postfix[PFPtr++] = set_element;
			postfix[PFPtr++] = Integer.parseInt(strTokens[current_token]);
			next_token();
			if (tokens[current_token] == cont) { //if current token is ".." then store consecutive integers from current number to end number to the postfix array
				next_token();
				if (tokens[current_token] == constant) {
					int endNumber = Integer.parseInt(strTokens[current_token]);
					currentNumber++; //update currentNumber because previous number was already
									//add to the postfix array
					while(currentNumber <= endNumber){
						postfix[PFPtr++] = set_element;
						postfix[PFPtr++] = currentNumber;
						currentNumber ++;
					}
					next_token();
					return true;
				} else {
					error("Invalid element structure: constant expected after '..'");
					return false;
				}
			} else {
				return true;
			}
		} else {
			error("element");
			next_token();
			return false;
		}
	}
	
	/*Scanner methods
	 * Returns the token value of the current character being read*/
	public static int Scanner() throws IOException{
		while(ch==' ') //ignore leading or ending blank spaces
		{
			ch = reader.GetChar(); //get the next character
		}
		while(ch=='\t') //ignore leading or ending tabs
		{
			ch = reader.GetChar(); //get the next character
		}
		
		//check for identifiers and keywords
		if(ch >= 'a' && ch<= 'z' ) 
		{
			StringBuilder str= new StringBuilder();
			boolean kWord = false; //stores whether string is a keyword
			while((ch>='a' && ch<='z')||(ch>='0' && ch<='9'))
			{
				str.append(ch);
				ch=reader.GetChar();
			}
			for(int i =0; i<numKeywords; i++) //check if string is a keyword
			{
				
				if(str.toString().equals(kwTable[i]))
				{
					strTokens[tokCounter+=1] = kwTable[i]; //added
					tok = i+numSymbols; //because the integer value for each keyword is equal to its index in the kwTable array + numSymbols
					kWord = true;
					break;
				}
			}
			if(!kWord) //if string is not a keyword
			{
				strTokens[tokCounter+=1] = str.toString(); //added
				tok = identifier;
			}
		}
		else if(ch>='0' && ch<='9')
		{
			StringBuilder str = new StringBuilder();
			while(ch>='0' && ch<='9')
			{
				str.append(ch);
				ch=reader.GetChar();
			}
			strTokens[tokCounter+=1] = str.toString(); //added
			tok = constant;
		}
		//check for two token symbols
		else if(ch==':') //check if token is :=
		{
			ch = reader.GetChar();
			if(ch=='='){
				strTokens[tokCounter+=1] = ":="; //added
				tok = assign;
				ch = reader.GetChar();
			}
			else{
				tok = error;
			}
		}
		else if(ch=='#') //check if token is # or ##
		{
			ch = reader.GetChar();
			if(ch=='#'){
				strTokens[tokCounter+=1] = "##"; //added
				tok = endProg;
				ch = reader.GetChar();
			}
			else{
				strTokens[tokCounter+=1] = "#"; //added
				tok = not;
			}
		}
		else if(ch=='.') //check if token is ..
		{
			ch = reader.GetChar();
			if(ch=='.'){
				strTokens[tokCounter+=1] = ".."; //added
				tok = cont;
				ch = reader.GetChar();
			}
			else{
				tok = error;
			}
		}
		else if(ch=='>') //check if token is >= or >
		{
			ch = reader.GetChar();
			if(ch=='='){
				strTokens[tokCounter+=1] = ">="; //added
				tok = greaterEq;
				ch = reader.GetChar();
			}
			else{
				strTokens[tokCounter+=1] = ">"; //added
				tok = greater;
			}
		}
		else if(ch=='<') //check if token is <= or <
		{
			ch = reader.GetChar();
			if(ch=='='){
				strTokens[tokCounter+=1] = "<="; //added
				tok = lessEq;
				ch = reader.GetChar();
			}
			else{
				strTokens[tokCounter+=1] = "<"; //added
				tok = less;
			}
		}
		else if(ch=='-') //check if token is -- or -
		{
			ch = reader.GetChar();
			if(ch=='-'){
				strTokens[tokCounter+=1] = "--"; //added
				tok = comment;
				reader.skipLine = true;
				ch = reader.GetChar();
			}
			else{
				strTokens[tokCounter+=1] = "-"; //added
				tok = minus;
			}

		}
		else if(ch=='+') //check if token is +
		{
			strTokens[tokCounter+=1] = "+"; //added
			tok = plus;
			ch = reader.GetChar();
		}
		else if(ch=='*') //check if token is *
		{
			strTokens[tokCounter+=1] = "*="; //added, store as *= in strToken array
			tok = mult;
			ch = reader.GetChar();
		}
		else if(ch=='/') //check if token is /
		{
			strTokens[tokCounter+=1] = "/"; //added
			tok = div;
			ch = reader.GetChar();
		}
		else if(ch=='(') //check if token is (
		{
			strTokens[tokCounter+=1] = "("; //added
			tok = lpar;
			ch = reader.GetChar();
		}
		else if(ch==')') //check if token is )
		{
			strTokens[tokCounter+=1] = ")"; //added
			tok = rpar;
			ch = reader.GetChar();
		}
		else if(ch=='{') //check if token is {
		{
			strTokens[tokCounter+=1] = "{"; //added
			tok = lbrac;
			ch = reader.GetChar();
		}
		else if(ch=='}') //check if token is }
		{
			strTokens[tokCounter+=1] = "}"; //added
			tok = rbrac;
			ch = reader.GetChar();
		}
		else if(ch=='=') //check if token is =
		{
			strTokens[tokCounter+=1] = "="; //added
			tok = equal;
			ch = reader.GetChar();
		}
		else if(ch==',') //check if token is ,
		{
			strTokens[tokCounter+=1] = ","; //added
			tok = comma;
			ch = reader.GetChar();
		}
		else if(ch==';') //check if token is ;
		{
			strTokens[tokCounter+=1] = ";"; //added
			tok = semi;
			ch = reader.GetChar();
		}
		else if(ch=='\'') //check if token is '
		{
			strTokens[tokCounter+=1] = "\'"; //added
			tok = quote;
			/*
			while(ch!='\'')
			{
				ch = reader.GetChar();
			}
			*/
			ch = reader.GetChar();
		}
		else if(ch=='$') //check if token is $
		{
			strTokens[tokCounter+=1] = "$"; //added
			tok = dollar;
			ch = reader.GetChar();
		}
		else if(ch=='&'){ //check is token is &
			strTokens[tokCounter+=1] = "&"; //added
			return tok = eof;
		}
		else{
			tok = error;
			ch = reader.GetChar();
		}
		return tok;
	}
	
	//writes integer values of tokens being read to a text file
	public static void output_token(int tok, String input_filename) throws IOException{
		File file = new File(input_filename + OUTPUT_FILE_EXTENSION);
		FileWriter fw;
		/*if the file exists but are currently running a new session of the compiler 
		 * then overwrite the existing output file*/
		if(file.exists() && new_session){ 
			fw = new FileWriter(file, false);
			new_session = false;
		}
		/*if the file exists but are currently running an old session of the compiler then
		 * append subsequent tokens to the end of the output file*/
		else if(file.exists() && !new_session){
			fw = new FileWriter(file, true);
		}
		/*if the file does not exist, then create a new file, and update the new_session
		 * variable*/
		else{
			fw = new FileWriter(file);
			new_session = false;
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//for next line
		if(tok == semi) //token is a semi-colon
		{
			bw.write(tok + "\n"); 
		}
		//otherwise print number
		else
		{
			bw.write(tok + " ");
		}
		bw.close();
	}
}

