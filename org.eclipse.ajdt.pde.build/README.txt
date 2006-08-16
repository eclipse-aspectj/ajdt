Build Automation for AspectJ-enabled plug-ins
=============================================

For information about the PDE Build mechanism for building plug-ins,
see the Eclipse Corner article here:

http://www.eclipse.org/articles/Article-PDE-Automation/automation.html

By default this process generates build.xml files which use the "javac"
Ant task. But this doesn't understand aspects, so any AspectJ-enabled
plug-ins containing aspects will not build properly.

A bug has been raised to consider this issue:
https://bugs.eclipse.org/bugs/show_bug.cgi?id=147432

In the meantime, a workaround is possible using the jar file in this
project. To do this, locate the lib/pdebuild-ant.jar file in
the org.eclipse.pde.build plugin. After making a backup of this file,
replace it with the one in this project. Now when the build
process executes it will generate build.xml files which use the "iajc"
Ant task. This will mean the AspectJ compiler is used to build all
projects (both AspectJ and Java projects).
