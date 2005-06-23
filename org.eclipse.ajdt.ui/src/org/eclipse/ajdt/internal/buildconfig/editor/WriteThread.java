/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.buildconfig.editor;

public class WriteThread extends Thread{
	
	private static WriteThread instance;
	//we only need to remember the last posted job and not the
	//whole queue because jobs in the middle of the queue get
	//overwritten anyway by the subsequent ones
	private static Runnable nextJob;
	
	private WriteThread(){}
	
	public static synchronized void asyncExec(Runnable r){
		nextJob = r;
		if (instance == null){
			instance = new WriteThread();
			instance.start();
		}
	}
	
	private synchronized Runnable getNextJob(){
		Runnable temp = nextJob;
		if (temp == null){
			instance = null;
			return null;
		}
		nextJob = null;
		return temp;
	}
	
	public void run(){
		Runnable myRunnable = getNextJob();
		while(myRunnable != null){
			myRunnable.run();
			myRunnable = getNextJob();
		}
	}
}
