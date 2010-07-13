package org.eclipse.ajdt.ui.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderInputStream extends InputStream {

	private Reader reader;
	
	public ReaderInputStream(Reader reader){
		this.reader = reader;
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return reader.read();
	}

	
    public void close() throws IOException {
    	reader.close();
    } 
	
}
