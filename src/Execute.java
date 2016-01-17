import java.util.ArrayList;
import java.util.Hashtable;



public class Execute {

	private int PFC, RSC; //PFC-->Postfix Counter; RSC--> RunStack Counter
	private int[][] runStack; //runStack is a 2D array that will hold operands( identifiers and constants are two part tokens)
	
	final int symTableError = -1000;
	final int runtimeError = -10001;
	
	//operand types
	final int identifier = 1;
	final int constant = 2;
	final int set_element =50; //used to denote a set element
	final int word_element = 128; //used to denote a character in a word
	
	//identifier type
	final int kw_set = 35; // set
	final int kw_integer = 36; // integer
	
	//operations
	final int assign = 3;
	final int minus = 11;
	final int plus = 12;
	final int mult = 13;
	final int div = 14;
	final int read = 25;
	final int write = 26;
	final int BR = 38;
	final int BMZ = 39;
	final int BM = 40;
	final int BPZ = 41;
	final int BP = 42;
	final int BZ = 43;
	final int BNZ = 44;
	final int uminus = 45;
	final int union = 101;
	final int intersection = 102;
	
	public Execute()
	{
		PFC = 0;
		RSC = 0;
		runStack = new int[50][2];
	}
	
	/*pushes an identifier, constant or set element
	 * for identifiers it pushes [identifier][symbolTablePosition]
	 * for constants and set elements it pushes [set_element/constant][value]*/
	public void Push_RunStack(int type, int value)
	{
		RSC++;
		runStack[RSC][0] = type;
		runStack[RSC][1] = value;
	}
	
