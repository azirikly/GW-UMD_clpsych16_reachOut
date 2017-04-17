from __future__ import division
from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.svm import SVC
from sklearn.linear_model import LogisticRegression
from sklearn.cross_validation import StratifiedKFold
from nltk.stem.porter import PorterStemmer
from nltk import word_tokenize
import pandas as pd
import numpy as np


stemmer = PorterStemmer()


def tokenize(text):
    tokens = word_tokenize(text)
    stems = [stemmer.stem(item) for item in tokens]
    return stems


def fix_nan_and_nbsp(x):
    if pd.isnull(x):
        return ' '
    return x.replace('&nbsp;', ' ')


def remove_delimiter(x):
    if pd.isnull(x):
        return ' '
    return x.replace('#@#', ' ')


def get_pos_tag_count(tags, pos_list):
    cnt = 0.0
    for pos in pos_list:
        cnt+=tags.count(pos)
    return cnt


def get_pos_neg_count(x):
    x = x.lower()
    cnt = 0
    cnt+= x.count('neg:')
    cnt+= x.count('pos:')
    cnt+= x.count('positive:')
    cnt+= x.count('negative:')
    cnt+= x.count('neg :')
    cnt+= x.count('pos :')
    cnt+= x.count('positive :')
    cnt+= x.count('negative :')
    return cnt


def get_POS_tags(x):
    x = remove_delimiter(x)
    tags = x.split()
    ws = {}
    #ws['nn_count'] = get_pos_tag_count(tags, ['NN', 'NNS', 'NNP', 'NNPS'])
    #ws['ad_count'] = get_pos_tag_count(tags, ['JJ', 'JJR', 'JJS'])
    ws['av_count'] = get_pos_tag_count(tags, ['RB', 'RBR', 'RBS'])
    #ws['vb_count'] = get_pos_tag_count(tags, ['VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ'])
    ws['wd_count'] = get_pos_tag_count(tags, ['WDT', 'WP', 'WP$', 'WRB'])
    ws['po_count'] = get_pos_tag_count(tags, ['PRP', 'PRP$'])
    #ws['m1_count'] = get_pos_tag_count(tags, ['DT', 'IN', 'JJ'])
    ws['md_count'] = get_pos_tag_count(tags, ['MD'])
    return pd.Series(ws)


