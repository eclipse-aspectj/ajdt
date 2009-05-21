package org.eclipse.ajdt.internal.ui.refactoring;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.util.TableLayoutComposite;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class PushInRefactoringInputPage extends UserInputWizardPage {
    
    private PushInRefactoringDescriptor descriptor;
    
	public PushInRefactoringInputPage(String name, PushInRefactoringDescriptor descriptor) {
		super(name);
		this.descriptor = descriptor;
	}

	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout= new GridLayout(2, false);
		result.setLayout(layout);

//		Label label = new Label(result, SWT.NONE);
//		label.setText("&ITD name:");
//		createNameField(result);

        Label label = new Label(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        label.setText("Select ITDs to push in");
        GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan= 2;
        gridData.verticalIndent= 5;
        label.setLayoutData(gridData);

		
		createTable(result);
//		
//        label = new Label(result, SWT.NONE);
//        label.setText("&Affects taget types:");
//        createAffectsTypesField(result);
//        
//        label = new Label(result, SWT.NONE);
//        label.setText("&Declaring Aspect:");
//        createDeclaringAspectField(result);

        handleInputChanged();
	}

//	private Label createNameField(Composite result) {
//        Label field = new Label(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        List itds = getPushInRefactoring().getITDs();
//        StringBuffer sb = new StringBuffer();
//        for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
//            IntertypeElement itd = (IntertypeElement) itdIter.next();
//            sb.append(itd.getElementName() + "\n");
//        }
//        field.setText(sb.toString());
//        return field;
//    }
//	private Label createAffectsTypesField(Composite result) {
//        Label field = new Label(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        IntertypeElement itd = getPushInRefactoring().getITD();
//        try {
//            field.setText(new String( itd.getTargetType()));
//        } catch (JavaModelException e) {
//            field.setText("Unknown");
//        }
//        return field;
//    }
//    private Label createDeclaringAspectField(Composite result) {
//        Label field = new Label(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        IntertypeElement itd = getPushInRefactoring().getITD();
//        field.setText(itd.getParent().getElementName());
//        return field;
//    }
    


	private PushInRefactoring getPushInRefactoring() {
		return (PushInRefactoring) getRefactoring();
	}

	private void handleInputChanged() {
		RefactoringStatus status= new RefactoringStatus();
		PushInRefactoring refactoring= getPushInRefactoring();
		try {
            status.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
        } catch (OperationCanceledException e) {
        } catch (CoreException e) {
            status.merge(RefactoringStatus.createFatalErrorStatus(e.getMessage()));
        }
		setPageComplete(!status.hasError());
		int severity= status.getSeverity();
		String message= status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}

	/*
	 * from @link ExtractClassUserInputWizardPage
	 */
    private void createTable(Composite parent) {
//        Composite result= new Composite(parent, SWT.NONE);
//        GridLayout layout= new GridLayout(2, false);
//        layout.marginHeight= 0;
//        layout.marginWidth= 0;
//        result.setLayout(layout);
        GridData gridData; //= new GridData(GridData.FILL_BOTH);
//        gridData.horizontalSpan= 2;
//        result.setLayoutData(gridData);


        TableLayoutComposite layoutComposite= new TableLayoutComposite(parent, SWT.NONE);
        layoutComposite.addColumnData(new ColumnWeightData(40, convertWidthInCharsToPixels(20), true));
        layoutComposite.addColumnData(new ColumnWeightData(40, convertWidthInCharsToPixels(20), true));
        layoutComposite.addColumnData(new ColumnWeightData(40, convertWidthInCharsToPixels(20), true));
        final CheckboxTableViewer tv= CheckboxTableViewer.newCheckList(layoutComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tv.setContentProvider(new FieldContentProvider());
        createColumns(tv);

        Table table= tv.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        gridData= new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);
        tv.setInput(descriptor);
        final Object[] fields= descriptor.getITDs();
        for (int i= 0; i < fields.length; i++) {
            IAspectJElement field= (IAspectJElement) fields[i];
            tv.setChecked(field, true);
        }
        tv.refresh(true);
        gridData= new GridData(GridData.FILL_BOTH);
        gridData.heightHint= SWTUtil.getTableHeightHint(table, Math.max(fields.length,5));
        layoutComposite.setLayoutData(gridData);
    }
    
    private void createColumns(final CheckboxTableViewer tv) {

        // three columns: Aspect name, ITD name, target type name(s)
        
        TableViewerColumn aspectTypeColumn = new TableViewerColumn(tv, SWT.LEAD);
        aspectTypeColumn.setLabelProvider(new CellLabelProvider() {
            public void update(ViewerCell cell) {
                Object elt = cell.getElement();
                if (elt instanceof IntertypeElement || elt instanceof DeclareElement) {
                    IAspectJElement itd = (IAspectJElement) elt;
                    cell.setText(itd.getParent().getElementName());
                }
            }
        });
        TableColumn column= aspectTypeColumn.getColumn();
        column.setText("Aspect type");
        
        TableViewerColumn itdNameColumn = new TableViewerColumn(tv, SWT.LEAD);
        itdNameColumn.setLabelProvider(new CellLabelProvider() {
            public void update(ViewerCell cell) {
                Object elt = cell.getElement();
                if (elt instanceof IntertypeElement) {
                    IAspectJElement itd = (IAspectJElement) elt;
                    cell.setText(itd.getElementName());
                } else if (elt instanceof DeclareElement) {
                    DeclareElement de = (DeclareElement) elt;
                    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(de).javaElementToProgramElement(de);
                    String details = ipe.getDetails();
                    cell.setText(details);
                }
            }
        });
        column = itdNameColumn.getColumn();
        column.setText("Intertype Name");
        
        TableViewerColumn targetTypeColumn = new TableViewerColumn(tv, SWT.LEAD);
        targetTypeColumn.setLabelProvider(new CellLabelProvider() {
            public void update(ViewerCell cell) {
                Object elt = cell.getElement();
                if (elt instanceof IntertypeElement) {
                    IntertypeElement itd = (IntertypeElement) elt;
                    try {
                        cell.setText(new String(itd.getTargetType()));
                    } catch (JavaModelException e) {
                    }
                } else if (elt instanceof DeclareElement) {
                    DeclareElement de = (DeclareElement) elt;
                    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(de).javaElementToProgramElement(de);
                    String details = ipe.getDetails();
                    cell.setText(details);
                }
            }
        });
        column = targetTypeColumn.getColumn();
        column.setText("Target type");
    }

    /*
     * from @link ExtractClassUserInputWizardPage
     */
    public class FieldContentProvider implements IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof PushInRefactoringDescriptor) {
                PushInRefactoringDescriptor descriptor= (PushInRefactoringDescriptor) inputElement;
                return descriptor.getITDs();
            }
            return null;
        }

    }

}