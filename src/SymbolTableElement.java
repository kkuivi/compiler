/*
 * It extends Interpreter so that we can use the set_values array
 * This will be used to store values in each set
 */
public class SymbolTableElement extends Interpreter {
	public int type;
	public String name;
	public int value; //if type is a set then this stores its location in the set_values array; if type is integer then this stores its integer value
	public int max_size;
	public int curr_size;
	public boolean init;
	
	public SymbolTableElement()
	{
		init = false;
	}
	public void setType(int typ)
	{
		this.type = typ;
	}
	public void setName(String n)
	{
		this.name = n;
	}
	public void setValue(int val)
	{
		this.value = val;
		this.init = true; //once a value has been set to a variable then it is marked as initialized
	}
	public void set_SetValue(int[] values){
		this.value = free_set_location;
		for(int i = 0; i < values.length; i ++){
			set_values[free_set_location][i] = values[i]; 
		}
		free_set_location = free_set_location + 1;
		curr_size = values.length; //sets the current size of the set
		this.init = true;
	}
	public void setMaxSize(int maxSize){
		this.max_size = maxSize;
	}
	
	public int getType()
	{
		return this.type;
	}
	public String getName()
	{
		return this.name;
	}
	public int getValue(){
		return this.value;
	}
	public int[] get_setValue(){
		int[] setValue = new int[curr_size];
		for(int i = 0; i < setValue.length; i ++){
			setValue[i] = set_values[value][i];
		}
		return setValue;
	}
	public int get_setCurrSize(){
		return this.curr_size;
	}
	public boolean initialized(){
		return this.init;
	}
	public String toString() {
		
		if(this.type == kw_integer)
	        return String.format("type: %d, name: %s, value: %d", this.type, this.name, this.value);
			
			else
			{
				String set_elements="{";
				for(int i = 0; i < curr_size; i ++){
					if(i == curr_size - 1)
						set_elements = set_elements + set_values[value][i] + "}";
					else
						set_elements = set_elements + set_values[value][i] + ",";
				}
				return String.format("type: %d, name: %s, max_size: %d, current_size: %d,"
						+ "value: %s", this.type, this.name, this.max_size, this.curr_size, 
						set_elements);
			}
    }
}
