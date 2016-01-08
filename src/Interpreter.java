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
	public static int[][] set_values = new int[200][10]; 
	public static int free_set_location = 0;
	
	static String[] strTokens= new String[200]; //used to store actual program tokens as they are found
	static int tokCounter = -1;
	
	
	static int[] tokens = new int[200];	//used to store token int Value as they are found by scanner
	static int current_token = 0;
	static int next_token = current_token + 1;
	
	
	 static String[] kwTable = { "if", "else", "read", "write", "declare",
	  "random", "for", "to", "then", "loop", "fi", "endloop", "set", "integer",
	  "in" };

	// static String[] symbTable = {"##", "<=", ">=", "--", "..", ":=", "'",
	// "+", "=", "*", "/", "(", ")", "{", "}", ">", "<", ",", ";"};

	static final int error = 100;
	static final int eof = 0; // end of file
	static final int identifier = 1;
	static final int constant = 2;
	static final int set_element = 50; //this will be used in the postfix to denote
    									//a set element
	
	static final int dollar = 99;
	
	// symbol values
	static final int assign = 3; // :=
	static final int endProg = 4; // ##
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

	static final int BR = 38;
	static final int BMZ = 39;
	static final int BM = 40;
	static final int BPZ = 41;
	static final int BP = 42;
	static final int BZ = 43;
	static final int BNZ = 44;
	
	static final int uminus = 45;
	static final int read  = 46;
	static final int write = 47;
	
	static final int not = 48;
	
	static final int numKeywords = 15;
	static final int numSymbols = 23; 
	
	/*variables needed for scanner*/
	static char ch;
	static int tok;
	static CharBufferedReader reader;
	
	static SymbolTable symbolTable;
	static Execute codeGenerator;

	public static void main(String args[])  throws IOException {
		
		/*Scanner Part*/
		//Start Reading char by char
		reader = new CharBufferedReader("test5.txt");
		symbolTable = new SymbolTable();
		codeGenerator = new Execute();
		ch = reader.GetChar(); //try to start in scanner
		
		System.out.print("IntVal Tokens:\t");
		while(ch!=reader.eofReached )
		{
			int tok = Scanner();
			System.out.print(tok + "\t\t");
			output_token(tok);
		}
		System.out.println();
		
		/*Print String Tokens*/
		System.out.print("String Tokens:\t");
		for(int i=0; i<strTokens.length && strTokens[i]!=null; i++ ){
			System.out.print(strTokens[i]+"\t\t");
		}
		System.out.println();
		System.out.println();
		
		
		/*Parser Part*/
		System.out.println("Parser Notes:");
		File file = new File("output.txt");
		read_input(file);
		if (program()) {
			System.out.println("----Parser: SUCCESS----");
		} else {
			System.out.println("----Parser: FAIL----");
		}
		System.out.println();
		
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
		
		/*Print symbol table again after executing sample program*/
		System.out.println("Symbol Table:");
		for(int i=1; i<100 && symbolTable.symTable[i].name!=null; i++)
		{
			System.out.println(symbolTable.symTable[i]);
		}
		System.out.println();
	}

	public static void read_input(File file) throws FileNotFoundException {
		Scanner filereader = new Scanner(file);
		int index = 0;
		while (filereader.hasNextInt()) {
			tokens[index] = filereader.nextInt();
			index++;
		}
	}

	public static void next_token() {
		if (current_token < 200) {
			current_token = current_token + 1;
		} else {
			System.out.println("error: no more tokens");
		}
	}

	public static void prev_token() {
		if (current_token >= 0) {
			current_token = current_token - 1;
		} else {
			System.out.println("error: no more tokens");
		}
	}

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
	}

	public static boolean program() { //modified
		// TODO: Re-evaluate program() method
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
					System.out.println("here " + tokens[current_token] );
					next_token();
					if (st_group()) {
						if (tokens[current_token] == endProg) {
							return true;
						} else {
							error("Missisng '##' at the end of program");
							return false;
						}
					} else {
						error("Statement group expected after '..'");
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

	public static boolean decl() {	//Add identifiers to symbol table in this part of the parsing
		if (tokens[current_token] == kw_declare) {
			System.out.println("decl()" + tokens[current_token]);
			next_token();  
			System.out.println("decl()" + tokens[current_token]);
			if (tokens[current_token] == kw_integer) {
				next_token();
				System.out.println("decl()" + tokens[current_token]);
				int startIndex =current_token;  //start of identifiers declared of type integer
				if(!identifer_list())
				{
					return false;
				}
				int endIndex=current_token; //end of identifiers declared of type integer
				
				for(int i=startIndex; i<endIndex; i+=2)
				{
					symbolTable.addElement(kw_integer, strTokens[i]);
				}
				return true;
			} else if (tokens[current_token] == kw_set) {
				next_token();
				int max_sizeIndex = current_token; //gets position of max size of
				                                  //set
				if (constant()) {
					int startIndex =current_token;  //start of identifiers declared of type set
					if (identifer_list()) {
						int endIndex=current_token; //end of identifiers declared of type set
						
						for(int i=startIndex; i<endIndex; i+=2)
						{
							//symbolTable.addTypeName("Set", strTokens[i]);
							int max_size = Integer.parseInt(strTokens[max_sizeIndex]);
							symbolTable.addSetElement(kw_set, strTokens[i], max_size);
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

	public static boolean constant() {
		// TODO Re-evaluate constant() method
		if (tokens[current_token] == constant) {
			next_token();
			System.out.println("constant()" + tokens[current_token]);
			return true;
		} else {
			error("constant");
			next_token();
			return false;
		}
	}

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
	

	public static boolean identifier() {
		// TODO Re-evaluate identifier() method
		if (tokens[current_token] == identifier) {
			next_token();
			System.out.println("identifier()" + tokens[current_token]);
			return true;
		} else {
			error("identifier");
			next_token();
			return false;
		}
	}
	public static boolean identifer_list(int operation) {
		if (tokens[current_token] == identifier) {
			postfix[PFPtr++] = identifier;
			postfix[PFPtr++] = symbolTable.find(strTokens[current_token]);
			PFPtr++; //leave a space for read or write operation
			next_token();
			while (tokens[current_token] == comma) {
				next_token();
				if (tokens[current_token] != identifier) {
					error("Identifier expected after  ','");
					return false;
				}
				postfix[PFPtr++] = identifier;
				postfix[PFPtr++] = symbolTable.find(strTokens[current_token]);
				PFPtr++; //leave a space for read or write operation
				next_token();
			}
			return true;
		} else {
			error("identifier_list");
			return false;
		}
	}

	public static boolean st_group() {
		
		if (st()) {
			System.out.println("st_group " + tokens[current_token]);
			while (tokens[current_token] == semi) {
				System.out.println("st_group " + tokens[current_token]);
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

	public static boolean st() {	//modified this method
		if (tokens[current_token] == identifier) {
			postfix[PFPtr++] = tokens[current_token];
			postfix[PFPtr++] = symbolTable.find(strTokens[current_token]);
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
	/*
	public static boolean loop() {
		
		if (loop_part() && tokens[current_token] == kw_loop) {
			
			next_token();
			if (st_group()){				
				if(tokens[current_token] == kw_endloop) {
					next_token();
					return true;
				}
			}
			error("Invalid loop structure: "
					+ "Stament group and 'endloop' keyword expected after 'loop' keyword");
			return false;
		} else {
			error("loop");
			return false;
		}
	}

	public static boolean loop_part() {	//made changes
											// TODO error(String errorname) calls for loop_part()
		if (tokens[current_token-1] == kw_to) { 
			
			symbolTable.add("integer", "temp", "0"); //add a temp identifier to symbol table
			int tempPos = symbolTable.find("temp");

			if (expression()) {
				return true;
			} else {
				error("Invalid loop part structure: "
						+ "Missing expression after 'to' keyword");
				return false;
			}
		} else if (tokens[current_token-1] == kw_for) {
			if (tokens[current_token] == identifier) {
				next_token();
				if (tokens[current_token] == kw_in) {
					next_token();
					if (expression()) {
						return true;
					}
				}
			}
			error("Invalid loop part structure: "
					+ "Missing identifier after  'for' keyword");
			return false;

		} else {
			next_token();
			error("loop part");
			return false;
		}

	}
	*/
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
				return false;
			}
			
			postfix[PFPtr++] = minus;
			save3 = PFPtr;
			PFPtr+=2; //
			postfix[PFPtr++] = BZ;
			
			next_token();
			if (!st_group()){
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
				return false;
			}
			next_token();
			return true;
			
		}
			
		else if (tokens[current_token-1] == kw_for) {
			
			if (tokens[current_token] != identifier) {
				return false;
			}
			next_token();
			if (tokens[current_token] != kw_in) {
				return false;
			}
			next_token();
			if (!expression()) {
				return false;
			}
			if (tokens[current_token] != kw_loop) {
				return false;
			}
			next_token();
			if (!st_group()){
				return false;
			}
			if(tokens[current_token] != kw_endloop) {
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
		//return false;
	}
	
	public static boolean cond() {
		// TODO error(String errorname) for cond()
		/*
		 * grammar rule:<cond> := if <expression> (=|>|<|>=|<=|#|in)
		 * <expression> then <st_group> [else <st_group>] fi
		 */
		
		int code = 0;
		int save1 = 0;
		int save2 = 0;
		// assumed keyword 'if' was already recognized
		if (expression()) // expression took care of getting next token
		{
			System.out.println("Expression1 true");
			if (tokens[current_token] != equal
					&& tokens[current_token] != greater
					&& tokens[current_token] != less
					&& tokens[current_token] != greaterEq
					&& tokens[current_token] != lessEq
					&& tokens[current_token] != not
					&& tokens[current_token] != kw_in) 
			{
				return false;
			}
			if(tokens[current_token] == equal)
			{
				code = BNZ;
			}
			if(tokens[current_token] == greater)
			{
				code = BMZ;
			}
			if(tokens[current_token] == less)
			{
				code = BPZ;
			}
			if(tokens[current_token] == greaterEq)
			{
				code = BM;
			}
			if(tokens[current_token] == lessEq)
			{
				code = BP;
			}
			if(tokens[current_token] == not)
			{
				code = BZ;
			}
			if(tokens[current_token] == kw_in)
			{
				//need to do
			}
				System.out.println("relation true");
				next_token(); // increment the token index
				if (expression()) 
				{
					System.out.println("Expression2 true");
					
					postfix[PFPtr++] = minus;
					postfix[PFPtr++] = constant;
					save1 = PFPtr;
					PFPtr++;
					postfix[PFPtr++] = code;
					
					if (tokens[current_token] == kw_then) 
					{
						System.out.println("kw_then true");
						next_token();
						if (st_group()) 
						{
							System.out.println("st_group 1 true");
							if (tokens[current_token] == kw_else) 
							{
								System.out.println("kw_else true");
								next_token();
								
								postfix[PFPtr++] = constant;
								save2 = PFPtr;
								PFPtr++;
								postfix[PFPtr++] = BR;
								postfix[save1] = PFPtr;
								
								if (!(st_group())) {
									System.out.println("st_group 2 false");
									// error: non st group followed the else
									// statement
									error("Non statement group follwed the else statement");
									return false;
								}
								
								//postfix[save2] = PFPtr;
							
								if (tokens[current_token] == kw_fi) {
									postfix[save2] = PFPtr;
									System.out.println("kw_fi true");
									next_token();
									return true;
								}
							} // no error follows this if statement because its
								// optional in the grammar
							if (tokens[current_token] == kw_fi) {
								postfix[save1] = PFPtr;
								System.out.println("kw_fi true");
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
			/*} else {
				// error: no relation token followed expression
				error("no relation token followed expression");
				return false;
			}*/
		} else {
			// don't need to call next_token because expression() took care of
			// that
			// error: no expression follows if keyword
			error("cond");
			return false;
		}
	}

	public static boolean write() { //modified
		if (output_list()) {
			return true;
		} 
		else
		{
			return false;
		}
	}

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
					} else if (tokens[current_token] == quote) {
						if (!quote()) {
							return false;
						}
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

	public static boolean word() {
		if (tokens[current_token] == identifier) {
			next_token();
			return true;
		}

		else if (tokens[current_token] == constant) {
			next_token();
			return true;
		} else {
			next_token();
			error("Invalid word structure");
			return false;
		}

	}

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
				System.out.println("expression " + tokens[current_token]);
				int oper = tokens[current_token];
				next_token();
				System.out.println("after adding operator "
						+ tokens[current_token]);
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

	public static boolean multiplying_operator() {
		System.out.println("multiplying operator " + tokens[current_token]);
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

	public static boolean read() { //modified
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

	public static boolean asgn() { //modified
		
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
			next_token();
			return false;
		}
	}

	public static boolean factor2() {	//modified
		System.out.println("factor2 " + tokens[current_token]);
		if (tokens[current_token] == identifier
				|| tokens[current_token] == constant) {
			int operand = tokens[current_token];
			if(operand==identifier)
			{
				postfix[PFPtr++] = identifier;
				postfix[PFPtr++] = symbolTable.find(strTokens[current_token]);
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

	public static boolean set() { //modified today
	
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

	public static boolean element() {
		if (tokens[current_token] == constant) {
			int startNumber = Integer.parseInt(strTokens[current_token]);
			int currentNumber = startNumber;
			postfix[PFPtr++] = set_element;
			postfix[PFPtr++] = Integer.parseInt(strTokens[current_token]);
			next_token();
			if (tokens[current_token] == cont) {
				next_token();
				if (tokens[current_token] == constant) {
					int endNumber = Integer.parseInt(strTokens[current_token]);
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
	
	/*Scanner methods*/
	public static int Scanner() throws IOException{
		while(ch==' ') //ignore leading or ending blank spaces
		{
			ch = reader.GetChar();
		}
		while(ch=='\t') //ignore leading or ending tabs
		{
			ch = reader.GetChar();
		}
		
		//check for identifiers and keywords
		if(ch >= 'a' && ch<= 'z' ) 
		{
			String str="";
			boolean kWord = false;
			while((ch>='a' && ch<='z')||(ch>='0' && ch<='9'))
			{
				str = str+ch;
				ch=reader.GetChar();
			}
			for(int i =0; i<numKeywords; i++)
			{
				if(str.equals(kwTable[i]))
				{
					strTokens[tokCounter+=1] = kwTable[i]; //added
					tok = i+numSymbols;
					kWord = true;
					break;
				}
			}
			if(!kWord)
			{
				strTokens[tokCounter+=1] = str; //added
				tok = identifier;
			}
		}
		else if(ch>='0' && ch<='9')
		{
			String str="";
			while(ch>='0' && ch<='9')
			{
				str = str+ch;
				ch=reader.GetChar();
			}
			strTokens[tokCounter+=1] = str; //added
			tok = constant;
		}
		//check for two token symbols
		else if(ch==':')
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
		else if(ch=='#')
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
		else if(ch=='.')
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
		else if(ch=='>')
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
		else if(ch=='<')
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
		else if(ch=='-')
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
		else if(ch=='+')
		{
			strTokens[tokCounter+=1] = "+"; //added
			tok = plus;
			ch = reader.GetChar();
		}
		else if(ch=='*')
		{
			strTokens[tokCounter+=1] = "*="; //added
			tok = mult;
			ch = reader.GetChar();
		}
		else if(ch=='/')
		{
			strTokens[tokCounter+=1] = "/"; //added
			tok = div;
			ch = reader.GetChar();
		}
		else if(ch=='(')
		{
			strTokens[tokCounter+=1] = "("; //added
			tok = lpar;
			ch = reader.GetChar();
		}
		else if(ch==')')
		{
			strTokens[tokCounter+=1] = ")"; //added
			tok = rpar;
			ch = reader.GetChar();
		}
		else if(ch=='{')
		{
			strTokens[tokCounter+=1] = "{"; //added
			tok = lbrac;
			ch = reader.GetChar();
		}
		else if(ch=='}')
		{
			strTokens[tokCounter+=1] = "}"; //added
			tok = rbrac;
			ch = reader.GetChar();
		}
		else if(ch=='=')
		{
			strTokens[tokCounter+=1] = "="; //added
			tok = equal;
			ch = reader.GetChar();
		}
		else if(ch==',')
		{
			strTokens[tokCounter+=1] = ","; //added
			tok = comma;
			ch = reader.GetChar();
		}
		else if(ch==';')
		{
			strTokens[tokCounter+=1] = ";"; //added
			tok = semi;
			ch = reader.GetChar();
		}
		else if(ch=='\'')
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
		else if(ch=='$')
		{
			strTokens[tokCounter+=1] = "$"; //added
			tok = dollar;
			ch = reader.GetChar();
		}
		else if(ch=='&'){
			strTokens[tokCounter+=1] = "&"; //added
			return tok = eof;
		}
		else{
			tok = error;
			ch = reader.GetChar();
		}
		return tok;
	}
	
	public static void output_token(int tok) throws IOException{
	
		File file = new File("output.txt");
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//for next line
		if(tok == semi)
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
	
	
	public static void runProg(int[] postfix) 
	{
		
	}
	

}

