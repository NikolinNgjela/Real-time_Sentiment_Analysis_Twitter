package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author Nikolin
 */
public class HTTPRequests {
    
    public String url;
    public String keyword;
    public String decode;
    public String fromStringFormat;
    public String toSringFormat;
    public String urlLastTweets;
    
    /*
    public HTTPRequests(LocalDateTime from, LocalDateTime to, String keyword, String decode, String url){
        this.fromStringFormat = from.toString().replace("T", "%20") + ":00";
        this.toSringFormat = to.toString().replace("T", "%20") + ":00";
        this.keyword = keyword;
        this.decode = decode;
        this.url = url.replace("[[from]]", this.fromStringFormat).replace("[[to]]", this.toSringFormat).replace("[[key]]", this.keyword).replace("[[decode]]", this.decode); 
        // Test custom Url **** Here ***
        //this.url = "http://localhost:8080/TestTweets/src=t&fromdate=2015-09-20$2001_00_00&todate=2015-09-23$2001_00_00&keyword=formula&decode=1.json";
        //this.url = "http://localhost:8080/TestTweets/src=t&fromdate=2015-09-10$2001_00_00&todate=2015-09-15$2001_00_00&keyword=peace&decode=1.json";        
              //this.url = "http://localhost:8080/phpTweets/testApp.php";
        System.out.println(this.url);
    } 
     */
    
    public HTTPRequests(LocalDateTime from, LocalDateTime to, String keyword, String decode, String url){
        this.fromStringFormat = from.toString().replace(":", "_") + "_00";
        this.toSringFormat = to.toString().replace(":", "_") + "_00";
        this.keyword = keyword;
        this.decode = decode;
        this.url = url.replace("[[from]]", this.fromStringFormat).replace("[[to]]", this.toSringFormat).replace("[[key]]", this.keyword).replace("[[decode]]", this.decode); 
        
        System.out.println(this.url);
    } 
    
    public HTTPRequests(String urlLastTweets){
        this.urlLastTweets = "http://localhost:8080/phpTweets/testApp.php";
    }
    
    public String getHTTPResponce_readURL(){
        try{
            URL jsonData = new URL(this.url);
            URLConnection connection = jsonData.openConnection();  
            connection.setDoOutput(true);  

            String response;
            try (Scanner scanner = new Scanner(jsonData.openStream())) {
                response = scanner.useDelimiter("\\Z").next();
                response = "{\"tweetsFeedback\":[" + response + "]}";
                               // response = "{\"tweetsFeedback\":" + response;
                response = response.replace("}{", "},{");
                               // response = response.replace("\"en\"}]", "\"en\"}]}");               
                // *** Here we can print the Json Object that we are get from ULR ***
                System.out.println(response);
            }
            
            //JOptionPane.showMessageDialog(null, "HTTP Request Successful", "HTTP Request", JOptionPane.INFORMATION_MESSAGE);
            
            return response;
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    public String getHTTPResponce_readULR_ForTheLastTweets(){
        try{
            URL jsonData = new URL(this.urlLastTweets);
            URLConnection connection = jsonData.openConnection();  
            connection.setDoOutput(true);  

            String response;
            try (Scanner scanner = new Scanner(jsonData.openStream())) {
                response = scanner.useDelimiter("\\Z").next();
                response = "{\"tweetsFeedback\":" + response;
                
                response = response.replace("\"en\"}]", "\"en\"}]}");               
                // *** Here we can print the Json Object that we are get from ULR ***
                System.out.println(response);
            }           
            //JOptionPane.showMessageDialog(null, "HTTP Request Successful", "HTTP Request", JOptionPane.INFORMATION_MESSAGE);           
            return response;
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    
    public static final String getJsonWithHTTPRequest(String spnDayChoser, String spnToTime, String keyword_local, String decode_local, String url_local){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d H:m:s z yyyy", Locale.ENGLISH);
        LocalDateTime fromDate_parsed = LocalDateTime.parse(spnDayChoser, formatter);
        LocalDateTime toDate_parsed = LocalDateTime.parse(/*this.spnToTime.getValue().toString()*/spnToTime, formatter);
        
        HTTPRequests getHttp = new HTTPRequests(fromDate_parsed, toDate_parsed, keyword_local, decode_local, url_local/*this.txtKeyword.getText(), this.txtDecode.getText(), this.txtURL.getText()*/);
        String response = getHttp.getHTTPResponce_readURL();
        
        //System.out.println(response);
        
        return response;
    }
    
        public static final String getJsonWithHTTPRequestLastTweets(String url_local){
    
        HTTPRequests getHttp = new HTTPRequests( url_local );
        String response = getHttp.getHTTPResponce_readULR_ForTheLastTweets();
        
        //System.out.println(response);
        
        return response;
    }
       
}
