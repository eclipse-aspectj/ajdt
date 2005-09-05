/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/*
 * Very simple perspective, put a java package view on the left, 
 * a task view at the bottom and the outline view on the right.
 */
public class AspectJPerspective implements IPerspectiveFactory { 

  public AspectJPerspective() {
    super();
  }

	
  /**
   * create the layout for this perspective
   */
  public void createInitialLayout(IPageLayout layout) {

    String editorArea = layout.getEditorArea();

    // Place the Java Package View on the left
    IFolderLayout folder =
      layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
    folder.addView(JavaUI.ID_PACKAGES);
 
    // Place the Task View at the bottom
    IFolderLayout outputfolder =
      layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
    outputfolder.addView(IPageLayout.ID_TASK_LIST);

  }
}