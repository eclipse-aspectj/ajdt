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
project. Export this project as a plugin and install it into your
eclipse/plugins directory.  Then, when doing a headless build, 
point to the scripts/build.xml ant file in this project.  Pass
it the same arguments as you would a non-AspectJ headless build.

Additionally, you must set the ajdt.pdebuild.home environment variable.
Set it to the root directory of this plugin.  Eg- 

ajdt.pdebuild.home=${eclipse_home}/plugins/org.eclipse.ajdt.pde.build