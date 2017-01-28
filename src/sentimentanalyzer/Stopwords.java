package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Nikolin
 */
public class Stopwords {
    
    public String fileWithStopwords;
    public String token;
    public ArrayList<String> words;
    public String[] wordsArray;
    
    
    
    public Stopwords(){
        this.fileWithStopwords = "stopwords_ita.txt";
        this.token = "";
        this.words = new ArrayList();
        
        try{
            //This is to read .txt file from the local hard disc, from the execution file.
            Scanner localTextFileReader = new Scanner(new File(this.fileWithStopwords)).useDelimiter("\\n");
        
            //LOOP to the rows of file .txt one by one and read the word, and add the word to our list words.
            while(localTextFileReader.hasNext()){
                this.token = localTextFileReader.next();
                this.words.add(token);
            }
            localTextFileReader.close();

            //Convert the list with the Array list for any case...
            this.wordsArray = this.words.toArray(new String[0]);
        }catch(Exception ex){
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }
        
        
        this.words.add("asdf");
        
        System.out.println("WORDS FOUND INSIDE stopwords_ita.txt: " + this.words.size());
        //System.out.println("ELEMENTS IN THE STRING ARRAY WITH WORDS: " + this.wordsArray.length);
    }
    
    public void printWords(){
        for(String s : this.wordsArray){
            System.out.println(s);
        }
    }
    
    public ArrayList<String> eraseStopwordsFromList(ArrayList<String> titleWords){      
        //Java function
        titleWords.removeAll(this.words);
        
        return titleWords;
    }
    
    public String cleanSpecialCharacters_onFullStringBeforeSplitting(String input){
        input = input.replaceAll("  ", "");
        input = input.replaceAll("\t", "");
        input = input.replaceAll("\n", "");
        input = input.replaceAll("-", " ");
        input = input.replaceAll(",", " ");
        input = input.replaceAll("@", " ");
        input = input.replaceAll("#", "");
        
        
        return input;
    }
    
    //Clean the string from characters like ? / . , etc. 
    public String cleanSpecialCharacters(String input){
        //CONVERT IT TO LOW CHARACTERS IMMEDIATELY
        input = input.toLowerCase();
        
        //0th PHASE - remove double spaces that produce the single '' words in the results
        input = input.replace("  ", " ");
        input = input.replace("   ", " ");
        //1st PHASE - clean combinations and locals
        input = input.replace("all’", "");
        input = input.replace("l’", "");
        input = input.replace("l'", "");
        input = input.replace("d’", "");
        input = input.replace("d'", "");
        input = input.replace("s’", "");
        input = input.replace("'s", "");
        input = input.replace("’s", "");
        input = input.replace("...", "");
        
        
        //2nd PHASE - clean special characters, singles
        input = input.replace(":", "");
        input = input.replace(";", "");
        input = input.replace(".", "");
        input = input.replace(",", "");
        input = input.replace("?", "");
        input = input.replace("$", "");
        input = input.replace("–", "");
        input = input.replace("!", "");
        input = input.replace("\\", "");
        input = input.replace("/", "");
        input = input.replace("_", "");
        input = input.replace("+", "");
        input = input.replace("(", "");
        input = input.replace(")", "");
        input = input.replace("&", "");
        input = input.replace("*", "");
        input = input.replace("«", "");
        input = input.replace("»", "");
        input = input.replace("%", "");
        input = input.replace("“", "");
        input = input.replace("”", "");
        input = input.replace("-", "");
        input = input.replace("\"", "");
        input = input.replace("\t", "");
        input = input.replace("\n", "");
        input = input.replace("0", "");
        input = input.replace("1", "");
        input = input.replace("2", "");
        input = input.replace("3", "");
        input = input.replace("4", "");
        input = input.replace("5", "");
        input = input.replace("6", "");
        input = input.replace("7", "");
        input = input.replace("8", "");
        input = input.replace("9", "");
        input = input.replace("|", "");
 
        //we can add other elements
        
        //2nd PHASE - part 2 - individual emaining quotations "'"
        input = input.replace("'", " ");
        
        //3rd PHASE - cleane very special characters :) remainings of the two first
        input = input.replace("\u00A0"," ");

        return input;
    }
}
