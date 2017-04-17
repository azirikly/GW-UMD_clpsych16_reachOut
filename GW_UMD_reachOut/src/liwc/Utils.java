package liwc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public HashMap<String, HashMap<Integer, Integer>> word2cat2Freq(
			String text, 
			String catFile, 
			String wordFile){

		HashMap<String, HashMap<Integer, Integer>> word2cat2Freq = 
				new HashMap<String, HashMap<Integer, Integer>>();

		// Load word2cat
		HashMap<String, ArrayList<Integer>> word2cats = ReadDic.word2cats(wordFile);

		String[] words = text.split("\\s+");
		HashMap<Integer, Integer> cat2freq;
		ArrayList<Integer> cats;
		for (String word: words){
			/*
			 * get the category
			 * The search should be by regex
			 */
			cats = getRegex(word2cats, word);
			if (cats != null){
				cat2freq = word2cat2Freq.get(word);
				for (Integer catID: cats){
					if (cat2freq == null){
						cat2freq = new HashMap<Integer, Integer>();
						cat2freq.put(catID, 1);
					}
					else if (cat2freq.get(catID) == null)
						cat2freq.put(catID, 1);
					else {
						int freq = cat2freq.get(catID);
						cat2freq.put(catID, ++freq);
					}
				}
				word2cat2Freq.put(word, cat2freq);
			}
		}
		return word2cat2Freq;
	}

	public HashMap<Integer, Integer> getLIWCcatFreq(String text, 
			String catFile, 
			String wordFile){

		// Load the categories
		HashMap<Integer, String> id2catName =  ReadDic.id2catName(catFile);
		ArrayList<Integer> categories = new ArrayList<Integer>();
		categories.addAll(id2catName.keySet());

		// Load word2cat
		HashMap<String, ArrayList<Integer>> word2cats = ReadDic.word2cats(wordFile);

		HashMap<Integer, Integer> catFreq = initZeroFreq(categories);

		String[] words = text.split("\\s+");
		ArrayList<Integer> cats;
		int freq = 0;
		for (String word: words){
			/*
			 * get the category
			 * The search should be by regex
			 */
			cats = getRegex(word2cats, word);
			if (cats != null){
				for (Integer catID: cats){
					freq = catFreq.get(catID);
					catFreq.put(catID, new Integer(freq+1));
				}
			}
		}
		return catFreq;
	}

	private HashMap<Integer, Integer> initZeroFreq(ArrayList<Integer> categories) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int i = 0;
		int size = categories.size();
		while (i < size){
			map.put(categories.get(i), 0);
			++i;
		}
		return map;
	}

	private ArrayList<Integer> getRegex(HashMap<String, ArrayList<Integer>> map, String searchTerm){
		Set<String> keys = map.keySet();
		ArrayList<Integer> values = null;
		Pattern p;
		Matcher m;
		int maxMatchLength = 0;
		
		for (String key: keys){
			if (key.equals(searchTerm.toLowerCase()))
				return map.get(key);

			p = Pattern.compile(key);
			m = p.matcher(searchTerm.toLowerCase());
			if (m.matches() && key.length() > maxMatchLength){
				maxMatchLength = key.length();
				values = map.get(key);
			}
		}
		return values;
	}
}
