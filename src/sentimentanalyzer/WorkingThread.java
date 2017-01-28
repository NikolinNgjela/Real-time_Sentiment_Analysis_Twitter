package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author Nikolin
 */
public class WorkingThread extends SwingWorker<Integer, String> {
    
    public String url;
    public String keyword;
    public String decode;
    public Date fromTime;
    public Date toTime;
    public int interval;
    
    public String responce;
    
    public JPanel pnlWordsCount;
    
    private SentimetAnalyzer mainWindow;
    private BatchTest_Parallel batchTest;
    
    public WorkingThread(Date fromTime, Date toTime, String url, String keyword, String decode, int interval, SentimetAnalyzer mainWindow, JPanel pnlWordsCount){
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.url = url;
        this.keyword = keyword;
        this.decode = decode;
        this.interval = interval;
        
        this.pnlWordsCount = pnlWordsCount;
        
        this.mainWindow = mainWindow;
    }

    @Override
    protected Integer doInBackground() throws Exception{
        int progress = 10;
        
        publish("Cleaning the tables and initializing the words graphs...");
        this.mainWindow.cleanTheTableModels();
        // This is used to return the original condition of the "Words Table" // **** disappeared it @ mouse click *****
        //this.mainWindow.charts.initializeCountWordsXYSeriesChart(this.pnlWordsCount, "Word Count", "count", "word", 0, "words");
        
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Init Score Calculation Engine");
        this.batchTest = new BatchTest_Parallel();
        
        progress += 10;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Requesting Json from URL...");
        
        if(this.keyword.isEmpty()){
                   //**********########### This is the URL CALL *************####################
        this.responce = HTTPRequests.getJsonWithHTTPRequestLastTweets(url);
        System.out.println(this.fromTime);
        System.out.println("### Last tweets from last news...");
        
        } else {
        //WE CAN USE THIS DIRECT CALL WITHOUT REFERENCE BECAUSE THE FUNCTION getJsonWithHTTPRequest IS STATIC
        this.responce = HTTPRequests.getJsonWithHTTPRequest(this.fromTime.toString(), this.toTime.toString(), this.keyword, this.decode, this.url);
 
        }

        progress += 10;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Parsing Json, populating the tables building initial ArrayList with words...");
        JsonParserT jsonparsert = new JsonParserT(this.mainWindow.tableModelTitles, batchTest);
        
        
        // ==>
        //AFTER THIS LINE IS EXECUTE THE 3 values positive, negative and neutral are INITIALISED
        ArrayList<EntryElements> list = jsonparsert.selectListForMainWindow(this.responce);
        
        this.mainWindow.plotPieChartGraphToMainWindow(jsonparsert.occurrences1, jsonparsert.occurrences_1, jsonparsert.occurrences0);
        
        
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Populating TOP TEN words...");
        //POPULATE CONTROLLER ON GUI
        for(int i=0; i<10; i++){
           this.mainWindow.tableModelTopTen.addRow(new Object[] { list.get(i).getKey(), list.get(i).getValue(), Boolean.FALSE });
        }
        
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Build small lists according to the intervals and counts, order on all of them...");

        this.mainWindow.interval = this.interval;
        this.mainWindow.setLabaleInformation("VALUES: Start Time = " + fromTime + " - End Time = " + toTime + " - Interval = " + this.interval);
                
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Reading, filtering, counting and ordering each interval...");
        
        
        //MAIN FUNCTION FOR READING JSON, FILTERING, COUNTING AND ORDERING FOR EACH INTERVAL
        //MAY TAKE SOME TIME TO EXECUTE
        this.mainWindow.listsForPloting_fromListManager = jsonparsert.selectListForMainWindow_forPloting_linkedListOfLinkedLists(fromTime, toTime, this.interval, this.responce);
       
        
        
        if(this.mainWindow.listsForPloting_fromListManager == null){
            this.mainWindow.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph = 0;
            this.mainWindow.setLabaleInformation("The lists come back empty with current search criteria, please modify search and run again! Check interval");
        }else{
            this.mainWindow.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph = 1;
        }
        //CHECK INTEGRITY AND LISTS
        //listsForPloting_fromListManager.checkIntegrityAndData();

        progress += 10;
        setProgress(progress);
        publish("ALL DONE!");
        
        return 0;
    }
    
    //FUNCTION FOR PRINTING OR DISPLAYING HTE publish() CONTENT ON THE MAIN WINDOW
    @Override
    protected void process(final List<String> chunks){
        for(final String string : chunks){
            this.mainWindow.setLabaleInformation(string);
        }
    }
    
    //FUNCTION TO THROUGH EXCEPTION IF THE THREAD IS INTERUPTED
    private static void failIfInterrupted() throws InterruptedException{
        if(Thread.currentThread().isInterrupted()){
            throw new InterruptedException("Interrupted while working with request!");
        }
    }
    
}
