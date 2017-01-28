package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 *
 * @author Nikolin
 */


/* NOTE: BECAUSE OF THIS FUNCTION:
public void selectAndLoopOverResults_JSON_withDateTimes(LocalDateTime from, LocalDateTime to, int interval), 
on: JsonParserT.java, on line: 131,
all the words are unique in the lists. The filtering code is present
on: JsonParserT.java, on line: 157 - 159
*/

public class EntryElementsManager {
    
    public int interval;
    public Date from;
    public Date to;
    public int cicles = 0;
    
    public Date dtHelper_from;
    public Date dtHelper_to;
    
    public ArrayList<EntryElementsDate> wordsAndCreatedDates_ofManager;
    public ArrayList<EntryElementsHolder> lists;
    
    private LinkedList<Tweet> tweets;
    
    private static final int MILLISECONDS_IN_A_MINUTE = 60000;
    
    
    public EntryElementsManager(int interval, Date from, Date to, ArrayList<EntryElementsDate> wordsAndCreatedDates){
        this.interval = interval;
        this.from = from;
        this.to = to;
     
        this.lists = new ArrayList<>();
        
        this.wordsAndCreatedDates_ofManager = wordsAndCreatedDates;
        
        //JOptionPane.showMessageDialog(null, "FROM: " + from.toString() + "   TO: " + to.toString() + " Interval: " +  interval, "Data - EntryElementManager", JOptionPane.INFORMATION_MESSAGE);
    } 
    
    public EntryElementsManager(int interval, Date from, Date to, LinkedList<Tweet> tweets){
        this.interval = interval;
        this.from = from;
        this.to = to;
     
        this.lists = new ArrayList<>();
        this.tweets = tweets;
        
    }
    
    public int[][] getChartDataForWord(String word){
        
        double difference = (this.to.getTime() - this.from.getTime())/ MILLISECONDS_IN_A_MINUTE;
        
        // Testing the difference from the interval date choosen.
       // System.out.println(difference + "Difffffff");
        
        
        double differenceDivInterval = difference / (this.interval * 60);
        
        // Using the floor method 
        double numberOfSlots = Math.ceil(differenceDivInterval);
        
        int valueToInitialize = (int)numberOfSlots;
        
        int[] result_wordCounter = new int[valueToInitialize];
        int[] result_sentimentAvarage = new int[valueToInitialize];
        
        for (Tweet t : tweets){
            if (t.words.contains(word)){
                double div2=(t.createdAt.getTime() - this.from.getTime())/ MILLISECONDS_IN_A_MINUTE;
                
                // We can output the first TOP WORD created at TIME with no loosing time
                //System.out.println(div2 + "Difffffff22222" + "from: " + this.from.toString() + "created:" + t.createdAt.toString());
                
                double twwetdiff = div2 / (this.interval * 60);

                double diff3 = Math.ceil(twwetdiff);
        
                int slot = (int)diff3;
                
                //THIS STORES THE TOTAL NUMBER OF TIMES A WORD IS FOUND
                result_wordCounter[slot - 1] = result_wordCounter[slot - 1] + 1;
                
                //ADDS TWEETS SENTIMENT SCORE IN TOTAL one + one + etc.
                result_sentimentAvarage[slot - 1] = result_sentimentAvarage[slot - 1] + t.sentimetScore;
            }
        }
        
        int countCicle = 0;
        for(int total : result_sentimentAvarage){
            //System.out.println(total + "   is the value at index[" + countCicle + "]");
            if(total != 0){
                result_sentimentAvarage[countCicle] = total / result_wordCounter[countCicle];
                //System.out.println("      operation performed is: " + total + "   divided with: " + result_wordCounter[countCicle] + "    produced: " + result_sentimentAvarage[countCicle]);
            }
            countCicle++;
        }
        
        int[][] arraysToReturn = new int[][] {result_wordCounter, result_sentimentAvarage};
        
        return arraysToReturn;
    }
    
    
    
    public EntryElementsManager(){
        this.lists = new ArrayList<>();
        this.wordsAndCreatedDates_ofManager = new ArrayList<>();
    } 
    
    
    /*
    //FUNCTION IN ORDER TO CREATE SMALL LISTS WITH INTERVAL TIME FRAMES
    public void prepareLists(){
        this.cicles = 0;
        int i = this.interval;
        
        Date localFrom = this.from;
        Date localTo = this.from; //this.from.plusHours(i);
        //JOptionPane.showMessageDialog(null, "FROM: " + this.from, "The time you have selected...", JOptionPane.INFORMATION_MESSAGE);
        //JOptionPane.showMessageDialog(null, "FROM: " + this.to, "The time you have selected...", JOptionPane.INFORMATION_MESSAGE);
        
        while(localTo.compareTo(this.to) <= 0){
            localTo = localTo.plusHours(i);
            //System.out.println("    hours: " + localTo + " - " + this.to);
            //System.out.println("DIFF hours: " + ChronoUnit.HOURS.between(localTo, this.to));
            //TAKING STRONGLY INTO CONSIDERATION THAT INTERVAL IS ONLY IN HOURS**ONLY HOURS
            this.dtHelper_from = localFrom;
            this.dtHelper_to = localTo;
            
            EntryElementsHolder entry = new EntryElementsHolder(this.dtHelper_from, this.dtHelper_to);
            entry.cicleOfData = this.cicles;
            
            this.lists.add(entry);
            
            localFrom = localFrom.plusHours(i);
            
            this.cicles++;
        }
        
        //JOptionPane.showMessageDialog(null, "A total of " + this.cicles + " cicles generated.", "Intervals", JOptionPane.INFORMATION_MESSAGE);
    } 
    
    */
    
    
    
