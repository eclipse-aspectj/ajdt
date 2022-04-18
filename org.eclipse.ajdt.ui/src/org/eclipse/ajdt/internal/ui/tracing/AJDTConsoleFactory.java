package org.eclipse.ajdt.internal.ui.tracing;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

public class AJDTConsoleFactory implements IConsoleFactory {
    private IConsoleManager fConsoleManager;
    private AJDTEventTraceConsole fConsole = null;


    public AJDTConsoleFactory() {
        fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
        fConsoleManager.addConsoleListener(new IConsoleListener() {
            public void consolesAdded(IConsole[] consoles) {
            }

            public void consolesRemoved(IConsole[] consoles) {
              for (IConsole console : consoles) {
                if (console == fConsole) {
                  fConsole = null;
                }
              }
            }

        });
    }

    public void openConsole() {
        if (fConsole == null) {
            fConsole = new AJDTEventTraceConsole();
            fConsoleManager.addConsoles(new IConsole[]{fConsole});
        }
        fConsoleManager.showConsoleView(fConsole);
    }

}