def generate_training_data():
    # Read train csv file
    df_corpus = pd.read_csv('')
    # Read LIWC feat file
    liwc_df = pd.read_csv('')
    emotion_df = pd.read_csv('')
    #emotion_df = emotion_df.drop(['docID', 'body', 'label'], inplace=True, axis=1)
    emotion_df.drop(['docID', 'body', 'label'], inplace=True, axis=1)
    liwc_df.drop(['docID', 'lemmas','kudos_count','views','label', 'id'],inplace=True,axis=1)

    # merge liwc and emotion dataframes
    liwc_df = liwc_df.rename(columns = {col:'liwc_'+ col for col in liwc_df.columns})
    liwc_df = pd.concat([liwc_df, emotion_df], axis =1)
    scaler = StandardScaler().fit(liwc_df)
    liwc_df = pd.DataFrame(scaler.transform(liwc_df), columns=liwc_df.columns, index=liwc_df.index)
    df = pd.concat([df_corpus, liwc_df], axis=1)

    #df['author'] = df.author.apply(lambda x:x.split('/')[3])
    df['text_features'] = df.body.apply(fix_nan_and_nbsp)
    df['hour'] = df.post_time.apply(lambda x: 'hour'+str(int(int(x.split('T')[1].split(':')[0])/3)))
    df['word_count'] = df.text_features.apply(lambda x: len(x.split(' ')))
    df['neg_pos'] = df.text_features.apply(get_pos_neg_count)
    df['@mention'] = df.text_features.apply(lambda x: x.count('@'))

    df = pd.concat([df, pd.get_dummies(df['hour'])], axis=1)
    df['sentiment'] = df.sentiment.apply(remove_delimiter)
    df['positive_word_count'] = df.sentiment.apply(lambda x: x.count('Positive')/len(x.split(' ')))
    df['negative_word_count'] = df.sentiment.apply(lambda x: x.count('Negative')/len(x.split(' ')))
    df['neutral_word_count'] = df.sentiment.apply(lambda x: x.count('Neutral')/len(x.split(' ')))
    pos_df = df.pos.apply(get_POS_tags)
    scaler = StandardScaler().fit(pos_df)
    pos_df = pd.DataFrame(scaler.transform(pos_df), columns=pos_df.columns, index=pos_df.index)
    #df = pd.concat([df, pos_df], axis=1)
    df = pd.concat([df, pd.get_dummies(df_corpus['board'])], axis=1)
    
    #add wordShape
    wordShape_df = pd.read_csv('')
    wordShape_df.drop(['docID', 'body'], inplace=True, axis=1)
    scaler = StandardScaler().fit(wordShape_df)
    #df = pd.concat([df, wordShape_df], axis=1)
    
    #add authorRanking
    authorRanking_df = pd.read_csv('')
    authorRanking_df.drop(['docID', 'body', 'authorRanking', 'label'], inplace=True, axis=1)
    scaler = StandardScaler().fit(authorRanking_df)
    df = pd.concat([df, authorRanking_df], axis=1)
    
    #add mental disease lexicon
    mentalDisLex_df = pd.read_csv('')
    mentalDisLex_df.drop(['label'], inplace=True, axis=1)
    scaler = StandardScaler().fit(mentalDisLex_df)
    df = pd.concat([df, mentalDisLex_df], axis=1)
    
    #add word2vec
    word2vec_df = pd.read_csv('')
    scaler = StandardScaler().fit(word2vec_df)
    #df = pd.concat([df, word2vec_df], axis=1)
    
    return df


def generate_test_data():
    df_corpus = pd.read_csv('')
    liwc_df = pd.read_csv('')
    emotion_df = pd.read_csv('')
    #emotion_df.drop(['docID', 'body'], inplace=True, axis=1)
    liwc_df.drop(['docID', 'id', 'lemmas','kudos_count','views'],inplace=True,axis=1)

    # merge liwc and emotion dataframes
    liwc_df = liwc_df.rename(columns = {col:'liwc_'+ col for col in liwc_df.columns if col != 'id'})
    liwc_df = pd.concat([liwc_df, emotion_df], axis =1)
    scaler = StandardScaler().fit(liwc_df)
    liwc_df = pd.DataFrame(scaler.transform(liwc_df), columns=liwc_df.columns, index=liwc_df.index)
    df = pd.concat([df_corpus, liwc_df], axis=1)

    df['text_features'] = df.body.apply(fix_nan_and_nbsp)
    df['hour'] = df.post_time.apply(lambda x: int(int(x.split('T')[1].split(':')[0])/3))
    df['word_count'] = df.text_features.apply(lambda x: len(x.split(' ')))
    df['neg_pos'] = df.text_features.apply(get_pos_neg_count)
    df['@mention'] = df.text_features.apply(lambda x: x.count('@'))

    df = pd.concat([df, df.hour.apply(lambda s: pd.Series({'hour'+str(k):1 if s==k else 0 for k in range(8)}))], axis=1)
    df['sentiment'] = df.sentiment.apply(remove_delimiter)
    df['positive_word_count'] = df.sentiment.apply(lambda x: x.count('Positive')/len(x.split(' ')))
    df['negative_word_count'] = df.sentiment.apply(lambda x: x.count('Negative')/len(x.split(' ')))
    df['neutral_word_count'] = df.sentiment.apply(lambda x: x.count('Neutral')/len(x.split(' ')))
    pos_df = df.pos.apply(get_POS_tags)
    scaler = StandardScaler().fit(pos_df)
    pos_df = pd.DataFrame(scaler.transform(pos_df), columns=pos_df.columns, index=pos_df.index)
    #df = pd.concat([df, pos_df], axis=1)
    board_names = ['/boards/id/Everyday_life_stuff',
                '/boards/id/Feedback_Suggestion',
                '/boards/id/Getting_Help',
                '/boards/id/Intros',
                '/boards/id/Something_Not_Right',
                '/boards/id/mancave']
    df = pd.concat([df, df.board.apply(lambda s: pd.Series({k:1 if s==k else 0 for k in board_names}))], axis=1)

    #add wordShape
    wordShape_df = pd.read_csv('')
    wordShape_df.drop(['docID', 'body'], inplace=True, axis=1)
    scaler = StandardScaler().fit(wordShape_df)
    #df = pd.concat([df, wordShape_df], axis=1)
    
    #add authorRanking
    authorRanking_df = pd.read_csv('')
    authorRanking_df.drop(['docID', 'body', 'authorRanking'], inplace=True, axis=1)
    scaler = StandardScaler().fit(authorRanking_df)
    df = pd.concat([df, authorRanking_df], axis=1)
    
    #add mental disease lexicon
    mentalDisLex_df = pd.read_csv('')
    scaler = StandardScaler().fit(mentalDisLex_df)
    df = pd.concat([df, mentalDisLex_df], axis=1)

    #add word2vec
    word2vec_df = pd.read_csv('')
    scaler = StandardScaler().fit(word2vec_df)
    #df = pd.concat([df, word2vec_df], axis=1)
    
    return df



