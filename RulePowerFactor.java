/**
 * 
 */

/**
 * @author ABWAR
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;
import java.math.MathContext;

public class RulePowerFactor {
	
	private static List<List<String>> inputData;
	private static double minimumSupport, minimumConfidence;
	
	private static List<KeyValue> finalTable;
	
	//Arguments passed are input data set items and input support and confidence values
	public static List<String> runRulePowerFactor(List<List<String>> data, double inputSupport, double inputConfidence) {
		inputData = data;
		minimumSupport = inputSupport;
		minimumConfidence = inputConfidence;
		finalTable = new ArrayList<KeyValue>();
 		List<KeyValue> frequentItemsetTable = generateTables();
		
		// Association aspect of the algorithm to generate the rules
		List<String> rules = runAssociation(finalTable);
		List<String> output = new ArrayList<String>();

		rules = removeDuplicates(rules);
		
		for(int i = 0; i <rules.size(); i++) {
			output.add("Rule#" + (i + 1) + ": " + rules.get(i) + "\n");
		}
		
		return output;
	}
	
	// Begins the Association aspect of the algorithm
	private static List<String> runAssociation(List<KeyValue> frequentItemsetTable) {
		List<Rule> rules = new ArrayList<Rule>();
		// Breaking the table apart into rows
		for(int r = 0; r < frequentItemsetTable.size(); r++) {
			rules.addAll(rulesForRow(frequentItemsetTable.get(r)));
		}
		
		// Purging repeats from the rules generated from the two sets of rows in the table
		for(int i = rules.size() - 1; i >= 0; i--) {
			if(rules.get(i).hasRepeat() || rules.get(i).confidence < minimumConfidence) {
				rules.remove(i);
			}
		}
								
		List<String> output = new ArrayList<String>();
		for(int i = 0; i < rules.size(); i++) {
			output.add(rules.get(i).toString());
		}
		
		return output;
	}
	
	// Generates the rules for the row of the frequency table
	private static List<Rule> rulesForRow(KeyValue rowValue) {
		
		// Determines all the subsets required for the row
		List<List<String>> subsets = genSubsets(rowValue.itemSet);
		
		List<Rule> rules = generateImplications(subsets);
		
		return rules;
	}
	
	// Finds implications (as implication of the form X→Y where X, Y subset of I)
	private static List<Rule> generateImplications(List<List<String>> subsets) {
		List<Rule> rules = new ArrayList<Rule>();
		
		for(int r = 0; r < subsets.size(); r++) {
			List<Item> base = new ArrayList<Item>();
			for(int c = 0; c < subsets.get(r).size(); c++) {
				base.add(new Item(subsets.get(r).get(c)));
			}
			
			for(int ri = 0; ri < subsets.size(); ri++) {
				List<Item> implication = new ArrayList<Item>();
				for(int ci = 0; ci < subsets.get(ri).size(); ci++) {
					implication.add(new Item(subsets.get(ri).get(ci)));
				}
				rules.add(new Rule(base, implication));
			}
		}
		
		// Purging repeats
		for(int i = rules.size() - 1; i >= 0; i--) {
			if(rules.get(i).hasRepeat()) {
				rules.remove(i);
			}
		}
		
		return rules;
	}
	
	// Breaks the set into all the subsets needed for association
	private static List<List<String>> genSubsets(List<Item> initialSets) {
		 List<List<String>> subsets = new ArrayList<List<String>>();
		 // Convert the item list to a string list for processing
		 List<String> setString = new ArrayList<String>();
		 for(int i = 0; i <initialSets.size(); i++) {
			 setString.add(initialSets.get(i).value);
		 }
		 
		 int numberOfSets = setString.size();
		 for (int i = 1; i < numberOfSets; i++)
			 subsets.addAll(combination(setString, i));
		 
		 return subsets;
	}
		
	// Generates the tables until there are none left
	private static List<KeyValue> generateTables() {
		
		List<List<String>> curDataSets;
		List<Item> uniqueItems;
		List<KeyValue> candidateTable;
		List<KeyValue> frequentItemsetTable;
		
		uniqueItems = findUniquesInData(inputData);
		candidateTable = buildFirstCandidate(uniqueItems);
		frequentItemsetTable = buildFrequentItemset(candidateTable);
		
		List<KeyValue> previousFrequentTable = new ArrayList<KeyValue>();
		
		int iteration = 2;
		while(!frequentItemsetTable.isEmpty()) {
			previousFrequentTable = frequentItemsetTable;
			curDataSets = expandItemSet(frequentItemsetTable, iteration);
			candidateTable = buildCandidate(curDataSets);

			frequentItemsetTable = buildFrequentItemset(candidateTable); // this was here previously
			iteration++;
		}
		
		return previousFrequentTable;
	}
	
	// Expands the itemSets to the all possible combinations so as to ease the task of finding support and confidence of the rules to be generated
	private static List<List<String>> expandItemSet(List<KeyValue> table, int n) {
		
		List<String> curData = convertTableToData(table);
		List<List<String>> expandSet = new LinkedList<List<String>>();

	    expandSet.addAll(combination(curData, n));
		return expandSet;
	}
	
	// Generating all possible combinations of an array
	public static <T> List<List<T>> combination(List<T> values, int size) {

	    if (0 == size) {
	        return Collections.singletonList(Collections.<T> emptyList());
	    }

	    if (values.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<List<T>> combination = new LinkedList<List<T>>();
	    T actual = values.iterator().next();
	    List<T> subSet = new LinkedList<T>(values);
	    subSet.remove(actual);
	    List<List<T>> subSetCombination = combination(subSet, size - 1);

	    for (List<T> set : subSetCombination) {
	        List<T> newSet = new LinkedList<T>(set);
	        newSet.add(0, actual);
	        combination.add(newSet);
	    }

	    combination.addAll(combination(subSet, size));
	    return combination;
	}
	// Converts a table into the same dataType this is read on input
	private static List<String> convertTableToData(List<KeyValue> table) {
		
		List<String> newData = new ArrayList<String>();
		
		for(int r = 0; r < table.size(); r++) {
			for(int c = 0; c < table.get(r).itemSet.size(); c++) {
				newData.add(table.get(r).itemSet.get(c).value);
			}
		}	
		
		return removeDuplicates(newData);
	}
	
	// Calculates the support value on an itemSet
	public static double calculateSupport(List<Item> itemsChecked) {
		int numSupport = 0;

		for(int r = 0; r < inputData.size(); r++ ) {
			int numCheck = itemsChecked.size();
			for(int i = 0; i < itemsChecked.size(); i++) {
				for(int c = 0; c < inputData.get(r).size(); c++) {
					if(itemsChecked.get(i).value.equals(inputData.get(r).get(c))) {
						numCheck--;
					}
				}
				if(numCheck == 0) {
					numSupport++;
				}
			}
		}
		return numSupport / ((double)inputData.size());
	}
	
	// Builds the frequency table
	private static List<KeyValue> buildFrequentItemset(List<KeyValue> candidate) {

		for(int r = candidate.size() - 1; r >= 0; r--) {
			if(candidate.get(r).support < minimumSupport) {
				candidate.remove(r);
			}
		}
		
		for(int i = 0; i < candidate.size(); i++) {
			finalTable.add(candidate.get(i));
		}
		return candidate;
	}
	
	// Builds the typical candidate table
	private static List<KeyValue> buildCandidate(List<List<String>> itemSets) {
		List<KeyValue> candidateTable = new ArrayList<KeyValue>();
		
		// Counting the occurrence of each itemSet i.e., populating the items
		for(int i = 0; i < itemSets.size(); i++) {
			List<Item> entry = new ArrayList<Item>();
			for(int j = 0; j < itemSets.get(i).size(); j++) {
				entry.add(new Item(itemSets.get(i).get(j)));
			}
			candidateTable.add(new KeyValue(entry, calculateSupport(entry)));
		}
		return candidateTable;
	}
	
	// Builds the first candidate table
	private static List<KeyValue> buildFirstCandidate(List<Item> itemSets) {
		List<KeyValue> candidateTable = new ArrayList<KeyValue>();
		
		// Counting the occurrence of each itemSet i.e., Populating all the items
		for(int i = 0; i < itemSets.size(); i++) {
			List<Item> entry = new ArrayList<Item>();
			entry.add(itemSets.get(i));
 			candidateTable.add(new KeyValue(entry, calculateSupport(entry)));
		}
		return candidateTable;
	}
	
	
	// Finds all of the unique items in the data
	private static List<Item> findUniquesInData(List<List<String>> data) {
		List<String> allItems = new ArrayList<String>();
		
		for(int r = 0; r < data.size(); r ++) {
			for(int c = 0; c < data.get(r).size(); c++) {
				allItems.add(data.get(r).get(c));
			}
		}
		
		// Removing duplicates
		List<String> uniques = removeDuplicates(allItems);

		List<Item> uniqueItems = new ArrayList<Item>();
		for(int i = 0; i < uniques.size(); i++) {
			uniqueItems.add(new Item(uniques.get(i)));
		}
		
		return uniqueItems;
	}
	
	// Removes duplicates from a list of strings
	private static List<String> removeDuplicates(List<String> withDups) {
		// Removing repeated elements from arraylist
		Set<String> hs = new HashSet<>();
		hs.addAll(withDups);
		List<String> uniques = new ArrayList<String>();
		uniques.addAll(hs);
		
		return uniques;
	}
	
} // end of the RulePowerFactor class

// A table row in both candidate and frequency tables
class KeyValue {
	List<Item> itemSet;
	double support;
	public KeyValue(List<Item> itemSet, double support) {
		this.itemSet = itemSet;
		this.support = support;
	}
}

// The item and its value
class Item {
	String value;
	public Item(String value) {
		this.value = value;
	}
}

class Rule {
	List<Item> base;
	List<Item> implies;
	double confidence;
	double support;
	double rulepowerfactor;
	
	public Rule(List<Item> base, List<Item> implies) {
		this.base = base;
		this.implies = implies;
		calcConfidence();
	}
	
	public String toString() {
		
		String baseString = "{ ";
		String impString = "{ ";
		
		for(int i = 0; i < base.size(); i++) 
			baseString += base.get(i).value + " ";
		baseString += "}";
		
		for(int i = 0; i < implies.size(); i++) 
			impString += implies.get(i).value + " ";
		impString += "}";
		
		return "(Support=" + support + ", " + "Confidence=" + confidence + ", " + "RulePowerFactor=" 
				+ String.format("%.2f",support*confidence) + ") " + baseString + "----> " + impString;
	}
	
	public boolean hasRepeat() {
		boolean result = false;

		for(int b = 0; b < base.size(); b++) {
			for(int i = 0; i < implies.size(); i++) {
				if(base.get(b).value.equals(implies.get(i).value)) {
					result = true;
					return result;
				}
			}
		}
		return false;
	}
	// Determines the confidence value of the rule, also fills in the support
	public void calcConfidence() {

		double baseSupport = RulePowerFactor.calculateSupport(base);
		List<Item> entireSet = new ArrayList<Item>();
		for(int i = 0; i < base.size(); i++) {
			entireSet.add(base.get(i));
		}
		for(int i = 0; i < implies.size(); i++) {
			entireSet.add(implies.get(i));
		}
		double allSupport = RulePowerFactor.calculateSupport(entireSet);
		
		double result = allSupport / baseSupport;
		
		// Rounding a decimal and truncating it to 2 decimals so as to use it again as a number hence avoided formating the number to required decimal places
		BigDecimal bigDecimal = new BigDecimal(result);
		bigDecimal = bigDecimal.round(new MathContext(2));
		confidence = bigDecimal.doubleValue();
		
		bigDecimal = new BigDecimal(allSupport);
		bigDecimal = bigDecimal.round(new MathContext(2));
		support = bigDecimal.doubleValue();
	}

}
