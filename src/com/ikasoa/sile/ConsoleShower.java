package com.ikasoa.sile;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleShower {

	private MessageConsole console = null;

	private MessageConsoleStream consoleStream = null;

	private IConsoleManager consoleManager = null;

	private final String CONSOLE_NAME = "Console";

	public ConsoleShower() {
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		if (consoles.length > 0)
			console = (MessageConsole) consoles[0];
		else {
			console = new MessageConsole(CONSOLE_NAME, null);
			consoleManager.addConsoles(new IConsole[] { console });
		}
		consoleStream = console.newMessageStream();
	}

	public void show(String message) {
		if (message != null) {
			consoleManager.showConsoleView(console);
			consoleStream.print(message + "\n");
		}
	}

}