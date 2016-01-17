import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class CharBufferedReader 
{
	private BufferedReader reader = null; 
	private int index; // index of the current char in fileLine
	private String fileLine;
	private boolean eof = false; //stores whether end of line has been reached
	public char eofReached = '&';
	public boolean skipLine = false;
	private String filename; //name of the file without the .txt extension
	
	public CharBufferedReader(String fileName, String file_extension) throws IOException
	{
		boolean exists = false;
		/*error check to ensure the filename entered exists*/
		while(!exists){
			try{
				reader = new BufferedReader(new FileReader(fileName + file_extension));
				exists = true;
				index = 0;
				this.filename = fileName; //sets the filename variable to the correct filename
				fileLine = reader.readLine();
			}
			catch(Exception e){
				Scanner scan = new Scanner(System.in);
				System.out.println("File does not exist, try again (without the .txt extension):");
				fileName = scan.next();
			}
		}
	}
	
	//returns the name of the file (without the .txt extension) being read from
	public String getFilename(){
		return filename;
	}
	

	public char GetChar() throws IOException
	{
		if(eof) //if end of file don't get next char
		{
			return eofReached;
		}
		
		if(skipLine){ //if current token = comment then skip entire line
			skipLine = false;
			
			//if there is still another line to read after the commented line then read the line and reset the char index to 0
			if((fileLine = reader.readLine()) !=null) 
			{
				index = 0;
				return ' ';
			}
			//if there is no line to read after the commented line then that is the end of the file
			else 
			{
				eof = true;
				return eofReached;
			}
		}
		/* if the current char index  is greater or equal to the length of the line read then the 
		 end of the line is reached and time to read the next line in the file*/
		else if (fileLine.length() <= index) 
		{
			if((fileLine = reader.readLine()) == null) //if there is no next line to read then that marks the end of the file
			{
				eof = true;
				return eofReached;
			}
			else // if there is a line to read then that becomes the new line and the char index is reset to 0
			{
				index = 0;
				return ' ';
			}
		}
		
		/*if the function has not returned at this point then the current character index is not pointing to a null char because it has
		exceeded the line length nor is it the last char of the file then the function will return the character at that index position*/
		return fileLine.charAt(index++);
	}

}
