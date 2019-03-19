/**
 * 
 */

/**
 * @author ABWAR
 *
 */

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.*;

public class Main {
	
	private static String filename;
	private static double support;
	private static double confidence;
	
	public static void main(String[] args) throws IOException{
		// Input filename, from user 
		System.out.print("Welcome to Rule Power Factor Miner!\nPlease enter data file's name: ");
		Scanner in = new Scanner(System.in);
		filename = in.nextLine();
				
		// Input support, from user
		// Accepts a number between 0 and 100
		int reloop;
		do {
			reloop = 0;
			System.out.print("Please enter support percentage or a decimal: ");
			in = new Scanner(System.in);
			try {
				support = in.nextDouble();
				// If input support is in percentage
				if (support>=1 && support<=100) {
					support = support/100;
				}
				// If input support is in decimal ranging 0-1
				while (support>1 || support<0) {
					System.out.print("Invalid number, please enter a percentage or a decimal: ");
					support = in.nextDouble();
					if (support>=1 && support<=100) {
						support = support/100;
					}
				}
			// Raising exception if input is in any other format	
			} catch(Exception e) {
				System.out.println("Incorrect format, please enter a percentage or a decimal: ");
				reloop++;
			}
		} while(reloop != 0);

		
		// Input confidence, from user
		// Accepts a number between 0 and 100
		do {
			reloop = 0;
			System.out.print("Please enter confidence percentage: ");
			in = new Scanner(System.in);
			try {
				confidence = in.nextDouble();
				// If input confidence is in percentage
				if (confidence>=1 && confidence<=100) {
					confidence = confidence/100;
				}
				// If input confidence is in decimal ranging 0-1
				while (confidence>1 || confidence<0) {
					System.out.print("Invalid number, please enter a percentage between 0 and 100: ");
					confidence = in.nextDouble();
					if (confidence>=1 && confidence<=100) {
						confidence = confidence/100;
					}
				}
			// Raising exception if input is in any other format	
			} catch(Exception e) {
				System.out.println("Incorrect format, please enter a percentage between 0 and 100: ");
				reloop++;
			}
		} while(reloop != 0);

		// variable "in" is assigned to a scanner instance and closing the scanner
		in.close();

		// Counting the rows in the input data set
		int numberOfRows = 0;
		List<List<String>> Data = new ArrayList<List<String>>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(filename));
			// Separating the first line and setting up the tags to add to the data
			String line = bufferedReader.readLine();
			String[] tags = line.split(" +");

			// Loops through the file, reading line by line
			// Splits the line, then adds the tags to the corresponding value
			// Adds the split line into the data array
			line = bufferedReader.readLine();
			while (line != null) {
				String[] split = line.split(" +");
				if(split.length == tags.length) {
					numberOfRows++;
					for (int i = 0; i < split.length; i++) {
						split[i] = tags[i] + "=" + split[i];
					}
				}

				Data.add(Arrays.asList(split));
				line = bufferedReader.readLine();
			}
		// Raising exception if "filename" given as input do not match with any file in the path  	
		} catch(Exception e) {
			System.out.println("No file found.");
			return;
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}
			catch (Exception e) {
				System.out.println("Error closing the reader");
			}
		}

		// Passes the input to the RulePowerFactor interest measure algorithm. Gets a list of rules in return.
		List<String> rules = RulePowerFactor.runRulePowerFactor(Data, support, confidence);

		// Outputs the summary and discovered rules to a file named "Rules"
		// Overwrites a file if it already exists, and creates one if it does not.
		BufferedWriter bufferedWriter = null; // declared outside so to use it again outside block
		try {
			File outPut = new File("Rules");
			if (!outPut.exists()) {
				outPut.createNewFile();
			}
			
			FileWriter fileWriter = new FileWriter(outPut);
			bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("Run Information: \n");
			bufferedWriter.write("Total number of rows in the original set: " + numberOfRows + "\n");
			bufferedWriter.write("Total number of rules discovered: " + rules.size() + "\n");
			bufferedWriter.write("The selected measures: Support=" + support + ", Confidence=" + confidence + "\n");
			bufferedWriter.write("--------------------------------------\n");
			bufferedWriter.write("Discovered rules:\n\n" + rules);
		} catch(Exception e) {
			System.out.println("Error outputting the rules.");
		} finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
			} catch(Exception e) {
				System.out.println("Error closing the writer.");
			}
		}
		//Outputting File "Rules" containing generated rules 
		
		BufferedReader br = new BufferedReader(new FileReader("Rules"));
        	for (String line; (line = br.readLine()) != null;) {
          	System.out.println(line);
        }
        br.close();
		
		//.............
	}
	
}
