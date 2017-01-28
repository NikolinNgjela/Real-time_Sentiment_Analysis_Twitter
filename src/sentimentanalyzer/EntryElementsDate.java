package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.time.LocalDateTime;

/**
 *
 * @author Nikolin
 */


//THIS CLASS IS NOT BEING USED

public class EntryElementsDate implements Comparable<EntryElementsDate> {
    
    public String word;
    public LocalDateTime date;
    public int count = 0;
    
    //SENTIMET SCORE BASED ON THE SENTENCE IT WAS BEFORE
    //NOT USED
    public int sentimetScore;
    
    public EntryElementsDate(String word, LocalDateTime date, int sentimentScore){
        this.word = word;
        this.date = date;
        this.sentimetScore = sentimentScore;
    }
    
    public EntryElementsDate(String word, LocalDateTime date, int counter, int sentimentScore){
        this.word = word;
        this.date = date;
        this.count = counter;
        this.sentimetScore = sentimentScore;
    }
    
    @Override
    public int compareTo(EntryElementsDate o) {
        int comparedSize = o.count;
        if (this.count > comparedSize) {
                return -1;
        } else if (this.count == comparedSize) {
                return 0;
        } else {
                return 1;
        }
    }
    
    public String getKey() {
        return this.word;
    }
    
    public void setKey(Object key) {
        this.word = (String)key;
    }
    
    public LocalDateTime getValue() {
        return this.date;
    }

    public void setValue(Object date) {
        this.date = (LocalDateTime)date;
    }
    
}
