package liwc;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * This class generate LIWC features
 * @author Aya Zirikly
 *
 */
public class PostsCatProcessing {

	Utils liwcUtils;
	public PostsCatProcessing(){
		liwcUtils = new Utils(); 
	}

	private Object[] generateCsvHeader(String type, ArrayList<Integer> categories){
		int catSize = categories.size();
		Object[] header = new Object[6 + catSize];
		header[0] = "docID";
		switch (type){
		case "word": header[1] = "body";
		break;
		case "token": header[1] = "tokens";
		break;
		case "lemma": header[1] = "lemmas";
		break;
		}
		header[2] = "id";
		header[3] = "kudos_count";
		header[4] = "views";
		int i = 0;
		while (i < catSize){
			header[i + 5] = categories.get(i);
			++i;
		}
		header[header.length - 1] = "label";
		return header;
	}

	private Object[] generateCsvHeader(String type, HashMap<Integer, String> categories, boolean train){
		int catSize = categories.size();
		Object[] header;
		if (train)
			header = new Object[6 + catSize];
		else
			header = new Object[5 + catSize];

		header[0] = "docID";
		switch (type){
		// switch from body to avoid duplication with one of LIWC category
		case "word": header[1] = "word";
		break;
		case "token": header[1] = "tokens";
		break;
		case "lemma": header[1] = "lemmas";
		break;
		}
		header[2] = "id";
		header[3] = "kudos_count";
		header[4] = "views";
		int i = 0;
		for (Integer cat: categories.keySet()){
			header[i + 5] = categories.get(cat);
			++i;
		}
		if (train)
			header[header.length - 1] = "label";
		return header;
	}

	public void writeLIWCCategories(String inputCsvFile, 
			String liwcFeatFile, 
			String type, 
			String catFile, String wordFile,
			boolean train){


		// Load the categories
		HashMap<Integer, String> id2catName =  ReadDic.id2catName(catFile);
		ArrayList<Integer> categories = new ArrayList<Integer>();
		categories.addAll(id2catName.keySet());

		CSVParser parser;
		FileWriter fileWriter = null;
		//Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter csvFilePrinter = null;
		try{
			parser = new CSVParser(
					new FileReader(inputCsvFile), 
					CSVFormat.DEFAULT.withHeader());
			fileWriter = new FileWriter(liwcFeatFile);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			// add header
			csvFilePrinter.printRecord(generateCsvHeader(type, id2catName, train));
			List<String> toWriteRecord;
			String text = "";
			HashMap<Integer, Integer> catFreq;
			int lineNum = 1;
			Integer freq = 0;
			for (CSVRecord record : parser) {
				System.out.println(lineNum++);

				toWriteRecord = new ArrayList<String>();
				toWriteRecord.add(record.get("docID"));
				switch(type){
				case "word": text = record.get("body");
				break;
				case "token": text = record.get("tokens");
				break;
				case "lemma": text = record.get("lemmas");
				break;
				}
				toWriteRecord.add(text);
				toWriteRecord.add(record.get("id"));
				toWriteRecord.add(record.get("kudos_count"));
				toWriteRecord.add(record.get("views"));
				// add categories values
				catFreq = liwcUtils.getLIWCcatFreq(
						text.replaceAll("#@#", " "), 
						catFile, wordFile);
				for (Integer cat: id2catName.keySet()){
					freq = catFreq.get(cat);
					if (freq == null) freq = 0;
					toWriteRecord.add(Integer.toString(freq));
				}
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
