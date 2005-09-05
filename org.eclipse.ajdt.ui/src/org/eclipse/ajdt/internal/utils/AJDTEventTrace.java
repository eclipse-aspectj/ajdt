/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
	AMC  08/12/2002 Added classpath information to build event output
**********************************************************************/
package org.eclipse.ajdt.internal.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.aspectj.ajde.Ajde;
import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class records the interesting events in the lifecycle of the
 * plugin for use in problem diagnosis.
 */
public class AJDTEventTrace {

	public static interface EventListener {
		public void ajdtEvent( Event e );
	};	

	private static Vector listeners = new Vector( );

	// TO DO - should really use a ring buffer to avoid memory
	// loss in long-running eclipse
	private static Vector eventTrace = new Vector();
	private static final int MAXENTRIES=5000;
	
	
	private static final int EVT_STARTUP = 0;
	private static final int EVT_EDITOR_OPEN = 1;
	private static final int EVT_EDITOR_CLOSE = 2;
	private static final int EVT_CONFIG_SELECT = 3;
	private static final int EVT_STRUCTURE_VIEW = 4;
	private static final int EVT_MODEL_UPDATED = 5;
	private static final int EVT_BUILD = 6;
	private static final int EVT_VIEW_ACTION = 7;
	private static final int EVT_NEW_PROJECT = 8;
	private static final int EVT_NEW_CONFIG = 9;
	private static final int EVT_NEW_ASPECT = 10;
	private static final int EVT_NODE_CLICK = 11;
	private static final int EVT_PROJ_PROPS_CHANGED = 12;
	private static final int EVT_PROJ_PROPS_DEFAULTED = 13;
	private static final int EVT_BUILD_CONFIG_READ = 14;
	private static final int EVT_BUILD_CONFIG_WRITE = 15;
	private static final int EVT_GENERAL = 99999;
	
	
	/**
	 * record version information & content of the preference store
	 */
	public static void startup( ) { 
		StringBuffer eventData = new StringBuffer( );
		eventData.append( "\tAJDT version: " ); //$NON-NLS-1$
		eventData.append( AspectJUIPlugin.VERSION );
		eventData.append( " for Eclipse " ); //$NON-NLS-1$
		eventData.append( EclipseVersion.MAJOR_VERSION + "." + EclipseVersion.MINOR_VERSION); //$NON-NLS-1$
		
		eventData.append( "\n\tAspectJ Compiler version: " ); //$NON-NLS-1$
		eventData.append( Ajde.getDefault().getVersion() );
		
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		String[] props = AspectJUIPlugin.getDefault().getPluginPreferences().propertyNames();
		for ( int i = 0; i < props.length; i++ ) {
			eventData.append( "\n\t" ); //$NON-NLS-1$
			eventData.append( props[i] );
			eventData.append( " = " ); //$NON-NLS-1$
			eventData.append( store.getString( props[i] ) );
		}
	
		addEvent( new AJDTEventTrace.Event( EVT_STARTUP, eventData ) );
	}
	
	public static void generalEvent( String eventDescription) {
		addEvent(new Event(EVT_GENERAL,eventDescription));
	}
	
	public static void editorOpened( IFile onFile ) {
		addEvent ( new Event( EVT_EDITOR_OPEN, onFile.getName() ) );	
	 }
	
	public static void editorClosed( IFile onFile ) { 
		addEvent ( new Event( EVT_EDITOR_CLOSE, onFile.getName() ) );		
	}

	public static void buildConfigSelected( String file, IProject project ) { 
		addEvent( new Event( EVT_CONFIG_SELECT, file, project.getName() ) );
	}

	public static void structureViewRequested( String file ) { 
		addEvent( new Event( EVT_STRUCTURE_VIEW, file ) );
	}
	
	public static void modelUpdated( IFile forFile ) { 
		addEvent( new Event( EVT_MODEL_UPDATED, forFile.getName() ) );
	}
	
	public static void build( IProject project, String config, String classpath ) { 
		addEvent( new Event( EVT_BUILD, 
				new Object[] { project.getName(), config, classpath } ) );
	}
	
	public static void outlineViewAction( String action, IFile onFile ) { 
		addEvent( new Event( EVT_VIEW_ACTION, action, onFile.getName() ) );
	}
	
	public static void newProjectCreated( IProject project ) { 
		addEvent( new Event( EVT_NEW_PROJECT, project.getName() ) );
	}
	
	public static void newConfigFileCreated( IFile file ) { 
		addEvent( new Event( EVT_NEW_CONFIG, file.getName() ) );	
	}
	
	public static void newAspectCreated( IFile file ) { 
		addEvent( new Event( EVT_NEW_ASPECT, file.getName() ) );
	}
	
	public static void nodeClicked( String label, IMarker target ) {
		addEvent( new Event( EVT_NODE_CLICK, label, target ) );	
	}
	
	public static void projectPropertiesChanged(IProject project) {
		addEvent( new Event(EVT_PROJ_PROPS_CHANGED,project.getName()));
	}
	
	public static void projectPropertiesDefaulted(IProject project) {
		addEvent( new Event(EVT_PROJ_PROPS_DEFAULTED,project.getName()));
	}