	public void RunProg(int[] postfix) 
	{
		boolean error = false;
		System.out.println();
		System.out.println("----Program Output:----");
		while(PFC < Interpreter.PFPtr && !error){ //while there are items in the postfix array to read and no error has been encountered
			int operand1 = 0;
			int pos = 0;
			int branchCondVal = 0;
			int branchPos = 0;
		
			switch (postfix[PFC])
			{
			case constant:
				Push_RunStack(postfix[PFC], postfix[PFC+1]); //postfix[PFC] stores the type (constant), 
															//postfix[PFC+1] stores its value
				PFC+=2;		//constants are two part tokens
				break;
			case identifier:
				Push_RunStack(postfix[PFC], postfix[PFC+1]);//postfix[PFC] stores the type (identifier), 
															//postfix[PFC+1] stores its position in the symbol Table
				PFC+=2;		//identifiers are two part tokens
				break;
			case set_element:
				Push_RunStack(postfix[PFC], postfix[PFC+1]);//postfix[PFC] stores the type (set_element), 
															//postfix[PFC+1] stores its value
				PFC +=2;
				break;
			case word_element:
				Push_RunStack(postfix[PFC], postfix[PFC+1]);//postfix[PFC] stores the type (word_element), 
														   //postfix[PFC+1] stores its ascii value/representation
				PFC+=2;    //word_elements are two part tokens
				break;
			case assign:
				//find value of operand1
				int[] set_values1 = new int [10]; //will store current list of 
				                                 //elements in set- maximum size is 
				                                 //10
				int[] set_values2 = {0,0,0,0} ; 
				                   //everything in set_values1 will be stored here
				                  // and its size will represent actual number of 
				                 // set elements
				int i = 0; //used to check how far RSC went back when assigning elements
				       //to a set
				//find value of operand1
				if(runStack[RSC][0]==identifier) //runStack-->[identifier][symTablePosition]
				{
	
						operand1 = Interpreter.symbolTable.getVal(runStack[RSC][1]); //get value of identifier at position runStack[RSC][1] in the symbol Table
				}
				else if(runStack[RSC][0]==constant) //runStack-->[constant][value]
				{
					operand1 = runStack[RSC][1]; 
				}
				else if(runStack[RSC][0]==set_element) //runStack-->[set_element][value]
				{
					i = RSC;
					int counter = 0;
					while(runStack[i][0]==set_element){
						set_values1[counter] = runStack[i][1];
						i = i - 1;
						counter ++;
					}
					//counter stored the number of items in added to set_values1
					int set1_length = counter - 1;
					set_values2 = new int[counter];
					for(int k = 0; k < set_values2.length; k ++){
						set_values2[k] = set_values1[set1_length - k]; //set_values1 stored set values in reverse order so
																	  //it needs to be traversed from the end in order to store the set elements in set_values2 in the right order
					}
				}
				
				//assign value of operand1 to identifier
				if((runStack[RSC][0] == constant) || (runStack[RSC][0] == identifier))
				{
					pos = runStack[RSC-1][1]; //runStack[RSC-1][1] is the value that the assign operation is being applied to
					Interpreter.symbolTable.symTable[pos].setValue(operand1); //change the value of the variable at index pos to operand1
				
					RSC = RSC-2; //RSC-2 because RSC-1 is the identifier in the symbol Table, and RSC is the value that is being assigned to RSC-1
					PFC++;
				}
				else if(runStack[RSC][0] == set_element)
				{
					pos = runStack[i][1]; //RSC[i] is the set that the set elements are being assigned to
					Interpreter.symbolTable.symTable[pos].set_SetValue(set_values2);
					
					RSC = i - 1;
					PFC++;
				}
				break;
			case plus:
				int sum = binaryOperation(plus);
				if(sum == union) //checks if operation was a set union
					PFC++; //this is because the set_union method already handled pushing elements of the union set to the stack
						  // so there's no need to do PushStack operations here
				else if(sum!=symTableError && sum!=runtimeError){
					RSC = RSC-2;	//simulates two pop() of stack;
					Push_RunStack(constant, sum);	//sum of operands is pushed on stack;
				
					PFC++;
				}
				else {
					error=true;
				}
				break;
			case minus:
				int difference = binaryOperation(minus);
				if(difference!=symTableError && difference!=runtimeError){
					RSC = RSC-2;	//simulates two pop() of stack;
					Push_RunStack(constant, difference);	//sum of operands is pushed on stack;
				
					PFC++;
				}
				else {
					error = true;
				}
				break;
			case mult:
				int product = binaryOperation(mult);
				if(product == intersection) //checks if operation was set intersecton
					PFC ++; //this is because the set_intersection method already handled pushing elements of the intersection set to the stack
						   // so there's no need to do PushStack operations here
				else if(product!=symTableError && product!=runtimeError){
					RSC = RSC-2;	//simulates two pop() of stack;
					Push_RunStack(constant, product);	//sum of operands is pushed on stack;
				
					PFC++;
				}
				else {
					error = true;
				}
				break;
			case div:
				int quotient = binaryOperation(div);
				if(quotient!=symTableError && quotient!=runtimeError){
					RSC = RSC-2;	//simulates two pop() of stack;
					Push_RunStack(constant, quotient);	//sum of operands is pushed on stack;
				
					PFC++;
				}
				else {
					error = true;
				}
				break;
			case uminus:
				int temp=0;
				if(runStack[RSC][0]==identifier) //runStack-->[identifier][symTablePosition]
				{
					operand1 = Interpreter.symbolTable.getVal(runStack[RSC][1]); 
					temp=operand1-(operand1*2); //negative value of operand1
					
					RSC--;
					Push_RunStack(constant, temp); //update runStack

				}
				else if(runStack[RSC][0]==constant) //runStack-->[constant][value]
				{
					operand1 = runStack[RSC][1]; 
					temp=operand1-(operand1*2);
					RSC--;
					Push_RunStack(constant, temp); //update runStack
				}	
				PFC++;
				break;
			case read:
				break;
			case write:
				if(runStack[RSC][0]==identifier)
				{
					operand1 = Interpreter.symbolTable.getVal(runStack[RSC][1]);
					System.out.println(operand1);
					RSC = RSC - 1;
				}
				else if(runStack[RSC][0]==constant)
				{
					operand1 = runStack[RSC][1];
					System.out.println(operand1);
					RSC = RSC -1;
				}
				else if (runStack[RSC][0] == word_element){
					int[] chars = copyElements(word_element); //go backwards through runStack and copy consecutive word elements into chars array
					for(int k = 0; k < chars.length; k ++){
						System.out.print((char)chars[k]);
					}
					System.out.println();
					RSC = RSC - chars.length; //Pop word elements from runStack
				}
				else if (runStack[RSC][0] == set_element){
					int[] set = copyElements(set_element); //go backwards through runStack and copy consecutive set elements into chars array
					System.out.print("{");
					for(int k = 0; k < set.length; k ++){
						if(k < set.length -1)
							System.out.print(set[k] + ",");
						else
							System.out.println(set[k] + "}");
					}
					RSC = RSC - set.length; //Pop set elements from runStack
				}
				else
				{
					System.out.println("Runtime error: Can only write an identifier, constant, set element or word element");
					error = true;
				}
				PFC++;
				break;
			case BR:
				if(runStack[RSC][0]==identifier)
				{
					branchPos = Interpreter.symbolTable.getVal(runStack[RSC][1]);
				}
				else if(runStack[RSC][0]==constant)
				{
					branchPos = runStack[RSC][1];
				}
				RSC--;
				PFC = branchPos;
				break;
			case BMZ:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal<=0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				break;
			case BM:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal<0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				break;
			case BPZ:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal>=0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				break;
			case BP:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal>0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				
				break;
			case BZ:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal==0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				break;
			case BNZ:
				branchPos = checkBranchPos();
				branchCondVal = checkBranchVal();
				
				if(branchCondVal!=0)
				{
					PFC = branchPos;
					RSC = RSC-2; //POP 2 off runStack
				}
				else
				{
					RSC = RSC-2; //POP 2 off runStack
					PFC++;
				}
				break;
			default:
				error = true;
				break;
			}
		}
	}
	
