package classes;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console {
	
	String consoleText;
	
	private JTextArea console;
	private JScrollPane consoleScroller;
	
	Console() {
		this.console = new JTextArea();
		this.consoleScroller = new JScrollPane();
	}
	
	Console(JTextArea console, JScrollPane consoleScroller) {
		this.console = console;
		this.consoleScroller = consoleScroller;
	}
	
	public void clearConsole() {
		console.setText("");
	}
	
	public void printConsole(String text) {
		console.setText(console.getText() + text);
		System.out.print(text);
		
		scrollDown();
	}
	
	public void printConsoleLine(String text) {
		printConsole("\n" + text);
	}
	
	public void printConsoleError(String text, int errorcode) {
		console.setText(console.getText() +  text + " (errorcode: " + errorcode + ")");
		System.err.print(text);
		
		scrollDown();
	}
	
	public void printConsoleErrorLine(String text, int errorcode) {
		printConsoleError("\n" + text, errorcode);
	}
	
	public void scrollDown() {
		// moves the viewport of the console to the bottom, so that the last messages can be read
		JScrollBar vertical = consoleScroller.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}
}