	public static void buildConfigRead(IFile configFile) {
		addEvent( new Event(EVT_BUILD_CONFIG_READ,configFile.getName()));
	}
	
	public static void buildConfigWrite(IFile configFile) {
		addEvent( new Event(EVT_BUILD_CONFIG_WRITE,configFile.getName()));
	}

	private static void addEvent( final Event e ) {
		if (eventTrace.size()>MAXENTRIES) eventTrace.remove(0);
		eventTrace.add( e );
		if ( !listeners.isEmpty() ) {
			AspectJUIPlugin.getDefault().getDisplay().asyncExec( 
				new Runnable( ) {
					public void run( ) {
						for (Iterator it = listeners.iterator(); it.hasNext(); ) {
							((EventListener)it.next()).ajdtEvent( e );	
						}					
					}				
				}
			);
		}
	}

	public static void addListener( EventListener l ) {
		listeners.add( l );
		for ( Iterator it = eventTrace.iterator(); it.hasNext(); ) {
			l.ajdtEvent( (Event) it.next() );
		}	
	}
	
	public static void removeListener( EventListener l ) {
		listeners.remove( l );
	}

	public static class Event {
		Date time;
		int eventCode;
		Object[] eventData;		
		
		public Event( int eventCode ) {
			init( eventCode, new Object[0] );					
		}

		public Event( int eventCode, Object eventData ) {
			init( eventCode, new Object[] { eventData } );			
		}

		public Event( int eventCode, Object eventData1, Object eventData2 ) {
			init( eventCode, new Object[] { eventData1, eventData2 } );	
		}		
		
		public Event( int eventCode, Object[] eventData ) {
			init( eventCode, eventData );
		}
		
		private void init( int eventCode, Object[] eventData ) {
			time = new Date( );
			this.eventCode = eventCode;
			if ( eventData != null ) {
				this.eventData = eventData;							
			} else {
				this.eventData = new Object[0];
			}			
		}
		
		public String toString( ) {
			StringBuffer buff = new StringBuffer( );
			buff.append( DateFormat.getTimeInstance().format( time ) );
			buff.append( "\t " ); //$NON-NLS-1$
			switch( eventCode ) {
				case EVT_STARTUP:
					buff.append( "AJDT Plugin Startup\n" ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_EDITOR_OPEN:
					buff.append( "Editor opened on " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_EDITOR_CLOSE:
					buff.append( "Editor closed - " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_CONFIG_SELECT:
					buff.append( "Configuration file " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					buff.append( " selected for " ); //$NON-NLS-1$
					buff.append( eventData[1] );
					break;
				case EVT_STRUCTURE_VIEW:
					buff.append( "Structure view requested for " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_MODEL_UPDATED:
					buff.append( "Model update notification for " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_BUILD:
					buff.append( "Building " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					buff.append( " with config " ); //$NON-NLS-1$
					buff.append( eventData[1] );
					buff.append( ".\n\t\t CLASSPATH=" ); //$NON-NLS-1$
					buff.append( eventData[2] );
					break; 		
				case EVT_VIEW_ACTION:
					buff.append( "Outline view action triggered: " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					buff.append( " for " ); //$NON-NLS-1$
					buff.append( eventData[1] );
					break;
				case EVT_NEW_PROJECT:
					buff.append( "New project created: " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_NEW_CONFIG:
					buff.append( "New config file created: " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;
				case EVT_NEW_ASPECT:
					buff.append( "New aspect file created: " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					break;	
				case EVT_NODE_CLICK:
					buff.append( "Tree node selected: " ); //$NON-NLS-1$
					buff.append( eventData[0] );
					buff.append( ". Navigation target: " ); //$NON-NLS-1$
					if ( eventData[1] != null ) {
						IMarker marker = (IMarker) eventData[1];
						buff.append( marker.getResource().getName( ) );
						buff.append( " line " ); //$NON-NLS-1$
						try {
							Integer lineNo = (Integer) marker.getAttribute( IMarker.LINE_NUMBER );
							buff.append( lineNo.intValue() );
						} catch ( Exception ex ) {
							buff.append( "ERR" );	 //$NON-NLS-1$
						}
					} else {
						buff.append( "<None>" ); //$NON-NLS-1$
					}
					break;
				case EVT_PROJ_PROPS_CHANGED:
				    buff.append( "Compiler properties changed for project: " ); //$NON-NLS-1$
				    buff.append(eventData[0]);
				    break;	
				case EVT_PROJ_PROPS_DEFAULTED:
				    buff.append( "Compiler properties reset to default for project: " ); //$NON-NLS-1$
				    buff.append(eventData[0]);
				    break;
				case EVT_BUILD_CONFIG_READ:
				    buff.append( "Build configuration file read: " ); //$NON-NLS-1$
				    buff.append(eventData[0]);
				    break;
				case EVT_BUILD_CONFIG_WRITE:
				    buff.append( "Build configuration file written: " ); //$NON-NLS-1$
				    buff.append(eventData[0]);
				    break;
				case EVT_GENERAL:
					buff.append(eventData[0]);
					break;
			}			
			return buff.toString();
		}
	};		
}