if __name__ == '__main__':

    neglected_cols = ['docID','author','board','board_id','body','id','last_edit_author','last_edit_time',
                      'subject','post_time','parent','thread','tokens','lemmas','pos','ne','parseTree','sentiment',
                      'label',
                      'fineGrainedLabel','hour','text_features','avgSentiment', 'deleted', 'read_only',
                      'message_rating',
                      'kudos_count',
                      #'liwc_filler',
                      #'views',
                      #'hour',
                      #'neg_pos', '@mention',
                      'word_count'
                      ]
    train_df = generate_training_data()
    test_df = generate_test_data()
    selected_features = [col for col in train_df.columns if col not in neglected_cols]
    vectorize = TfidfVectorizer(tokenizer=tokenize, ngram_range=(1, 1), binary= True, max_features=300,
                                 min_df=2, max_df= 0.60)

    train_data_features = vectorize.fit_transform(train_df['text_features'].replace(np.nan,' ', regex=True))
    test_data_features = vectorize.transform(test_df['text_features'].replace(np.nan,' ', regex=True))
    clf1 = SVC(decision_function_shape='ovo', class_weight='balanced', kernel='linear', probability=True)
    #clf1 = LogisticRegression()
    clf1.fit(train_data_features, train_df['label'])
    predicted_values = clf1.predict(test_data_features)
    cols = ['ngram_amber_prob', 'ngram_crisis_prob', 'ngram_green_prob', 'ngram_red_prob']
    test_predicted_probs_df = pd.DataFrame(clf1.predict_proba(test_data_features), index=test_df.index, columns=cols)
    test_df = pd.concat([test_df, test_predicted_probs_df], axis=1, join='inner')

    train_predicted_probs_df = pd.DataFrame(clf1.predict_proba(train_data_features), index=train_df.index, columns=cols)
    train_df = pd.concat([train_df, train_predicted_probs_df], axis=1, join='inner')


    features = [col for col in test_df.columns if col not in neglected_cols]

    selected_features = features

    clf2 = LogisticRegression(class_weight='balanced')
    clf2.fit(train_df[selected_features].fillna(0), train_df['label'])
    predicted_y = clf2.predict(test_df[selected_features].fillna(0))

    output_file = open('result.tsv','w')
    for pid, py in zip(test_df['id'].tolist(), predicted_y):
        output_file.write(str(pid) +'\t' + py + '\n')
    output_file.close()


