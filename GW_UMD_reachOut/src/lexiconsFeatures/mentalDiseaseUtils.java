package lexiconsFeatures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * This class generates mental-disease frequency-based features given a disease dictionaries 
 * [http://mental-health-matters.com/psychologicaldisorders/alphabetical-list-of-disorders]
 * @author Aya Zirikly
 *
 */

public class mentalDiseaseUtils {

	/**
	 * Load the mental disease dictionary 
	 * @param mentalDisDic
	 * @return
	 */
	private HashSet<String> readDic(String mentalDisDic){
		BufferedReader br;
		HashSet<String> dic = new HashSet<String>();
		System.out.println(mentalDisDic);
		try{
			br = Files.newBufferedReader(Paths.get(mentalDisDic), StandardCharsets.UTF_8);
			String line = "";
			while ((line = br.readLine()) != null){
				dic.add(line.trim());
			}
			br.close();
		} catch (IOException e){
			System.err.println("Dic Not Found");
			e.printStackTrace();
		}
		return dic;
	}

	private Object[] generateCsvHeader(String headerStr, String delimiter){
		String[] words  = headerStr.split(delimiter);
		Object[] header = new Object[words.length];

		for (int i = 0; i < words.length; i++){
			header[i] = words[i];
		}
		return header;
	}

	/**
	 * 
	 * @param dic Mental Disease dictionary 
	 * @param text Input text
	 * @return frequency of occurrence 
	 */
	private int dicTermsFreq(HashSet<String> dic, String text){
		int termsFreq = 0;
		String[] terms;

		for (String entry: dic){
			terms = entry.split("\\s+");
			if (text.toLowerCase().contains(entry)){
				termsFreq++;
			}
			else{
				// Check for patrial match
				for (String term: terms){
					if (text.toLowerCase().contains(term)){
						termsFreq++;
						// If partial match is true don't check for other terms once it is true
						break; 
					}
				}
			}
		}
		return termsFreq;
	}

	/**
	 * 
	 * @param postsCsv Train/Test input csv file
	 * @param mentalDicFeatFile Output file 
	 * @param mentalDisDic Input dictionary file 
	 * @param header What items to include in the output csv file (simplest would be just the mentalDiseaseFreq)
	 * @param delimiter
	 * @param train
	 */
	public void generateTermsFreq(String postsCsv, 
			String mentalDicFeatFile, 
			String mentalDisDic, 
			String header, String delimiter, 
			boolean train){

		HashSet<String> dic = readDic(mentalDisDic);
		CSVParser parser;
		FileWriter fileWriter = null;
		//Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter csvFilePrinter = null;
		try{
			parser = new CSVParser(
					new FileReader(postsCsv), 
					CSVFormat.DEFAULT.withHeader());
			fileWriter = new FileWriter(mentalDicFeatFile + ".csv");
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			// add header
			csvFilePrinter.printRecord(generateCsvHeader(header, delimiter));
			List<String> toWriteRecord = new ArrayList<String>();
			String text = "";
			int lineNum = 1;
			int dicTermFreqWords = 0;
			int dicTermFreqLemmas = 0;
			int dicTermFreqTokens = 0;

			for (CSVRecord record : parser) {
				System.out.println(lineNum++);
				// Check overlap with words
				text = record.get("body");
				dicTermFreqWords  = dicTermsFreq(dic, text);
				text = record.get("lemmas");
				dicTermFreqLemmas = dicTermsFreq(dic, text);
				text = record.get("tokens");
				dicTermFreqTokens = dicTermsFreq(dic, text);

				toWriteRecord.clear();
				/*
				 * We take the maximum frequency after comparing the test to the dictionary
				 * in terms of lemmas/words/tokens  
				 */
				toWriteRecord.add(Integer.toString(Math.max(
						Math.max(dicTermFreqWords, dicTermFreqLemmas),
						dicTermFreqTokens)));
				if (train)
					toWriteRecord.add(record.get("label"));
				csvFilePrinter.printRecord(toWriteRecord);
			}

		} catch (IOException e){
			System.err.println("File Handling Exception");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
				e.printStackTrace();
			}
		}

	}
}
