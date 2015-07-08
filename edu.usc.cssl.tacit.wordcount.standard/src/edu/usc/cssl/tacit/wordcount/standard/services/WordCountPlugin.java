package edu.usc.cssl.tacit.wordcount.standard.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.wordcount.standard.Activator;

public class WordCountPlugin {

	private SentenceModel sentenceModel;
	private TokenizerModel tokenizerModel;
	private POSModel posModel;
	private SentenceDetectorME sentDetector;
	private TokenizerME tokenize;
	private POSTaggerME posTagger;
	
	private boolean weighted;
	private Date dateObj;

	//wordDictionary<word,<category,weight>>
	private HashMap<String, HashMap<Integer,Double>> wordDictionary = new HashMap<String, HashMap<Integer,Double>>();
	//counts<words,<category,current weight>>
	//File count to maintain word counts for an individual file
	//Overall count to maintain word counts for all the files
	//User maps have integer keys because we read them from dictionary
	//Penn maps use the Penn treebank POS tags as keys
	private HashMap<String, HashMap<Integer,Double>> userFileCount = new HashMap<String, HashMap<Integer,Double>>();
	private HashMap<String, HashMap<Integer,Double>> userOverallCount = new HashMap<String, HashMap<Integer,Double>>();
	private HashMap<String, HashMap<String,Double>> pennFileCount = new HashMap<String, HashMap<String,Double>>();
	private HashMap<String, HashMap<String,Double>> pennOverallCount = new HashMap<String, HashMap<String,Double>>();
	private HashMap<Integer,String> categoryID = new HashMap<Integer, String>();
	
	public WordCountPlugin(boolean weighted, Date dateObj){
		this.weighted = weighted;
		this.dateObj = dateObj;
	}
	
	public void countWords(List<String> inputFiles, List<String> dictionaryFiles, String outputPath){
		if(!setModels()) return;
		
		try {
			buildMaps(dictionaryFiles);
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error parsing dictionary.");
			e.printStackTrace();
			return;
		}
		
		System.out.println(wordDictionary.toString());
		System.out.println(userFileCount.toString());
		
		if(weighted) TacitUtility.createRunReport(outputPath, "Weighted Word Count",dateObj);
		else TacitUtility.createRunReport(outputPath, "Standard Word Count",dateObj);
		return;
	}

	/**
	 * Function to initialize all the HashMap's used to maintain
	 * word counts.
	 * @param dictionaryFiles
	 * List of dictionary Files
	 * @throws IOException
	 * Catch error in caller. The errors might be due to 
	 * bad dictionary format.
	 */
	private void buildMaps(List<String> dictionaryFiles) throws IOException {
		
		for (String dFile : dictionaryFiles) {
			BufferedReader br = new BufferedReader(new FileReader(new File(dFile)));
			
			String currentLine = br.readLine().trim();
			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file "+ dFile + " is empty.");
			}

			if (currentLine.equals("%"))
				while ((currentLine = br.readLine().trim().toLowerCase()) != null
						&& !currentLine.equals("%"))
					categoryID.put(
							Integer.parseInt(currentLine.split("\\s+")[0]
									.trim()), currentLine.split("\\s+")[1]
									.trim());
			
			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file "+ dFile + " does not have any categorized words.");
			} else {
				while ((currentLine = br.readLine()) != null) {
					String[] words = currentLine.split("\\s+");
					
					//If word not in the maps, add it
					if (!wordDictionary.containsKey(words[0])) {
						wordDictionary.put(words[0], new HashMap<Integer, Double>());
						userFileCount.put(words[0], new HashMap<Integer, Double>());
						userOverallCount.put(words[0], new HashMap<Integer, Double>());
						pennFileCount.put(words[0], new HashMap<String, Double>());
						pennOverallCount.put(words[0], new HashMap<String, Double>());
					}
					
					for (int i=1;i<words.length;i=increment(i)){
						//Add a category to the maps if it was not added earlier
						if (!wordDictionary.get(words[0]).containsKey(Integer.parseInt(words[i]))) {
							if(weighted) {
								wordDictionary.get(words[0]).put(Integer.parseInt(words[i]), Double.parseDouble(words[i+1]));
							} else {
								wordDictionary.get(words[0]).put(Integer.parseInt(words[i]), 1.0);
							}
							userFileCount.get(words[0]).put(Integer.parseInt(words[i]), 0.0);
							userOverallCount.get(words[0]).put(Integer.parseInt(words[i]), 0.0);
						}
					}
				}
			}
			
			br.close();
		}
	}
	
	/**
	 * Utility function to increment loops while handling
	 * weighted and non weighted dictionaries.
	 * @param val
	 * @return
	 */
	private int increment(int val) {
		if (weighted) {
			return val + 2;
		} else
			return val + 1;
	}
	
	/**
	 * Sets all the models for OpenNLP
	 * @return
	 * Returns true if no errors while loading models
	 */
	private boolean setModels() {
		InputStream sentenceIs = null,tokenIs = null,posIs = null;
		try {
			File setupFile = new File(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("en-sent.bin")).getPath());
			sentenceIs = new FileInputStream(setupFile.toString());
			setupFile = new File(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("en-token.bin")).getPath());
			tokenIs = new FileInputStream(setupFile.toString());
			setupFile = new File(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("en-pos-maxent.bin")).getPath());
			posIs = new FileInputStream(setupFile.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Model file not found");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			sentenceModel = new SentenceModel(sentenceIs);
			tokenizerModel = new TokenizerModel(tokenIs);
			posModel = new POSModel(posIs);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		sentDetector = new SentenceDetectorME(sentenceModel);
		tokenize = new TokenizerME(tokenizerModel);
		posTagger = new POSTaggerME(posModel);
		
		return true;
	}
	
	private void UtilityFunc(){
		String testString = "Hi! My name is Anurag (Singh). This is a test string to check sentence breaking. Will it work? Let's see!";
		
		String[] results = sentDetector.sentDetect(testString);
		
		for (int i=0;i<results.length;i++){
			String[] tokens = tokenize.tokenize(results[i]);
			String[]  posTags = posTagger.tag(tokens);
			System.out.println("Tokens - POSTags: ");
			
			for (int j=0;j<tokens.length;j++){
				System.out.println(tokens[j]+" - "+posTags[j]);
			}
		}
	}
}