	public int binaryOperation(int operation){
		int operand1 = 0;
		int operand2 = 0;
		int value = 0;
		int[] set_operand1 = {0,0,0,0};
		int[] set_operand2 = {0,0,0,0};
		int[] all_setElements = {0,0,0};
		int pop_count = 0; //will be used by the set_union() and set_intersection() methods to know the number of elements to pop from runStack
		boolean set_identifier1 = false; //used to check if an identifier a set
		boolean setElement = false; //used to check whether token is a set element
		
		//find value of operand1
		if(runStack[RSC-1][0]==identifier) //runStack-->[identifier][symTablePosition]
		{
			if (Interpreter.symbolTable.getType(runStack[RSC - 1][0]) == kw_integer) { //if identifier is an integer
				if (Interpreter.symbolTable.getVal(runStack[RSC - 1][1]) != symTableError) {
					operand1 = Interpreter.symbolTable.getVal(runStack[RSC - 1][1]);
				} else {
					System.out.println("ERROR: Variable was not initialized"); // variable was not initialized before using it														
					return symTableError;
				}
			}
			else if(Interpreter.symbolTable.getType(runStack[RSC - 1][1]) == kw_set){ //if identifier is a set
				if (Interpreter.symbolTable.getVal(runStack[RSC - 1][1]) != symTableError) {
					set_operand1 = Interpreter.symbolTable.getSetVal(runStack[RSC - 1][1]);
					set_identifier1 = true;
				} else {
					System.out.println("ERROR: Variable was not initialized"); // variable was not initialized before using it														
					return symTableError;
				}
			}
			else{
				return runtimeError;
			}
		}
		else if(runStack[RSC-1][0]==constant) //runStack-->[constant][value]
		{
			operand1 = runStack[RSC-1][1]; 
		}
		else if(runStack[RSC - 1][0] == set_element)
 		{
			all_setElements = copyElements(set_element); //store all consecutive set elements in runStack going backwards from RSC
			pop_count = all_setElements.length; //pop_count is set to the length of to the length of the all_setElements because all it's elements have been pushed on runStack
			setElement = true;	
		}
		else
		{
			System.out.println("Invalid operand type for binary operation!");
			return runtimeError;
		}
		/*find value of operand2
		 * if token was a set element then there will be no need to find the value of operand2 because 
		 * all set elements were copied using the copyElements() method. However, if token was an identifier
		 * or constant then the value of operand2 will has to be found. */
		
		if(set_identifier1){ //if identifier was a set, then operand 2 must be a set
			if (Interpreter.symbolTable.getType(runStack[RSC][1]) == kw_set) {
				if (Interpreter.symbolTable.getVal(runStack[RSC][1]) != symTableError) {
					set_operand2 = Interpreter.symbolTable.getSetVal(runStack[RSC][1]); //stores the value of the set in symbolTable[runStack[RSC][1]]
					all_setElements = copySetElements(set_operand1, set_operand2); //stores the elements of set_operand1 and set_operand2
					operation = changeToSetOperation(operation); //changes operation to the appropriate set operation; thus, it changes plus to union, and mult to intersection
					pop_count = 2; //pop_count is set to 2 because the set operation will only be between two identifiers 
					if (operation == runtimeError) {//if operation was neither plus nor mult 
						System.out.println("ERROR: Operations on sets must be + or *");
						return runtimeError;
					}
				}
				else{
					System.out.println("ERROR: Variable was not initialized"); // variable was not initialized before using it
					return symTableError;
				}
			}
			else{
				System.out.println("ERROR: Binary Operations with sets can only be done between two sets");
				return runtimeError;
			}
		}
		else if(setElement){ //if token was just a set element and not an identifier 
			operation = changeToSetOperation(operation); //changes operation to the appropriate set operation; thus, it changes plus to union, and mult to intersection
			if(operation == runtimeError){ //if operation was neither plus nor mult
				System.out.println("ERROR: Operations on sets must be + or *");
				return runtimeError;
			}
		}	
		else if(runStack[RSC][0]==identifier) //runStack-->[identifier][symTablePosition]
		{
			if(Interpreter.symbolTable.getVal(runStack[RSC][1])!=symTableError)
			{
				operand2 = Interpreter.symbolTable.getVal(runStack[RSC][1]);
			}
			else 
			{
				System.out.println("ERROR: Variable was initialized"); //variable was not initialized before using
				return symTableError;
			}
		}
		else if(runStack[RSC][0]==constant) //runStack-->[constant][value]
		{
			operand2 = runStack[RSC][1]; 
		}
		else 
		{
			System.out.println("Invalid operand type for binary operation!");
			return runtimeError;
		}
		
		//compute operation on operand1 and operand2
		switch (operation) {
			case plus:  
				value = operand1+operand2;
				break;
			case minus:  
				value = operand1-operand2;
				break;
			case mult:  
				value = operand1*operand2;
				break;
			case div:  
				value = operand1/operand2;
				break;
			case union:
				set_union(all_setElements, pop_count);
				value = union;
				break;
			case intersection:
				set_intersection(all_setElements, pop_count);
				value = intersection;
				break;
			default:
				break;
		 }
		return value;
	}

