package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author Nikolin
 */

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sentimentanalyzer.BatchTest_Parallel;
import static sentimentanalyzer.SentimetAnalyzer.tz;

public class JsonParserT {
    private static final String filePath = "twitter.json";
    
    public ArrayList<String> llTitlesWords;
    public ArrayList<String> listToHoldCounted;
    
    public ArrayList<EntryElements> storeCountedWords;
    
    public ArrayList<EntryElementsDate> wordsAndCreatedDates;
    public EntryElementsManager storeCountedWords_forPloting;
    
    public Stopwords stopwords;
    
    public LinkedList<Tweet> tweets;
    
    private DefaultTableModel tableForPrint_model;
    private BatchTest_Parallel batchScoreCalc;
    
    public double occurrences1;
    public double occurrences0;
    public double occurrences_1;
    
    
    public JsonParserT(DefaultTableModel tableForPrint_model, BatchTest_Parallel bt){
        this.llTitlesWords = new ArrayList<>();
        this.listToHoldCounted = new ArrayList<>();
        
        this.storeCountedWords = new ArrayList<>();
        this.wordsAndCreatedDates = new ArrayList<>();
        this.storeCountedWords_forPloting = new EntryElementsManager();
        
        this.stopwords = new Stopwords();
        
        tweets = new LinkedList<Tweet>();
        
        this.tableForPrint_model = tableForPrint_model;
        
        batchScoreCalc = bt;
        
    }

    //Clean all list before using
    public void prepareLinkedListsForOperation_cleanRestore(){
        this.llTitlesWords.clear();
        this.listToHoldCounted.clear();
    }   
    
    //Function to loop over the results;
    public void selectAndLoopResults(){
        this.prepareLinkedListsForOperation_cleanRestore();
    }
        
    //JSON WITH LOCAL FILE
    public void selectAndLoopOverResults_JSON(String jsonRequest){
        try{
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonRequest);

            JSONArray text = (JSONArray) jsonObject.get("tweetsFeedback");

            Iterator i = text.iterator();
            int count = 0;
            
            this.tweets.clear();
            
            
             List<String> l = new ArrayList();
            
 
        
             while (i.hasNext()) {
                
                Tweet tweet = new Tweet();
                
                count++;
                JSONObject innerObj = (JSONObject) i.next();
                JSONObject user_id = (JSONObject) innerObj.get("user");
                
                // ***IMPORTANT NOTE HERE***   -- HIGH MEMORY USE HERE!!!
                // IF WE UNCOMMENT THE BELOW LINE THE TWEETS WE ARE PARSING WILL PRINT ON OUR CONSOLT
                // BUT IT WILL TAKE DOUBLE TIME TO CALCULATE THE RESULTS
                
                //System.out.println( count + " Content: " +  innerObj.get("text"));
                
                //System.out.println("Time of retrieval " + innerObj.get("created_at"));
                String createdAt = innerObj.get("created_at").toString();

                //SPECIAL CASES WERE DATE IS NOT PARSED OK FROM JSON -- TEAM IN ITALY SHOULD CHECK THE JSON, IS NOT CORRECT SOME TIMES
                createdAt = createdAt.replace("+0000 ", "");

                
                
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d H:m:s yyyy");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                //formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                try {
                    tweet.createdAt = formatter.parse(createdAt);
                } catch (java.text.ParseException ex) {
                    Logger.getLogger(JsonParserT.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //System.out.println("The id of the user: " + user_id.get("id"));
                
                String holder =( count + ") " + " " + this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("text").toString()));
                //String holder =( count + ") " + " " + innerObj.get("text").toString());
                String holderTime = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("created_at").toString());
                
                //System.out.println(holder);
                //System.out.println("    -" + holderTime);
                
                String[] individualWordsInsideTitle = holder.split(" ");
                
                
                for(int j=0; j<individualWordsInsideTitle.length; j++){
                    String output = this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]);
                    
