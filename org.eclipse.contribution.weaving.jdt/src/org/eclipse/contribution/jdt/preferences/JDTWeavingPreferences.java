package org.eclipse.contribution.jdt.preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.contribution.jdt.IsWovenTester;
import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class JDTWeavingPreferences
	extends PreferencePage
	implements IWorkbenchPreferencePage {

    private final boolean isWeaving = IsWovenTester.isWeavingActive();
    private Shell shell;
    
    private final static Version MIN_WEAVER_VERSION = new Version(1, 6, 3);
    
	public JDTWeavingPreferences() {
		super("JDT Weaving preferences");
		setDescription("Preferences for the JDT Weaving plugin.  Enable and disable weaving here.");
	}

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        
        Composite area = new Composite(parent, SWT.NONE);
        shell = area.getShell();
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        area.setLayout(gridLayout);
        
        Label messageLabel = new Label(area, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        if (isWeaving) {
            messageLabel.setText("JDT Weaving is currently ENABLED");
        } else {
            messageLabel.setText("JDT Weaving is currently DISABLED");
        }
        
        Button changeWeavingButton = new Button(area, SWT.PUSH);
        changeWeavingButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        if (isWeaving) {
            changeWeavingButton.setText("Click to DISABLE (requires restart)");
        } else {
            changeWeavingButton.setText("Click to ENABLE (requires restart)");
        }
        
        changeWeavingButton.addSelectionListener(new SelectionListener() {
        
            public void widgetSelected(SelectionEvent e) {
                changeWeavingState();
            }
        
            public void widgetDefaultSelected(SelectionEvent e) {
                changeWeavingState();
            }
        });
        
        
        Label reindexLabel = new Label(area, SWT.NONE);
        reindexLabel.setText("Click here if you want to reindex your workspace so\n" +
        		"that all Java-like compilation units can be located by the indexer\n " +
        		"(e.g., during Java searches)");
        
        Button reindexButton = new Button(area, SWT.PUSH );
        reindexButton.setText("Reindex now");
        reindexButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                new ReindexingJob().schedule();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                new ReindexingJob().schedule();
            }
        });
        
        // warning label if wrong version of the weaver is being used.
        Label warningLabel = new Label(area, SWT.NONE);
        warningLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        warningLabel.setText("Note that disabling the weaving service may disable some features " +
        		"in the workbench.");
        
        Label weaverVersion = new Label(area, SWT.NONE);
        weaverVersion.setText(getWeaverVersionInfo());

        return area;
    }

    public void init(IWorkbench workbench) {
    }
	
    private void changeWeavingState() {
        String areYouSure = "Are you sure that you want to " + 
                (isWeaving ? "DISABLE" : "ENABLE") + " JDT Weaving?";
        boolean result = MessageDialog.openQuestion(shell, "Enable/disable JDT Weaving", areYouSure);
        
        if (!result) {
            return;
        }
        
//        ProvisioningAction action = getProvisioningAction();
//        IStatus status = action.execute(getActionParameters());
        
        // a little crude
        // find the config.ini
        // go through each line and filter out the osgi.framework.extensions line
        String configArea = FrameworkProperties.getProperty("osgi.configuration.area") + "config.ini";
        configArea = configArea.replaceAll(" ", "%20");
        IStatus success;
        try {
            File f = new File(new URI(configArea));
            BufferedReader br = new BufferedReader(new FileReader(f));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                if (! line.contains("osgi.framework.extensions")) {
                    sb.append(line + "\n");
                }
            }
            
            if (!isWeaving) {
                sb.append("osgi.framework.extensions=org.eclipse.equinox.weaving.hook\n");
            }
            br.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(sb.toString());
            bw.close();
            success = new Status(IStatus.OK, JDTWeavingPlugin.ID, "Weaving service successfully " + 
                    (isWeaving ? "DISABLED" : "ENABLED"));
        } catch (Exception e) {
            success = new Status(IStatus.ERROR, JDTWeavingPlugin.ID, e.getMessage(), e);
        }

        JDTWeavingPlugin.getInstance().getLog().log(success);
        
        if (success.getSeverity() == IStatus.OK) {
            boolean doRestart = MessageDialog.openQuestion(shell, "Restart", "Weaving will be " + 
                    (isWeaving ? "DISABLED" : "ENABLED") + " after restarting the workbench.\n\n" +
                    		"Do you want to restart now?");
            if (doRestart) {
                PlatformUI.getWorkbench().restart();
            }
        } else {
            ErrorDialog.openError(shell, "Error", "Could not " + (isWeaving ? "DISABLE" : "ENABLE") + 
                    " JDT Weaving", success);
        }
    }
    
    private String getWeaverVersionInfo() {
        BundleDescription weaver = 
            Platform.getPlatformAdmin().getState(false).
            getBundle("org.aspectj.weaver", null);
        
        if (weaver != null) {
            if (MIN_WEAVER_VERSION.compareTo(weaver.getVersion()) <= 0) {
                return "";
//                return "AspectJ weaver version " + weaver.getVersion().toString() + " OK!";
            } else {
                return "No compatible version of org.aspectj.weaver found.  " +
                "JDT Weaving requires 1.6.3 or higher.  Found version " +
                weaver.getVersion();
            }
        } else {
            return "org.aspectj.weaver not installed.  JDT Weaving requires 1.6.3 or higher.";
        }
    }

//    private Map<String, Object> getActionParameters() {
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put(EclipseTouchpoint.PARM_MANIPULATOR, (new EquinoxFrameworkAdminFactoryImpl()).createFrameworkAdmin().getManipulator());
//        params.put(ActionConstants.PARM_PROP_NAME, "osgi.framework.extensions");
//        
//        if (isWeaving) {
//            params.put(ActionConstants.PARM_PROP_VALUE, "");
//            params.put(ActionConstants.PARM_PREVIOUS_VALUE, "org.eclipse.equinox.weaving.hook");
//        } else {
//            params.put(ActionConstants.PARM_PROP_VALUE, "org.eclipse.equinox.weaving.hook");
//            params.put(ActionConstants.PARM_PREVIOUS_VALUE, "");
//        }
//        return params;
//    }
//
//    private ProvisioningAction getProvisioningAction() {
//        return ActionFactory.create(SetProgramPropertyAction.ID);
//    }
//	
}