	//goes through runStack copying all consecutive elements of the specified type starting from RSC and returning them as an array of integers
	private int[] copyElements(int type) {
		ArrayList<Integer> elements1 = new ArrayList<Integer>();
		int[] result;
		int i = RSC; //used to check how far RSC went back when copying the set elements into the arraylist
		while(runStack[i][0]==type){
			elements1.add(runStack[i][1]); //add elements to elements1 arraylist
			i = i - 1;			
		}
		int set1_lastIndex = elements1.size() - 1; //index of last element in elements1
		result = new int[elements1.size()];
		for(int k = 0; k < result.length; k ++){
			result[k] = elements1.get(set1_lastIndex - k); //elements1 stored values in reverse order so
														  //it needs to be traversed from the end in order to store the elements in the results array in the right order
		}
		return result;
	}
	
	private int[] copySetElements(int[] set1, int[] set2){
		ArrayList<Integer> all_elements = new ArrayList<Integer>();
		int[] result;
		for(int i = 0; i < set1.length; i ++){
			all_elements.add(set1[i]);
		}
		for(int i = 0; i < set2.length; i ++){
			all_elements.add(set2[i]);
		}
		result = new int[all_elements.size()];
		for(int i = 0; i < result.length; i ++){
			result[i] = all_elements.get(i);
		}
		return result;
	}
	
	public int checkBranchVal()
	{
		int branchCondVal = 0;
		
		if(runStack[RSC-1][0]==identifier)
		{
			branchCondVal = Interpreter.symbolTable.getVal(runStack[RSC-1][1]);
		}
		else if(runStack[RSC-1][0]==constant)
		{
			branchCondVal = runStack[RSC-1][1];
		}
		
		return branchCondVal;
	}
	
	public int checkBranchPos()
	{
		int branchPos = 0;
		
		if(runStack[RSC][0]==identifier)
		{
			branchPos = Interpreter.symbolTable.getVal(runStack[RSC][1]);
		}
		else if(runStack[RSC][0]==constant)
		{
			branchPos = runStack[RSC][1];
		}
		return branchPos;
	}
	
	//pushes the result of the set union operation onto the runStack 
	public void set_union(int[] values, int pop_count){
		Hashtable<Integer, Integer> htable = new Hashtable<Integer, Integer>();
		RSC = RSC - pop_count; //simulates popping the set elements in the values array from the runStack
		int[] union_set = new int[values.length]; //will store the result of the set union operation
		int numbers_added = 0; //will store the number of set elements added to union_set
		for(int i = 0; i < values.length; i++){
			if(!htable.containsKey(values[i])){ //makes sure duplicates are not put in the union set
				htable.put(values[i], values[i]); //since each set must contain distinct elements, a set element's value could also act as a key in the hashtable
				union_set[numbers_added] = values[i];
				numbers_added++;
			}
		}
		for(int i = 0; i < numbers_added; i ++){ //push the elements of the union set unto the runstack
			Push_RunStack(set_element, union_set[i]); 
		}
	}
	
	//pushes the result of the set intersection operation onto the runStack
	public void set_intersection(int[] values, int pop_count){
		Hashtable<Integer, Integer> htable = new Hashtable<Integer, Integer>();
		RSC = RSC - pop_count; //simulates popping the set elements in the values array from the runStack
		int[] intersection_set = new int[values.length]; //will store the result of the set intersection operation
		int numbers_added = 0; //will store the number of set elements added to the intersection_set
		for(int i = 0; i < values.length; i++){
			/*only set elements that appear more than once are added to the intersection set*/
			if(!htable.containsKey(values[i])) //if set element is not in the hashtable then add it to the hashtable
				htable.put(values[i], values[i]);
			else{ //if set element is already in the hashtable then store it in the intersection_set
				intersection_set[numbers_added] = values[i];
				numbers_added++;
			}
		}
		for(int i = 0; i < numbers_added; i ++){ //push the elements of the intersection set unto the runStack
			Push_RunStack(set_element, intersection_set[i]); 
		}
	}
	
	private int changeToSetOperation(int operation){
		if(operation == plus)
			return union;
		else if(operation == mult)
			return intersection;
		return runtimeError;
	}
}
