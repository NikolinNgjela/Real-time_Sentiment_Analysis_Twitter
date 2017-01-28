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
public class EntryElements implements Comparable<EntryElements> {
    
    public String word;
    public int count;
    
    
    public EntryElements(String word, int count){
        this.word = word;
        this.count = count;
    }
    
    @Override
    public int compareTo(EntryElements o) {
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

    public int getValue() {
        return this.count;
    }

    public void setValue(Object value) {
        this.count = (Integer)value;
    }
    
}
