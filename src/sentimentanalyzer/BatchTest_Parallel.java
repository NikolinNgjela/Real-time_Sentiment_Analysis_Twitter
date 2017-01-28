package sentimentanalyzer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.python.util.PythonInterpreter;

import com.opencsv.CSVReader;

import org.python.core.*;

public class BatchTest_Parallel {
	PySystemState sys;
	PythonInterpreter pi;
	public static final String modulename = "parallelsentix_executor";
	public static final String objectname = "MasterCaller";
	public static final String sentixdicname = "sentix3.csv";
	public static final String amplifiersname = "amplifiers.txt";
	public static final String decreasersname = "decreasers.txt";
	public static final String serializedDictsName = "serializedDicts.bin";
	public static String dsep;
	
	public static String osname = null;
	
	public static String getOsName() {
		if (osname == null) {
			osname = System.getProperty("os.name");
		}
		return osname;
	}
	
	public static boolean isWindowsOS() {
		getOsName();
		if (osname.toLowerCase().contains("windows")) {
			return true;
		}
		return false;
	}
	
	public BatchTest_Parallel() {
		
		/* Responsibilities:
		 * 
		 * - Create thread pool
		 * - Import dictionaries 
		 * 
		*/
		
		String patternmodule = (isWindowsOS())?"site-packages\\Pattern-2.6-py2.7.egg":"site-packages/Pattern-2.6-py2.7.egg";
		
		sys = Py.getSystemState();
		// sys.path.append(new PyString("/opt/jython/Lib/site-packages/Pattern-2.6-py2.7.egg"));
		sys.path.append(new PyString(patternmodule));
		pi = new PythonInterpreter(null, sys);
		pi.exec("import "+modulename);
		// pi.exec("S="+modulename+"."+objectname+"('"+sentixdicname+"','"+amplifiersname+"','"+decreasersname+"')");
		pi.exec("S="+modulename+"."+objectname+"(serializedDictsName='"+serializedDictsName+"')");
	}
	
	public List<Integer> classify_sentiment(List<String> l) {
		/* Responsibilities:
		 * 
		 * - Split text array for worker threads 
		 * - Run worker threads 
		 * 
		*/
		
		PyList pl = new PyList(l);
		pi.set("tl",pl);
		
		pi.exec("scores = S.classify_sent(tl)");
		PyList sentscores = (PyList)pi.get("scores");
		
		List<Integer> results = new ArrayList<Integer>();
		
		for (Object a : sentscores) {
			int f = (int)a;
			results.add(f);
		}
		
		return results;
	}
	
	public List<List> classify_full(List<String> l) {
		PyList pl = new PyList(l);
		pi.set("tl",pl);
		
		pi.exec("(sentscores,subjscores) = S.classify_full(tl)");
		PyList sentscores = (PyList)pi.get("sentscores");
		PyList subjscores = (PyList)pi.get("subjscores");
		
		List<Integer> sentresults = new ArrayList<Integer>();
		List<Integer> subjresults = new ArrayList<Integer>();
		
		for (Object a : sentscores) {
			int f = (int)a;
			sentresults.add(f);
		}
		
		for (Object a : subjscores) {
			int f = (int)a;
			subjresults.add(f);
		}
		
		List<List> allresults = new ArrayList<List>();
		
		allresults.add(sentresults);
		allresults.add(subjresults);
		
		return allresults;
	}
	
	

	public static void main(String[] args) throws PyException {
		
		long startTime,endTime;
		/*
		 * Stats
		 * 20000 texts : 270 sec (single CPU)
		 * 
		 *  4 CPUs (8 Threads) - called from JVM
		 * Init:	34.182410647 s
		 * Sentiment classification:	154.158188697 s
		 * 
		 * 4 CPUs (8 Threads) - called from JVM (-Xmx8192m), with serialized dicts
		 * Init:	3.496912269 s
		 * Sentiment classification:	68.428114553 s
		 * 
		 * 4 CPUs (8 Threads) - called from JVM (-Xmx8192m), with serialized dicts
		 * Init:	4.38340884
		 * Sentiment+subjectivity classification:	134.747773376 s
		 * 
		 * 4 CPUs (8 Threads) - called from Jython console
		 * Init: ~22 s
		 * Sentiment classification: ~70 s
		 * 
		 */
		
		// Read CSV file with tweets
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader("texts.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    List<String[]> ll = null;
		try {
			ll = reader.readAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // take first element of each array of strings in the list
	    List<String> l = new ArrayList<String>();
	    for (String[] a : ll) l.add(a[0]);
		
	    /*********** START TIMING ************/
		startTime = System.nanoTime();
		
		BatchTest_Parallel c = new BatchTest_Parallel();
		
		endTime = System.nanoTime();
		System.out.println("Init:\t"+(double)(endTime-startTime)/1000000000);
		/*********** END TIMING ************/
		
		startTime = System.nanoTime();
		
		// XXX BEWARE: the method to invoke from the Java GUI is classify_sentiment, not classify_full, which is not used for now
		// List<Integer> subjscores = c.classify_sentiment(l);
		List<List> fullscores = c.classify_full(l);
		
		endTime = System.nanoTime();
		System.out.println("Sentiment+subjectivity classification:\t"+((double)(endTime-startTime)/1000000000)+" s");
		/*********** END TIMING ************/
	}
}