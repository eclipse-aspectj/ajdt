/*
 * Created on 04-Nov-2004
 */
package figures.support;

import java.io.*;
import figures.FigureElement;
import figures.Group;
import figures.gui.*;

/**
 * @author hawkinsh
 */
public aspect SaveAndRestore {

	declare parents: FigureElement+ implements Serializable;

	pointcut editorClosing() : 
		execution(* FigureWindowListener.windowClosing(..));
	pointcut editorOpening() :
		execution(* FigureWindowListener.windowOpened(..));

	before() : editorClosing() {
		Group g = Main.getFigurePanel().getGroup();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream("project.fig"));
			oos.writeObject(g);
			oos.close();
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
	after() returning : editorOpening() {
		try {
			File f = new File("project.fig");
			if (f.exists()) {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(f));
				Group g = (Group)ois.readObject();
				ois.close();
				Main.getFigurePanel().setGroup(g);
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
}
