package authorRanking;

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

import utils.MiscUtils;

/**
 * This class generate features related to authorRanking provided in the meta-data
 * @author Aya Zirikly
 * 
 */
public class Features {

	MiscUtils miscUtils;
	private static final Object [] FILE_HEADER_TRAIN =
		{"docID", "body", "authorRanking", "authorRankingSummary", "label"};

	private static final Object [] FILE_HEADER_TEST =
		{"docID", "body", "authorRanking", "authorRankingSummary"};

	public Features(){
		miscUtils = new MiscUtils();
	}

	/**
	 * 
	 * @param csvFile The train/test file which contains all the info from the XML along with the label
	 * @param outputFile The output to write to
	 * @param authorRankingFile The author ranking file provided by shared-task
	 * @param authorRankingSummaryFile The author ranking summary file provided by shared-task
	 * @param train Enable if training to write the label
	 */
	public void writeAuthorFeatures(String csvFile, String outputFile, 
			String authorRankingFile, String authorRankingSummaryFile, 
			boolean train){

		HashMap<String, String> userID2authorRankingDesc = 
				miscUtils.userID2authorRankingDesc(authorRankingFile);

		HashMap<String, Integer> rankingSummary = 
				miscUtils.loadAuthorRankingSummary(authorRankingSummaryFile);
		HashMap<String, Integer> userID2role = 
				miscUtils.userID2role(authorRankingFile, rankingSummary);


		CSVParser parser;
		FileWriter fileWriter = null;

		//Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter csvFilePrinter = null;

		try{
			parser = new CSVParser(
					new FileReader(csvFile), 
					CSVFormat.DEFAULT.withHeader());

			fileWriter = new FileWriter(outputFile);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			// add header
			if (train)
				csvFilePrinter.printRecord(FILE_HEADER_TRAIN);
			else
				csvFilePrinter.printRecord(FILE_HEADER_TEST);


			List<String> toWriteRecord = new ArrayList<String>();
			String userID;
			for (CSVRecord record : parser) {
				toWriteRecord.clear();
				toWriteRecord.add(record.get("id"));
				toWriteRecord.add(record.get("body"));
				userID = record.get("author").substring(
						record.get("author").lastIndexOf("/") + 1
						);

				// Write author ranking full description
				toWriteRecord.add(userID2authorRankingDesc.get(userID));
				// Write author role (mod vs. user)
				toWriteRecord.add(userID2role.get(userID) + "");

				// add label if train
				if (train)
					toWriteRecord.add(record.get("label"));

				csvFilePrinter.printRecord(toWriteRecord);
			}
		}catch (IOException e){
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
