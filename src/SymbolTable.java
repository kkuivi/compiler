
public class SymbolTable {

	public SymbolTableElement[] symTable;
	private int freeIndex;
	private int freeTempIndex;  
	
	public SymbolTable()
	{
		symTable = new SymbolTableElement[100];
		
		for(int i=0; i<symTable.length; i++)
		{
			symTable[i] = new SymbolTableElement();
		}
		
		freeIndex = 1;	//identifiers will be stored in symbol table starting at index 1
		freeTempIndex = 50; //temp values will be stored in symbol table starting at index 50
	}
	
	public void addElement(int type, String name, int val)
	{
		symTable[freeIndex] = new SymbolTableElement();
		symTable[freeIndex].setType(type);
		symTable[freeIndex].setName(name);
		symTable[freeIndex].setValue(val);
		freeIndex+=1;
	}
	public void addElement(int type, String name)
	{
		symTable[freeIndex] = new SymbolTableElement();
		symTable[freeIndex].setType(type);
		symTable[freeIndex].setName(name);
		freeIndex+=1;
	}
	public void addTempElement(int type, String name, int val)
	{
		symTable[freeTempIndex] = new SymbolTableElement();
		symTable[freeTempIndex].setType(type);
		symTable[freeTempIndex].setName(name);
		symTable[freeTempIndex].setValue(val);
		freeTempIndex+=1;
	}
	public void addSetElement(int type, String name,int max_size, int[] values){
		symTable[freeIndex] = new SymbolTableElement();
		symTable[freeIndex].setType(type);
		symTable[freeIndex].setName(name);
		symTable[freeIndex].setMaxSize(max_size);
		symTable[freeIndex].set_SetValue(values);
		freeIndex+=1;
	}
	public void addSetElement(int type, String name, int max_size){
		symTable[freeIndex] = new SymbolTableElement();
		symTable[freeIndex].setType(type);
		symTable[freeIndex].setName(name);
		symTable[freeIndex].setMaxSize(max_size);
		freeIndex+=1;
	}
	public int find(String str)
	{
		for(int i=1; i<symTable.length; i++)
		{
			if(symTable[i]!=null && symTable[i].name!=null)
			{
				if(symTable[i].name.equals(str))
				{
					return i;
				}
			}
		}
		System.out.println("ERROR: Variable " + str + " not found in symbol table");
		return -1000; 
	}
	public int getVal(int position) {
		if(symTable[position].initialized()){ //if the variable at the given position is initialized then return it's value 
			return symTable[position].getValue();
		}
		System.out.println("ERROR: Variable not initialized");
		return -1000; 
	}
	public int[] getSetVal(int position) {
		int[] zero_set = {0,0,0,0};
		if(symTable[position].initialized()){ //if the set variable at the given position is initialized then return it's value
			return symTable[position].get_setValue();
		}
		System.out.println("ERROR: Variable not initialized");
		return zero_set; 
	}
	public int getType(int position){
		if(symTable[position].name != null)
			return symTable[position].getType();
		System.out.println("ERROR: Variable not declared");
		return -1000;
	}
}
	

