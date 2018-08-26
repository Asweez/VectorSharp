package vectorsharp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class VectorSharp {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException{
		Display display = new Display();
		File file = new File("code.vsharp");
		if(!file.exists()){ 
			file.createNewFile();
			PrintWriter writer = new PrintWriter(file);
			writer.write(" ");
			writer.close();
		}
		String code = new Scanner(file).useDelimiter("\\Z").next();
		display.display(code);
	}
}
