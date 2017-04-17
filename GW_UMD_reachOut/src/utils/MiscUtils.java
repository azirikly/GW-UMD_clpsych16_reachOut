package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * This class contains misc methods used by other classes
 * @author Aya Zirikly
 *
 */
public class MiscUtils {

	public HashMap<String, String> userID2authorRankingDesc(String authorRankingFile) {
		HashMap<String, String> userID2ranking = new HashMap<String, String>();
		BufferedReader br;

		try{
			br = Files.newBufferedReader(Paths.get(authorRankingFile), StandardCharsets.UTF_8);
			String line = "";
			String fields[];
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				userID2ranking.put(fields[0], fields[1].trim().toLowerCase());
			}
			br.close();
		} catch (IOException e){
			System.err.println("File Handling Exception");
			e.printStackTrace();
		}
		return userID2ranking;
	}

	public HashMap<String, Integer> userID2role(String authorRankingFile, 
			HashMap<String, Integer> rankingSummary) {
		HashMap<String, Integer> userID2role = new HashMap<String, Integer>();
		BufferedReader br;

		try{
			br = Files.newBufferedReader(Paths.get(authorRankingFile), StandardCharsets.UTF_8);
			String line = "";
			String fields[];
			Integer roleID;
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				roleID = rankingSummary.get(fields[1].trim().toLowerCase());
				userID2role.put(fields[0], roleID);
			}
			br.close();
		} catch (IOException e){
			System.err.println("File Handling Exception");
			e.printStackTrace();
		}
		return userID2role;
	}

	public HashMap<String, Integer> loadAuthorRankingSummary(String authorRankingSummaryFile) {
		BufferedReader br;
		HashMap<String, Integer> authorSummaries = new HashMap<String, Integer>();

		try{
			br = Files.newBufferedReader(Paths.get(authorRankingSummaryFile), StandardCharsets.UTF_8);
			String line = "";
			String fields[];
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				authorSummaries.put(fields[0].trim().toLowerCase(), Integer.parseInt(fields[1]));
			}
			br.close();
		} catch (IOException e){
			System.err.println("File Handling Exception");
			e.printStackTrace();
		}
		return authorSummaries;
	}

	/**
	 * 
	 * @param text
	 * @return True if the word has repetition
	 */
	private int hasRepetition(String text){
		String[] words = text.split("\\s+");
		char[] letters;
		for (String word: words){
			letters = word.toCharArray();
			for (int i = letters.length - 1; i > 2; i--){
				if (letters[i] != '.' && 
						letters[i] == letters[i-1] && 
						letters[i-1] == letters[i-2])
					return 1;
			}
		}
		return 0;
	}

	/**
	 * 
	 * @param text
	 * @return True if the word has all upper
	 */
	private int hasallUpper(String text){
		String[] words = text.split("\\s+");
		for (String word: words){
			if (word.toUpperCase().equals(word))
				return 1;
		}
		return 0;
	}


	public void writeAllCapsRepetitive(String inputCsvFile, String outputFile){
		CSVParser parser;
		FileWriter fileWriter = null;

		//Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter csvFilePrinter = null;
		try{
			fileWriter = new FileWriter(outputFile);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			Object[] header = {"docID", "body", "All_Caps", "RepetitiveLetters"};

			// add header
			csvFilePrinter.printRecord(header);

			System.out.println(inputCsvFile);
			parser = new CSVParser(
					new FileReader(inputCsvFile), 
					CSVFormat.DEFAULT.withHeader());
			List<String> toWriteRecord = new ArrayList<String>();
			String text = "";

			for (CSVRecord record : parser) {
				toWriteRecord.clear();
				toWriteRecord.add(record.get("id"));
				toWriteRecord.add(record.get("body"));
				text = record.get("body")
						.replaceAll("&lt;", " ")
						.replaceAll("#@#", " ")
						.replaceAll("-LRB-", "(")
						.replaceAll("-RRB-", ")");
				// Add All caps feature
				toWriteRecord.add(hasallUpper(text) + "");

				// Add repetitive letters
				toWriteRecord.add(hasRepetition(text) + "");
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
