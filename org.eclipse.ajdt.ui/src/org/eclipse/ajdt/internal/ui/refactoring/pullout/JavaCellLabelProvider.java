/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring.pullout;

import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * A cell label provider that can be used to provide icons / text for elements in a table,
 * assuming the data displayed in the cell is an IJavaElement.
 * @author kdvolder
 */
public abstract class JavaCellLabelProvider extends CellLabelProvider {
	
	private JavaUILabelProvider javaLabelProvider;

	/**
	 * Rather than have every cell in the table create its onw JavaUILabelProvider,
	 * it better to create one in the table and pass this is as a parameter here.
	 */
	public JavaCellLabelProvider(JavaUILabelProvider javaLabelProvider) {
		this.javaLabelProvider = javaLabelProvider;
	}
	
	/**
	 * Each column in a table displays some data that is somehow derived from the element
	 * Object that is associated with that line in the table.
	 * <p>
	 * You must implement this method to extract the column data from a row element. 
	 */
	public abstract Object getColumnData(Object element);

	@Override
    public void update(ViewerCell cell) {
		Object columnData = getColumnData(cell.getElement());
		cell.setText(javaLabelProvider.getText(columnData));
		cell.setImage(javaLabelProvider.getImage(columnData));
	}

}