                    //POSSIBLE MEMORY LEAK, DUPLICATED LIST 
                    this.llTitlesWords.add(output);
                    tweet.words.add(output);
                }
                
                
                //COUNT SENTIMENT SCORE ONLY FOR ONE WORD, each being part of the sentence that is being analysed
                //************* ADDED LINES TO ATTACH SENTIMENT SCORE IN TWEETS
                List<String> lToHoldSentiment_forScoreOfEachWord = new ArrayList();
                lToHoldSentiment_forScoreOfEachWord.add(innerObj.get("text").toString());
                List<Integer> word_sentimentScore = batchScoreCalc.classify_sentiment(lToHoldSentiment_forScoreOfEachWord);
                tweet.sentimetScore = word_sentimentScore.get(0);
                //*********************************************************************************************************
                
                this.tweets.add(tweet);
                
                // * Add the text that represent the tweets to the list l...
                l.add(innerObj.get("text").toString());
                
                
                // Adding the tweets on the "Individual Tweets" table of the GUI 
                this.tableForPrint_model.addRow(new Object[] { holderTime, holder, 0  });
            }
             
             
            // Here we are computing the Sentiment Score to the table of tweets
            List<Integer> sentimentResult = batchScoreCalc.classify_sentiment(l);
            
            
            int count2= 0;
            for (Integer ll : sentimentResult){
                this.tableForPrint_model.setValueAt(ll, count2, 2);
                count2++;
            }  
            
            
            this.occurrences1 = Collections.frequency(sentimentResult, 1);
            this.occurrences0 = Collections.frequency(sentimentResult, 0);
            this.occurrences_1 = Collections.frequency(sentimentResult, -1);
            
            
            // Here we can output the compute of the AVERAGE Sentiment Score from the entire tweets 
            /*
            if (sentimentResult == null || sentimentResult.isEmpty())
                System.out.println("0");
                // Calculate the summation of the elements in the list
                long sum = 0;
                int n = sentimentResult.size();
                // Iterating manually is faster than using an enhanced for loop.
                for (int m = 0; m < n; m++)
                    sum += sentimentResult.get(m);
                // We don't want to perform an integer division, so the cast is mandatory.
            
            System.out.println("The average score is: " + ((double) sum)/n);
            */
            
        }catch (ParseException ex){
            System.out.println(ex.getMessage());        
        }
    }
     
       
    public EntryElementsManager selectAndLoopOverResults_JSON_withDateTimes(Date from, Date to, int interval, String jsonRequest){
  
        return new EntryElementsManager(interval, from, to, this.tweets);
        
    }
    
    /*    
    public void selectEvery6HoursPrintAndStoreResults(String jsonRequest){
        this.selectAndLoopOverResults_JSON(jsonRequest);

        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.customFrequency(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);
        this.printTop_fromInbuildMergeSort(10);
    } */
    
    public ArrayList<EntryElements> selectListForMainWindow(String jsonRequest){
        this.storeCountedWords.clear();

        this.selectAndLoopOverResults_JSON(jsonRequest);

        
        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.customFrequency(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);
        

        return this.storeCountedWords;
    }
    
 
    
    //PREPARES ALL THE LISTS BASED ON THE INTERVAL ARGUMENT
    //MIGHT TAKE A WHILE TO RUN (on excecution)
    public EntryElementsManager selectListForMainWindow_forPloting_linkedListOfLinkedLists(Date from, Date to, int interval, String jsonRequest){

        this.storeCountedWords_forPloting = selectAndLoopOverResults_JSON_withDateTimes(from, to, interval, jsonRequest);
        return this.storeCountedWords_forPloting;
        
    } 
    
    /*  
    // Same function as the following one but it is use the ***Collection.frequency*** instead of the HashMap  
    public void countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(ArrayList<String> words){
        for(int i=0; i<words.size(); i++){
            if(!this.listToHoldCounted.contains(words.get(i))){
                int count = Collections.frequency(words, words.get(i));
                this.listToHoldCounted.add(words.get(i));
                
                EntryElements entry = new EntryElements(words.get(i), count);
                this.storeCountedWords.add(entry);
            }
        }
    }   */
    
     public void customFrequency(ArrayList<String> words){		    
        HashMap<String, Integer > hm = new HashMap<>();		
        		
        for(int i=0; i<words.size(); i++){		
            String key = words.get(i);		
            			
            if(hm.containsKey(key)){		
                hm.put(key, hm.get(key) + 1);		
            }else{		
                hm.put(key, 1);		
            }		
        }		
        		
        for (String key : hm.keySet()){		
            EntryElements entry = new EntryElements(key, hm.get(key));		
            this.storeCountedWords.add(entry);	         
        }
    }
    
    
    
    public void printLLTitlesWords(){
        for(int i=0; i<this.llTitlesWords.size(); i++){
            System.out.println(this.llTitlesWords.get(i));
        }
    } 
    
    public void printTop_fromInbuildMergeSort(int elementsToPrint){
        System.out.println("-============================");
        System.out.println("-TOP " + elementsToPrint + " =====================");
        
        System.out.println("-============================");
        
        if(elementsToPrint > this.storeCountedWords.size()){
            elementsToPrint = this.storeCountedWords.size();
        }
        
        for(int i=0; i<elementsToPrint; i++){
            String lineToShow = "\"" + this.storeCountedWords.get(i).getKey() + "\"" + "\t" + this.storeCountedWords.get(i).getValue();
            System.out.println(lineToShow);
        }
    }
    
}
    
    
    
            
            
        

