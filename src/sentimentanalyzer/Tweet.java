package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author Nikolin
 */
public class Tweet {
    
    public Date createdAt;
    public HashSet<String> words = new HashSet <String>();
    
    //SENTIMET SCORE BASED ON THE SENTENCE IT WAS BEFORE
    public int sentimetScore;
      
}
