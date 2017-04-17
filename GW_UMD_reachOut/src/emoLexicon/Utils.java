package emoLexicon;

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
 * This class generates 10 emotional features based on NRC lexicon
 * http://www.saifmohammad.com/WebPages/NRC-Emotion-Lexicon.htm
 * @author Aya Zirikly
 *
 */
public class Utils {

	private static final Object [] FILE_HEADER_TRAIN = 
		{"docID", "body",
				"anger", "anticipation", "disgust", "fear", "joy", 
				"negative", "positive", "sadness", "surprise", "trust", 
		"label"};

	private static final Object [] FILE_HEADER_TEST = 
		{"docID", "body",
				"anger", "anticipation", "disgust", "fear", "joy", 
				"negative", "positive", "sadness", "surprise", "trust"};

	/**
	 * 
	 * @param lexicon NRC lexicon [please download from website mentioned above]
	 * @return Hashmap of words to their emotional values 
	 */
	public HashMap<String, ArrayList<String>> loadNRClexicon(String lexicon){
		HashMap<String, ArrayList<String>> nrcLexicon = 
				new HashMap<String, ArrayList<String>>();
		BufferedReader br;
		try{
			br = Files.newBufferedReader(Paths.get(lexicon), StandardCharsets.UTF_8);
			String line = "";
			String[] fields;
			String word, emoVal;
			ArrayList<String> emoVec;
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				if (fields.length == 3){
					word = fields[0];
					emoVal = fields[2];
					emoVec = nrcLexicon.get(word);
					if (emoVec == null)
						emoVec = new ArrayList<String>();
					emoVec.add(emoVal);
					nrcLexicon.put(word, emoVec);
				}
			}
			br.close();
		} catch (IOException e){
			System.err.println("Lexicon File Does Not Exist");
		}
		return nrcLexicon;
	}

	/**
	 * 
	 * @param nrcLexicon The map of NRC lexicon
	 * @param text  The text that we would like to generate its frequency emotion vector
	 * @return An array of size 10 (emotional dimensions) where the values are the frequencies
	 */
	public int[] generateEmoVectorText(
			HashMap<String, ArrayList<String>> nrcLexicon, 
			String text){
		int[] emoVectorText = new int[10];
		String[] words = text.split("\\s+");
		ArrayList<String> emoVector;
		for (String word: words){
			emoVector = nrcLexicon.get(word);
			// Increment frequencies
			if (emoVector != null)
				for (int i = 0; i < emoVectorText.length; i++){
					emoVectorText[i] += Integer.parseInt(emoVector.get(i));
				}
		}
		return emoVectorText;
	}

	/**
	 * 
	 * @param nrcLexiconFile
	 * @param csvTagFile Train/Test file that contains the xml info in addition to preprocessing info (lemmas and tokens)
	 * @param outputFile
	 * @param wordUnitType The word preprocessing level that we would like to generate emotion frequency vector for {word, lemma, token} 
	 * @param train
	 */
	public void generateEmoVector(String nrcLexiconFile, 
			String csvTagFile, String outputFile, 
			String wordUnitType, 
			boolean train){

		HashMap<String, ArrayList<String>> nrcLexicon = loadNRClexicon(nrcLexiconFile);
		CSVParser parser;
		FileWriter fileWriter = null;

		//Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter csvFilePrinter = null;
		try{
			fileWriter = new FileWriter(outputFile + "_" + wordUnitType + ".csv");
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			// add header
			if (train)
				csvFilePrinter.printRecord(FILE_HEADER_TRAIN);
			else
				csvFilePrinter.printRecord(FILE_HEADER_TEST);

			System.out.println(csvTagFile);

			parser = new CSVParser(
					new FileReader(csvTagFile), 
					CSVFormat.DEFAULT.withHeader());


			List<String> toWriteRecord = new ArrayList<String>();
			String text = "";
			int[] emoValues;

			for (CSVRecord record : parser) {
				toWriteRecord.clear();
				//				toWriteRecord.add(record.get("id"));
				switch(wordUnitType){
				case ("token"): text = record.get("tokens");
				break;
				case ("lemma"): text = record.get("lemmas");
				break;
				case ("word"): text = record.get("body");
				break;
				}

				text = text
						.replaceAll("&nbsp;", " ")
						.replaceAll("&lt;", "")
						.replaceAll("#@#", " ") 
						.replaceAll("-RRB-", ")")
						.replaceAll("-LRB-", "(");

				//				toWriteRecord.add(text);
				emoValues = generateEmoVectorText(nrcLexicon, text);
				for (int emoVal: emoValues){
					toWriteRecord.add(Integer.toString(emoVal));
				}
				if (train)
					toWriteRecord.add(record.get("label"));

				csvFilePrinter.printRecord(toWriteRecord);
			} // end loop over records
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
