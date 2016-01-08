
public class Execute {

	private int PFC, RSC;
	private int[][] runStack; //runStack 2D array will hold operands( identifiers and constants are two part tokens)
	
	final int symTableError = -1000;
	
	//operand types
	final int identifier = 1;
	final int constant = 2;
	final int set_element =50; //used to denote a set element
	
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
	
	public Execute()
	{
		PFC = 0;
		RSC = 0;
		runStack = new int[50][2];
	}
	
	public void Push_RunStack(int x, int y)
	{
		RSC++;
		runStack[RSC][0] = x;
		runStack[RSC][1] = y;
	}
	
	public void RunProg(int[] postfix) 
	{
		boolean error = false;
		System.out.println("Program Output:");
		while(PFC < Interpreter.PFPtr && !error){
			/*
			System.out.println("Current Run Stack:");
			for(int i=0; i<5; i++)
			{
				System.out.println(runStack[i][0] + "     "+runStack[i][1]);
				System.out.println();
			}
			System.out.println("RUNSTACK PTR: "+ RSC);
			System.out.println("PFC: "+ PFC);
			*/
			int operand1 = 0;
			int operand2 = 0;
			int pos = 0;
			int branchCondVal = 0;
			int branchPos = 0;
		
			switch (postfix[PFC])
			{
			case constant:
				Push_RunStack(postfix[PFC], postfix[PFC+1]);
				PFC+=2;		//identifiers and constants are two part tokens
				break;
			case identifier:
				Push_RunStack(postfix[PFC], postfix[PFC+1]);
				PFC+=2;		//identifiers and constants are two part tokens
				break;
			case assign:
				//find value of operand1
				int[] set_values1 = new int [10]; //will store current list of 
				                                 //elements in set- maximum size is 
				                                 //10
				int[] set_values2 = {0,0,0,0} ; 
				                   //everything in set_values1 will be stored here
				                  // and its size will represent actual number of 
				                 //elements
				int i = 0; //used to check how far RSC went back when assigning elements
				       //to a set
				//find value of operand1
				if(runStack[RSC][0]==identifier) //runStack-->[identifier][symTablePosition]
				{
					operand1 = Interpreter.symbolTable.getVal(runStack[RSC][1]); 
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
					set_values2 = new int[counter];
					for(int k = 0; k < set_values2.length; k ++){
						set_values2[k] = set_values1[k];
					}
				}
				
				//assign value of operand1 to identifier
				if((runStack[RSC][0] == constant) || (runStack[RSC][0] == identifier))
				{
					pos = runStack[RSC-1][1];
					Interpreter.symbolTable.symTable[pos].setValue(operand1);
				
					RSC = RSC-2;
					PFC++;
				}
				else if(runStack[RSC][0] == set_element)
				{
					pos = runStack[i][1];
					Interpreter.symbolTable.symTable[pos].set_SetValue(set_values2);
					
					RSC = i - 1;
					PFC++;
				}
				break;
			case plus:
				int sum = binaryOperation(plus);
				if(sum!=symTableError){
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
				if(difference!=symTableError){
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
				if(product!=symTableError){
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
				if(quotient!=symTableError){
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
					temp=operand1-(operand1*2);
					
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
				}
				else if(runStack[RSC][0]==constant)
				{
					operand1 = runStack[RSC][1];
					System.out.println(operand1);
				}
				//else if quote..
				//else if set..
				else
				{
					System.out.println("runtime error");
					error = true;
				}
				PFC++;
				RSC--;
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
		
		//find value of operand1
		if(runStack[RSC-1][0]==identifier) //runStack-->[identifier][symTablePosition]
		{
			if(Interpreter.symbolTable.getVal(runStack[RSC-1][1])!=symTableError)
			{
				operand1 = Interpreter.symbolTable.getVal(runStack[RSC-1][1]);
			}
			else 
			{
				System.out.println("ERROR"); //variable was not initialized before using
				//break;
				return symTableError;
			}
		}
		else if(runStack[RSC-1][0]==constant) //runStack-->[constant][value]
		{
			operand1 = runStack[RSC-1][1]; 
		}
		else
		{
			//error
		}
		
		//find value of operand2
		if(runStack[RSC][0]==identifier) //runStack-->[identifier][symTablePosition]
		{
			if(Interpreter.symbolTable.getVal(runStack[RSC][1])!=symTableError)
			{
				operand2 = Interpreter.symbolTable.getVal(runStack[RSC][1]);
			}
			else 
			{
				System.out.println("ERROR"); //variable was not initialized before using
				//break;
				return symTableError;
			}
		}
		else if(runStack[RSC][0]==constant) //runStack-->[constant][value]
		{
			operand2 = runStack[RSC][1]; 
		}
		else 
		{
			//error
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
			default:
				break;
		 }
		return value;
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
}
