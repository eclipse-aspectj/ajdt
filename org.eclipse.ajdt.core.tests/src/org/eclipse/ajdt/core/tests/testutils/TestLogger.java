/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.testutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.IAJLogger;

/**
 * Logger to help with builder tests
 */
public class TestLogger implements IAJLogger {

    List log;
    List buildLog;
    List tempBuildLog;
    boolean loggingBuildEvent = false;
    boolean foundSplit = false;
    
    /* (non-Javadoc)
     * @see org.eclipse.ajdt.core.IAJLogger#log(java.lang.String)
     */
    public void log(String msg) {
        if (log == null) {
            log = new ArrayList();
        }
        log.add(msg);
        
        if (buildLog == null) {
			buildLog = new ArrayList();
		}
        if (tempBuildLog == null) {
			tempBuildLog = new ArrayList();
		}
        
        StringBuffer sb = new StringBuffer(msg);
        if (sb.indexOf("=======================") != -1) { //$NON-NLS-1$
        	// we think we might be starting a build
        	foundSplit = true;
        } else if (foundSplit && (sb.indexOf("Build kind =") != -1)) { //$NON-NLS-1$
        	// this is the first line in the log after the "====" 
        	// when we do a build, therefore, we know we're doing 
        	// a build and want to start creating the buildLog.
        	foundSplit = false;
        	loggingBuildEvent = true;
        	tempBuildLog.add(msg);
        } else if (sb.indexOf("Total time spent in AJBuilder.build()") != -1) { //$NON-NLS-1$
			// this is the last log line when we build. 
        	loggingBuildEvent = false;
			tempBuildLog.add(msg);
			buildLog.add(new ArrayList(tempBuildLog));
			tempBuildLog.clear();
		} else if (loggingBuildEvent) {
			// we're in a build, therefore add msg to tempBuildLog.
			tempBuildLog.add(msg);
		}
    }

    /**
     * Returns whether or not the log contains the given string 
     */
    public boolean containsMessage(String msg) {
        for (Iterator iter = log.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            if (logEntry.indexOf(msg) != -1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the last line in the log that contains the given string
     * @param msg
     * @return
     */
    public String getMostRecentMatchingMessage(String msg) {
    	for (int i = log.size()-1; i >= 0; i--) {
           String logEntry = (String)log.get(i);
            if (logEntry.indexOf(msg) != -1) {
                return logEntry;
            }
        }
        return null;
    }
    
    /**
     * Returns the number of times the given string appears
     * in the log
     */
    public int numberOfEntriesForMessage(String msg) {
        int occurances = 0;
        for (Iterator iter = log.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            StringBuffer sb = new StringBuffer(logEntry);
            if (sb.indexOf(msg) != -1) {
                occurances++;
            }
        }
        return occurances;
    }
    
    /**
     * Returns the last given number of lines of the log
     */
    public List getMostRecentEntries(int numberOfLines) {
    	return log.subList(log.size() - numberOfLines,log.size());
    }
    
    /**
     * Clears the log
     */
    public void clearLog() {
    	log.clear();
    }
    
    public boolean isEmpty() {
    	return log.isEmpty();
    }
    
    /**
     * Prints the contents of the log to the screen - useful
     * in testcase development
     */
    public void printLog() {
    	System.out.println(""); //$NON-NLS-1$
    	System.out.println("Printing log begin ------------------------------------"); //$NON-NLS-1$
    	for (Iterator iter = log.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.out.println("LOG: " + element); //$NON-NLS-1$
		}
    	System.out.println("-------------------------------------- Printing log end"); //$NON-NLS-1$
    }
    
    public List getPreviousBuildEntry(int i) {
    	return (ArrayList)buildLog.get(buildLog.size() - i);
    }

	public void waitForLogToClear() {
		boolean logEmpty = log.isEmpty();
		int counter = 0;
		while (!logEmpty && (counter < 5)) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logEmpty = log.isEmpty();
			counter++;
		}
	}
	
	
	public int getNumberOfBuildsRun() {
		return buildLog.size();
	}
}
