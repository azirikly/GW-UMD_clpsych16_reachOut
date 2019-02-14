GW/UMD CLPsych ReachOut shared-task source code
Authors: 
Ayah Zirikly	ayaz@gwu.edu
Varun Kumar	varunk@cs.umd.edu
Philip Resnik resnik@umd.edu

-Last update: April 2016

=================================
	DIRECTORY CONTENT
=================================
+ GW_UMD_reachOut: 
 ++ src: contains all the java source code
 ++ dic: contains the NRC and disease lexicons
 ++ lib: contains commons-csv-1.2 library, that this code uses

+ 2stage_clf_best.py: GW/UMD 2stage classifier that requires features generation as pre-step


=================================
	FEATURES GENERATION
=================================
In order to run the code there are multiple preprocessing steps:
1) Prepare Train/Test file in the form of CSV that contains all the data in the XML file provided by the task along with the label for train file
2) Run StanfordCoreNLP on the posts’ text and generate Lemmas, tokens, sentiments and concatenate them to the csv file generated in (1)
3) Features generation:
 3.1) Generate LIWC features for train and test by adding a main method in PostsCatProcessing.java in liwc package as follow:
   public static void main (String[] args){
	//generate train file
	new PostsCatProcessing().writeLIWCCategories(inputTrainCSV, outputLIWCfile, lemma, LIWC_catFile, LIWC_wordFile, true);
	// generate test file
	new PostsCatProcessing().writeLIWCCategories(inputTrainCSV, outputLIWCfile, lemma, LIWC_catFile, LIWC_wordFile, false);
   }
The above call assumes you have LIWC files in the form of:
+ LIWC_catFile: cat_id\tcat_name
+ LIWC_wordFile: tab separated file where first column is the word and the other columns are all the categories that apply to the word

 3.2) Emotion Features:
 	Similarly to generating LIWC features, call generateEmoVector method under emoLexicon package/Utils class in your main method. please consult the comments in the code for further info on the parameters. 
 3.3) Mental Disease Lexicon Features:
	Similarly to generating LIWC features, call generateTermsFreq in your main method. please consult the comments in the code for further info on the parameters. 

 3.4) Author Ranking Features:
  Please call writeAuthorFeatures method in your main method. please consult the comments in the code for further info on the parameters.

=================================
		MODEL
=================================
2stage_clf_best.py
To run the code please use:
python 2stage_clf_best.py 

The following need to be modified in the code:
a) Provide the input csv file (generated in 1) for train and test in line 72 and 132
df_corpus = pd.read_csv('')
b) Provide the LIWC features file generated in (3.1) for train and test files 
c) Provide the emotion features file generated in (3.2) for train and test files 
d) Provide the mental disease features file generated in (3.3) for train and test files 
e) Provide the author ranking features file generated in (3.4) for train and test files
f) An output file results.tsv will be written to the working directory that is in the form of postID\tlabel (the form requested by the shared task)