     /*
    //POPULATES ALL THE TIMEFRAMES ACCORDINGLY WITH DATA FROM THE WORDS, STILL NOT COUNTED BUT INSIDE THE RIGHT DIVISION
    public void populateMyData(){
        for(int i=0; i<this.wordsAndCreatedDates_ofManager.size(); i++){
            for(int j=0; j<this.lists.size(); j++){
                if((this.wordsAndCreatedDates_ofManager.get(i).date.after(lists.get(j).from)) && (this.wordsAndCreatedDates_ofManager.get(i).date.before(lists.get(j).to))){
                    this.lists.get(j).wordsAndCreatedDates_ofHolder.add(this.wordsAndCreatedDates_ofManager.get(i));
                }
            }
        }
    }
    
    // Used to caon the occurance for every hour and sort the word on each list
    //COUNT EACH WORD OCCURANCE FOR EACH INDIVIDUAL HOUR
    public void countAllWordsInTheLists(){
        for(int i=0; i<this.lists.size(); i++){
            if(this.lists.get(i).wordsAndCreatedDates_ofHolder.size() > 0){
                this.lists.get(i).counterForTheWords_individualObjectList();
            }
        }
    } 

    //SORT THE WORD ON EACH LIST FROM TOP TO BOTTOM
    public void sortAllWordsInTheLists_afterCounted(){
        for(int i=0; i<this.lists.size(); i++){
            if(this.lists.get(i).wordsAndCreatedDates_ofHolder.size() > 0){
                this.lists.get(i).sortForTheWords_individualObjectList();
            }
        }
    }  */
    
    
    
    //FUNCTION ONLY TO CHECK IF DATA ARE STORED CORRECTLY AND STATS
    public void checkIntegrityAndData(){
        System.out.println("**********LIST CHECKING ON EntryElementsManager***************");
        System.out.println("**************************************************************");
        System.out.println("INTERVAL VALUE: " + this.interval);
        System.out.println("FROM DATE VALUE: " + this.from.toString());
        System.out.println("TO DATE VALUE: " + this.to.toString());
        System.out.println("WORDS THAT CAME FROM JSON read OPERATION: " + this.wordsAndCreatedDates_ofManager.size());
        
        //for(int i=0; i<this.wordsAndCreatedDates_ofManager.size(); i++){
        //    System.out.println("WORD[" + i + "]: " + this.wordsAndCreatedDates_ofManager.get(i).word + "  --DATE: " + this.wordsAndCreatedDates_ofManager.get(i).date.toString() + "  --COUNT:" + this.wordsAndCreatedDates_ofManager.get(i).count);
        //}
        
        System.out.println("LISTS CREATED: " + this.lists.size());
        System.out.println("PRINTING EACH INDIVIDUAL LIST *********************");
        
        //for(int i=0; i<this.lists.size(); i++){
            //System.out.println("    ...printing UNcountedLIST[" + i + "] with total size: " + this.lists.get(i).wordsAndCreatedDates_ofHolder.size());
            
            //for(int j=0; j<this.lists.get(i).wordsAndCreatedDates_ofHolder.size(); j++){
            //    System.out.println("  -UNCountedrecord[" + j + "]: " + this.lists.get(i).wordsAndCreatedDates_ofHolder.get(j).word + "      --DATE: " + this.lists.get(i).wordsAndCreatedDates_ofHolder.get(j).date + "      --COUNT: " + this.lists.get(i).wordsAndCreatedDates_ofHolder.get(j).count);
            //}
        //}
        
        for(int i=0; i<this.lists.size(); i++){
            System.out.println("    ...printing CountedList[" + i + "] with total size: " + this.lists.get(i).storeCountedWords_ofHolder.size());
            
            //for(int j=0; j<this.lists.get(i).storeCountedWords_ofHolder.size(); j++){
            //    System.out.println("    -Countedrecord[" + j + "]: " + this.lists.get(i).storeCountedWords_ofHolder.get(j).word + "      --DATE: " + this.lists.get(i).storeCountedWords_ofHolder.get(j).date + "      --COUNT: " + this.lists.get(i).storeCountedWords_ofHolder.get(j).count);
            //}
        }
    }
}


