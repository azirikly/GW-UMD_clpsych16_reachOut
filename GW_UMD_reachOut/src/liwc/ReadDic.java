package liwc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class has methods related to LIWC .dic file
 * @author Aya zirikly
 *
 */
public class ReadDic {

	public static HashMap<Integer, String> id2catName(String catFile){
		BufferedReader br;
		HashMap<Integer, String> id2cat = new HashMap<Integer, String>();
		try{
			br = Files.newBufferedReader(Paths.get(catFile), StandardCharsets.UTF_8);
			String line = "";
			String[] fields;
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				id2cat.put(new Integer(fields[0]), fields[1]);
			}
			br.close();
		} catch (IOException e){
			System.err.println("CATEGORIES File Handling Exception");
		}
		return id2cat;
	}

	public static HashMap<String, ArrayList<Integer>> word2cats(String wordFile){
		BufferedReader br;
		HashMap<String, ArrayList<Integer>> word2cats = 
				new HashMap<String, ArrayList<Integer>>();
		try{
			br = Files.newBufferedReader(Paths.get(wordFile), StandardCharsets.UTF_8);
			String line = "";
			String[] fields;
			ArrayList<Integer> catIDs;
			while ((line = br.readLine()) != null){
				fields = line.split("\\t");
				catIDs = new ArrayList<Integer>();
				if (fields[0].equals("kind")){
					catIDs.addAll(Arrays.asList(131,125,135,126));
				}
				else if (fields[0].equals("like")){
					catIDs.addAll(Arrays.asList(02,134,125,464,126,253));
				}
				else{
					for (int i = 1; i < fields.length; ++i){
						catIDs.add(new Integer(fields[i]));
					}
					if (fields[0].endsWith("*"))
						word2cats.put("^" + fields[0].replace("*", ".*$"), catIDs);
					else
						word2cats.put(fields[0], catIDs);
				}
			}
			br.close();
		} catch (IOException e){
			System.err.println("WORDS File Handling Exception");
			e.printStackTrace();
		}
		return word2cats;
	}
}
