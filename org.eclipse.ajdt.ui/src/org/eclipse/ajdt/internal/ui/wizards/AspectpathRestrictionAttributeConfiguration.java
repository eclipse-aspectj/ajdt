package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.ClasspathAttributeConfiguration;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

public class AspectpathRestrictionAttributeConfiguration extends
        ClasspathAttributeConfiguration {

    public AspectpathRestrictionAttributeConfiguration() {
    }

    public boolean canEdit(ClasspathAttributeAccess attribute) {
        return true;
    }

    public boolean canRemove(ClasspathAttributeAccess attribute) {
        return attribute.getClasspathAttribute().getValue().length() > 0;
    }

    public ImageDescriptor getImageDescriptor(ClasspathAttributeAccess attribute) {
        return AspectJImages.JAR_ON_ASPECTPATH.getImageDescriptor();
    }

    public String getNameLabel(ClasspathAttributeAccess attribute) {
        return "Only the following elements are included on Inpath";
    }

    public String getValueLabel(ClasspathAttributeAccess attribute) {
        if (attribute.getClasspathAttribute().getValue().equals("")) {
            return PathBlock.NO_RESTRICTIONS;
        } else {
            return attribute.getClasspathAttribute().getValue();
        }
    }

    public IClasspathAttribute performEdit(Shell shell,
            ClasspathAttributeAccess attribute) {
        String oldValue = attribute.getClasspathAttribute().getValue();
        InputDialog d = new InputDialog(shell, "Add a restriction to the classpath container", 
                "Enter a comma separated list to specify a subset of this classpath container's\n" +
                "elements to be on the inpath.\n\n" +
                "Enter a fragment of the desired name to include.  Example, if the classpath\n" +
                "container includes:\njar1.jar\notherjar1.jar\notherjar2.jar\n\n" +
                "and you enter: \"otherjar\", the result will be to include otherjar1.jar and\n" +
                "otherjar2.jar on the inpath, and to exclude jar1.jar.",
                oldValue, null);
        int res = d.open();
        if (res == InputDialog.OK) {
            return JavaCore.newClasspathAttribute(attribute.getClasspathAttribute().getName(), d.getValue());
        } else {
            return attribute.getClasspathAttribute();
        }
    }

    public IClasspathAttribute performRemove(ClasspathAttributeAccess attribute) {
        return JavaCore.newClasspathAttribute(attribute.getClasspathAttribute().getName(), "");
    }

}
