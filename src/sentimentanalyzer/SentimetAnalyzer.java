package sentimentanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nikolin
 */


public class SentimetAnalyzer extends javax.swing.JFrame {
    
    public DefaultTableModel tableModelTopTen;
    public DefaultTableModel tableModelTitles;
    public final ChartController charts = new ChartController();
    public EntryElementsManager listsForPloting_fromListManager;
    
    public Date fromDateTime;
    public Date toDateTime;
    public int interval = 0;
    
    public LocalDateTime forNaming;
    
    public CheckBoxModelListener listenerForTableCheckboxes = new CheckBoxModelListener(this);
    
    public int localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph = 0;
    
    private WorkingThread backgroundThreadWorker;
    
    public static final TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
    
    
    

    /**
     * Creates new form SentimetAnalyzer
     */
    public SentimetAnalyzer () {
        initComponents();
        
        this.cleanTheTableModels();
        
        //KETO THIRREN VETEM PER TI TESTUAR QE PUNOJNE OK
        this.charts.createAndPopulatePieChart__TESTER(this.pnlPieChart);
        this.charts.initializeCountWordsXYSeriesChart(this.pnlWordsCount, "Word Count", "Intervals of:", "word", 0, "words");
        this.charts.initializeCountWordsXYSeriesChart(this.pnlWordsSentiment, "Sentiment Score", "score", "word", 0, "sentiment");
        //KA NGA NJE FUNKSION IDENTIK SI KETO POR PA ___TESTER QE THIRRET ME VLERAT REALE KUR I GJENERON
        
        
        
        
        //TESTING DATES AND TIME SETTING, NOT TO PUT MANUALLY
        try{         
            SimpleDateFormat testingDateFrom = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
            testingDateFrom.setTimeZone(tz);
            Date testingFromNew = testingDateFrom.parse("2015-09-22 01:00:00.000");
           
            SimpleDateFormat testingDateTo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
            testingDateTo.setTimeZone(tz);
            Date testingToNew = testingDateTo.parse("2015-09-23 01:00:00.000");
            

            
            this.spnDayChoser.setValue(testingFromNew);
            this.spnToTime.setValue(testingToNew);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        this.btnRun.setText("RUN");
        this.btnRun.setBackground(Color.green);
        this.btnRunLT.setText("Run on Last Tweets");
        this.btnRunLT.setBackground(Color.GRAY);
    }
    
   
    
    //NEW FUNCTION TO CONTROL LISTENER ON THE CHECKBOXES////////////
    public void plotDataToGraphs_forXYSeriesChart_WordsCount(String word, int row){
        if(this.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph == 1){
            this.charts.plotXYSeriesForWord(this.pnlWordsCount, this.listsForPloting_fromListManager, word, row, this.interval);
        }else{
            //WHAT TO DO IF LISTS ARE EMPTY AND NOT ABLE TO PLOT GRPAHS WITH DATA, MESSAGES etc.
        }
    } 
    
    
    // ** FUNCTION TO CONTROL LISTENER ON THE CHECKBOXES // FOR THE SENTIMENT SCORE
    
    public void plotDataToGraphs_forXYSeriesChart_SentimentScore(String word, int row){
        if(this.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph == 1){
            this.charts.plotXYSeriesForSentimentScore(this.pnlWordsSentiment, this.listsForPloting_fromListManager, word, row, this.interval);
        }else{
            //WHAT TO DO IF LISTS ARE EMPTY AND NOT ABLE TO PLOT GRPAHS WITH DATA, MESSAGES etc.
        }
    }
    
    
    
    //FUNCTION FOR PLOTING THE PIE CHART
    public void plotPieChartGraphToMainWindow(double positive, double negative, double neutral){
        this.charts.createAndPopulatePieChart(this.pnlPieChart, positive, negative, neutral);
    }
    
    public void removeDataFromGrpah_forXYSeriesChart_WordsCount(int row, String were){
        if(were.equals("words")){
            this.charts.removeLIneOfDataTo_XYSeriesLineChart(row, were);
        }else if(were.equals("sentiment")){
            //later on
            
        }
    }
    
    //Remove line of data from Sentiment Score
    public void removeDataFromGrpah_forXYSeriesChart_SentimentScore(int row, String were){
        if(were.equals("sentiment")){
            this.charts.removeLIneOfDataTo_XYSeriesLineChart(row, were);
            
  
        }
    }
    
    
    ////////////////////////////////////////////////////////////////
    
    public void cleanTheTableModels(){
        this.tableModelTopTen = new DefaultTableModel(){
                    @Override
                    public Class<?> getColumnClass(int column) {
                        if (column == 2) {
                            return Boolean.class;
                        } else {
                            return String.class;
                        }
                    }
                };
        this.tableModelTopTen.addColumn("Word");
        this.tableModelTopTen.addColumn("Wordcount");
        this.tableModelTopTen.addColumn("Plot");
      
        //Titles table
        this.tableModelTitles = new DefaultTableModel();
        this.tableModelTitles.addColumn("Datetime of retrieval");
        this.tableModelTitles.addColumn("Content");
        this.tableModelTitles.addColumn("Sentiment score");
        
        
        this.tblIndividualTweets.setModel(this.tableModelTitles);
        
        this.tblIndividualWords.setModel(this.tableModelTopTen);
        this.tblIndividualWords.getModel().addTableModelListener(listenerForTableCheckboxes);
        
        //Adding vertical Scrollbar 
        tblIndividualTweets.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        //new JScrollPane(tblIndividualTweets, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
    }
    
    public void prepareButtonsForRun(){
        this.btnRun.setText("RUN");
        this.btnRun.setBackground(Color.green);
        this.btnRunLT.setText("Run on Last Tweets");
        this.btnRunLT.setBackground(Color.GRAY);
        
        this.btnTestProgressBar.setEnabled(true);
        this.btnGetJsonHTTP.setEnabled(true);
        this.btnClean.setEnabled(true);
        this.btnSave.setEnabled(true);
        this.btnRunLT.setEnabled(true);
        this.btnRun.setEnabled(true);
    }
    
    public void prepareButtonsForCancel(){
        this.btnRun.setText("CANCEL");
        this.btnRun.setBackground(Color.red);

        this.btnTestProgressBar.setEnabled(false);
        this.btnGetJsonHTTP.setEnabled(false);
        this.btnClean.setEnabled(false);
        this.btnSave.setEnabled(false);
        this.btnRunLT.setEnabled(false);
    }
    
    public void prepareButtonsForCancelLastTweets(){
        this.btnRunLT.setText("CANCEL");
        this.btnRunLT.setBackground(Color.red);
        
        this.btnTestProgressBar.setEnabled(false);
        this.btnGetJsonHTTP.setEnabled(false);
        this.btnClean.setEnabled(false);
        this.btnSave.setEnabled(false);
        this.btnRun.setEnabled(false);
    }
    
    public void setLabaleInformation(String labelToSet){
        this.lblMode.setText(labelToSet);
    }
    
    private void cancelWorkingThread() {
        this.backgroundThreadWorker.cancel(true);
        this.backgroundThreadWorker = null;
        
    }
    
    private void runWorkingCycle(){
        //PASSING ALL NECCESSARY PARAMETERS TO INITIALIZE AND USE THE THREAD LIKE IN THE MAIN WINDOW
        this.backgroundThreadWorker = new WorkingThread((Date)this.spnDayChoser.getValue(), (Date)this.spnToTime.getValue(), this.txtURL.getText(), this.txtKeyword.getText(), this.txtDecode.getText(), this.sldSubIntDuration.getValue(), this, this.pnlWordsCount);
    
        this.backgroundThreadWorker.addPropertyChangeListener(new PropertyChangeListener(){
            @Override
            public void propertyChange(final PropertyChangeEvent event){
                switch(event.getPropertyName()){
                    case "progress":
                        //WHEN setProgress() IS CALLED YOU CAN SET THE VALUE OF PROGRESS HERE
                        prgBarRunningTIme.setValue((Integer) event.getNewValue());
                        break;
                    case "state":
                        switch((SwingWorker.StateValue) event.getNewValue()){
                            case DONE:
                                //WHEN THE THREAD IS FINISHED HERE YOU CAN SET THE RESULTS
                                prepareButtonsForRun();
                                prgBarRunningTIme.setValue(100);
                                backgroundThreadWorker = null;
                                break;
                            case STARTED:
                                //IF NECESSARY DOSOMETHING HERE
                                prgBarRunningTIme.setValue(0);
                            case PENDING:
                                //IF NECCESSARY DOSOMETHING HERE
                                break;
                        }
                        break;
                }
            }
        });
    
    this.backgroundThreadWorker.execute();
  }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblIndividualTweets = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblIndividualWords = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        prgBarRunningTIme = new javax.swing.JProgressBar();
        txtNumberOfWords = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        lblQuery = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        spnDayChoser = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        spnToTime = new javax.swing.JSpinner();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        pnlPieChart = new javax.swing.JPanel();
        pnlWordsSentiment = new javax.swing.JPanel();
        lblMode = new javax.swing.JLabel();
        sldSubIntDuration = new javax.swing.JSlider();
        jLabel10 = new javax.swing.JLabel();
        btnRun = new javax.swing.JButton();
        pnlWordsCount = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        btnClean = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnTestProgressBar = new javax.swing.JButton();
        btnGetJsonHTTP = new javax.swing.JButton();
        txtURL = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtKeyword = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtDecode = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        btnRunLT = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sentiment Analyzer");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tblIndividualTweets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblIndividualTweets);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 390, 270));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Individual tweets in the chosen time interval");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        tblIndividualWords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblIndividualWords.setOpaque(false);
        tblIndividualWords.setShowHorizontalLines(false);
        tblIndividualWords.setShowVerticalLines(false);
        jScrollPane2.setViewportView(tblIndividualWords);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 50, 290, 250));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Top words in the seek time interval");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, -1, -1));

        prgBarRunningTIme.setStringPainted(true);
        getContentPane().add(prgBarRunningTIme, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 690, -1));

        txtNumberOfWords.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtNumberOfWords.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtNumberOfWords.setText("10");
        getContentPane().add(txtNumberOfWords, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, 40, 30));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 50, 510, 10));

        jLabel3.setText("TOP WORD nr");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 20, -1, -1));

        lblQuery.setText("query:");
        getContentPane().add(lblQuery, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, -1, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 120, 510, 10));

        jLabel6.setText("Select Date(s) and Time");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 130, -1, -1));

        spnDayChoser.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        spnDayChoser.setModel(new javax.swing.SpinnerDateModel());
        getContentPane().add(spnDayChoser, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 150, 190, -1));

        jLabel8.setText("FROM");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 150, -1, -1));

        jLabel9.setText("TO");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 180, -1, -1));

        spnToTime.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        spnToTime.setModel(new javax.swing.SpinnerDateModel());
        getContentPane().add(spnToTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 180, 190, -1));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 220, 510, 20));
        getContentPane().add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 380, 1210, 20));

        javax.swing.GroupLayout pnlPieChartLayout = new javax.swing.GroupLayout(pnlPieChart);
        pnlPieChart.setLayout(pnlPieChartLayout);
        pnlPieChartLayout.setHorizontalGroup(
            pnlPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );
        pnlPieChartLayout.setVerticalGroup(
            pnlPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 410, Short.MAX_VALUE)
        );

        getContentPane().add(pnlPieChart, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 390, 450, 410));

        javax.swing.GroupLayout pnlWordsSentimentLayout = new javax.swing.GroupLayout(pnlWordsSentiment);
        pnlWordsSentiment.setLayout(pnlWordsSentimentLayout);
        pnlWordsSentimentLayout.setHorizontalGroup(
            pnlWordsSentimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlWordsSentimentLayout.setVerticalGroup(
            pnlWordsSentimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        getContentPane().add(pnlWordsSentiment, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 390, 760, 200));

        lblMode.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lblMode.setForeground(new java.awt.Color(255, 0, 51));
        lblMode.setText("TEST MODE, TEST GRAPHS");
        getContentPane().add(lblMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, -1, -1));

        sldSubIntDuration.setMaximum(24);
        sldSubIntDuration.setMinimum(1);
        sldSubIntDuration.setMinorTickSpacing(1);
        sldSubIntDuration.setPaintLabels(true);
        sldSubIntDuration.setPaintTicks(true);
        sldSubIntDuration.setValue(10);
        sldSubIntDuration.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                sldSubIntDurationMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                sldSubIntDurationMouseMoved(evt);
            }
        });
        sldSubIntDuration.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sldSubIntDurationMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sldSubIntDurationMousePressed(evt);
            }
        });
        getContentPane().add(sldSubIntDuration, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 80, 320, 30));

        jLabel10.setText("Subinterval duration: every 10 hours");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 50, -1, 30));

        btnRun.setText("Run");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });
        getContentPane().add(btnRun, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 230, 90, 70));

        javax.swing.GroupLayout pnlWordsCountLayout = new javax.swing.GroupLayout(pnlWordsCount);
        pnlWordsCount.setLayout(pnlWordsCountLayout);
        pnlWordsCountLayout.setHorizontalGroup(
            pnlWordsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlWordsCountLayout.setVerticalGroup(
            pnlWordsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 210, Short.MAX_VALUE)
        );

        getContentPane().add(pnlWordsCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 590, 760, 210));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setText("(all data, from begin to end time according to records)");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, -1, -1));

        btnClean.setText("Clean");
        btnClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanActionPerformed(evt);
            }
        });
        getContentPane().add(btnClean, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 270, 80, 30));

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        getContentPane().add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 230, 80, 30));

        btnTestProgressBar.setText("Test PB");
        btnTestProgressBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestProgressBarActionPerformed(evt);
            }
        });
        getContentPane().add(btnTestProgressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 240, -1, -1));

        btnGetJsonHTTP.setText("Test HTTP Requests");
        btnGetJsonHTTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetJsonHTTPActionPerformed(evt);
            }
        });
        getContentPane().add(btnGetJsonHTTP, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 270, -1, -1));

        txtURL.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtURL.setText("http://localhost:8080/TestTweets/src=t&fromdate=[[from]]&todate=[[to]]&keyword=[[key]]&decode=[[decode]].json");
        txtURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtURLActionPerformed(evt);
            }
        });
        getContentPane().add(txtURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 320, 490, 30));

        jLabel5.setText("(URL Variables, [[from]] - from Date, [[to]] - to Date, [[key]] - keyword, [[decode]] - 0/1)");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 350, -1, -1));

        txtKeyword.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtKeyword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtKeyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKeywordActionPerformed(evt);
            }
        });
        getContentPane().add(txtKeyword, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 10, 120, 30));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("KEYWORD:");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 20, -1, -1));

        txtDecode.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDecode.setText("1");
        getContentPane().add(txtDecode, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 10, 40, 30));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setText("DECODE:");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 20, -1, -1));

        btnRunLT.setText("Run on Last Tweets");
        btnRunLT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunLTActionPerformed(evt);
            }
        });
        getContentPane().add(btnRunLT, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 230, 150, 70));

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public void useSQLMode(){
        this.fromDateTime = (Date)this.spnDayChoser.getValue();
        this.toDateTime = (Date)this.spnToTime.getValue();
        
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(spnDayChoser.getValue()));
        
        
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(spnToTime.getValue()));

        // *** Date convert to "yyyy/MM/dd HH:mm:ss"  fromDateTime
        SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        formater.setTimeZone(tz);
        String fromDateTimeLocal = formater.format(spnDayChoser.getValue());
        
        try {
            this.fromDateTime = formater.parse(fromDateTimeLocal);
        } catch (ParseException ex) {
            Logger.getLogger(SentimetAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // *** Date convert to "yyyy/MM/dd HH:mm:ss"  toDateTIme
        SimpleDateFormat formater2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        formater2.setTimeZone(tz);
        String toDateTimeLocal = formater2.format(spnToTime.getValue());
        
        try {
            this.toDateTime = formater2.parse(toDateTimeLocal);
        } catch (ParseException ex) {
            Logger.getLogger(SentimetAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void sldSubIntDurationMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sldSubIntDurationMouseClicked
        // TODO add your handling code here:
         this.jLabel10.setText(String.valueOf("Subinterval duration: every " + this.sldSubIntDuration.getValue()) + " hours" );
    }//GEN-LAST:event_sldSubIntDurationMouseClicked

    private void sldSubIntDurationMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sldSubIntDurationMouseDragged
        // TODO add your handling code here:
         this.jLabel10.setText(String.valueOf("Subinterval duration: every " + this.sldSubIntDuration.getValue()) + " hours" );
    }//GEN-LAST:event_sldSubIntDurationMouseDragged

    private void sldSubIntDurationMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sldSubIntDurationMouseMoved
        // TODO add your handling code here:
         
    }//GEN-LAST:event_sldSubIntDurationMouseMoved

    private void sldSubIntDurationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sldSubIntDurationMousePressed
        // TODO add your handling code here:
      
    }//GEN-LAST:event_sldSubIntDurationMousePressed

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        // TODO add your handling code here:
        if(this.btnRun.getText().equals("RUN")){
            this.runWorkingCycle();
            
            this.prepareButtonsForCancel();
        }else if(this.btnRun.getText().equals("CANCEL")){
            this.cancelWorkingThread();
            
            this.prepareButtonsForRun();
        }
        
    }//GEN-LAST:event_btnRunActionPerformed

    private void btnCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanActionPerformed
        // TODO add your handling code here:
        this.cleanTheTableModels();
       // this.charts.initializeCountWordsXYSeriesChart(this.pnlWordsCount, "Word Count", "count", "word", 0, "words");
        // Cleans the 3 charts and prepares them for a new operation
        this.charts.createAndPopulatePieChart__TESTER(this.pnlPieChart);
        this.charts.initializeCountWordsXYSeriesChart(this.pnlWordsCount, "Word Count", "Intervals of:", "word", 0, "words");
        this.charts.initializeCountWordsXYSeriesChart(this.pnlWordsSentiment, "Sentiment Score", "score", "word", 0, "sentiment");
        
    }//GEN-LAST:event_btnCleanActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        Date d = new Date();
        this.charts.saveWordCountsChartPNG(d.toString().replace(":", "").replace(" ", "") + "INTERV" + this.interval);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnTestProgressBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestProgressBarActionPerformed
        // TODO add your handling code here:
        this.prgBarRunningTIme.setMaximum(100);
        this.prgBarRunningTIme.setMinimum(0);
        
        for(int i=0; i<=100; i = i + 10){
            this.prgBarRunningTIme.setValue(i);
        }
    }//GEN-LAST:event_btnTestProgressBarActionPerformed

    private void btnGetJsonHTTPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetJsonHTTPActionPerformed
        // TODO add your handling code here:
        String response = HTTPRequests.getJsonWithHTTPRequest(this.spnDayChoser.getValue().toString(), this.spnToTime.getValue().toString(), this.txtKeyword.getText(), this.txtDecode.getText(), this.txtURL.getText());
        
        if(response == null){
            JOptionPane.showMessageDialog(null, "HTTP Request Error", "HTTP Request", JOptionPane.INFORMATION_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "HTTP Request Successful", "HTTP Request", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnGetJsonHTTPActionPerformed

    private void txtKeywordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKeywordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKeywordActionPerformed

    private void btnRunLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunLTActionPerformed
        // TODO add your handling code here:
        if(this.btnRunLT.getText().equals("Run on Last Tweets")){
             this.runWorkingCycle();
            
            this.prepareButtonsForCancelLastTweets();
        }else if(this.btnRunLT.getText().equals("CANCEL")){
            this.cancelWorkingThread();
            
            this.prepareButtonsForRun();
        }
        
    }//GEN-LAST:event_btnRunLTActionPerformed

    private void txtURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtURLActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SentimetAnalyzer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SentimetAnalyzer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SentimetAnalyzer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SentimetAnalyzer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        //NEEDED FOR MULTI THREADING 
        SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new SentimetAnalyzer().setVisible(true);
          }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClean;
    private javax.swing.JButton btnGetJsonHTTP;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnRunLT;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnTestProgressBar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lblMode;
    private javax.swing.JLabel lblQuery;
    private javax.swing.JPanel pnlPieChart;
    private javax.swing.JPanel pnlWordsCount;
    private javax.swing.JPanel pnlWordsSentiment;
    private javax.swing.JProgressBar prgBarRunningTIme;
    private javax.swing.JSlider sldSubIntDuration;
    private javax.swing.JSpinner spnDayChoser;
    private javax.swing.JSpinner spnToTime;
    private javax.swing.JTable tblIndividualTweets;
    private javax.swing.JTable tblIndividualWords;
    private javax.swing.JTextField txtDecode;
    private javax.swing.JTextField txtKeyword;
    private javax.swing.JTextField txtNumberOfWords;
    private javax.swing.JTextField txtURL;
    // End of variables declaration//GEN-END:variables
}
