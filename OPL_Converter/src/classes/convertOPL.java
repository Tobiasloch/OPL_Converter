package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

public class convertOPL {
	
	/* errorleves: 200-299
	 * 
	 * 0: no error
	 * 
	 * 200: übergebene Referenzen sind nicht vorhanden
	 * 201: der fehlerstatus des headers ist nicht 0
	 * 202: FileNotFoundException
	 * 203: outputFile already exists
	 * 204: outputFile could not be created
	 * 205: id in block element not found
	 * 206: cancelled progress
	 * 
	 * */
	
	private final String BLOCK_HEADER = "(?<time>\\d{10}) \\d{10} (?<SZP>\\d{3}) \\d{3} \\d{10} \\d{3} \\d{3}";
	private final String BLOCK_ELEMENT = "(?<time>\\d{10}) (?<id>\\d{10}) \\d{1} \\d{2} \\d{1} (?<value>\\d{10})";
	
	private JFrame mainFrame;
	
	private final String DEFAULT_DELIMITER = ";";
	
	// for error handling
	ArrayList<Long> notMatchingLines; // lines that did not match any pattern
	ArrayList<Long> linesInWrongBlock; // lines that where in the wrong second block
	
	private File outputFile;
	private OplHeader header; // header of the opl file
	
	private String delimiter; // Trenn String; wird zwischen jeden wert gepackt
	
	private Console console;
	
	public convertOPL() {
		this(null, null);
	}
	
	public convertOPL(OplHeader header, File outputFile) {
		this(header, outputFile, new Console());
	}
	
	public convertOPL(OplHeader header, File outputFile, Console console) {
		this.setHeader(header);
		this.setOutputFile(outputFile);
		this.setConsole(console);
		
		notMatchingLines = new ArrayList<Long>();
		linesInWrongBlock = new ArrayList<Long>();
		delimiter = DEFAULT_DELIMITER;
		setMainFrame(null);
	}

	public int convertToTextFile() {
		return convertToTextFile(this.header, this.outputFile, this.console, delimiter);
	}
	
	public int convertToTextFile(OplHeader header, File outputFile) {
		return convertToTextFile(header, outputFile, new Console(), DEFAULT_DELIMITER);
	}
	
	public int convertToTextFile(OplHeader header, File outputFile, Console console, String delimiter) {
		///////////////////////////////////////////////////////////////////////////////////
		// checking for errors
		if (console == null) {
			console = new Console();
			console.printConsoleWarningLine("Es wurde keine Konsole übergeben, also wird eine neue erstellt.", 200);
		}
		if (outputFile == null) {
			console.printConsoleErrorLine("Es wurde keine outputFile übergeben!", 200);
			return 200;
		} else if (outputFile.exists()) {
			console.printConsoleErrorLine("Die outputFile existier bereits! \nPfad: " + outputFile, 203);
			return 203;
		}
		if (header == null) {
			console.printConsoleErrorLine("Es wurde keine header übergeben! pointer:" + header, 200);
			return 200;
		}
		
		int headerStatus = header.checkErrorStatus();
		if (headerStatus != 0) { // wenn es ein Fehlercode beim header gibt
			console.printConsoleErrorLine("Der Header ist fehlerhaft!", headerStatus);
			return headerStatus;
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		
		// creating monitor
		ProgressMonitor monitor = new ProgressMonitor(mainFrame, "Vorgang wird ausgeführt...", "", 0, 100);
		long progress = 0;
		long fileSize = header.getOplFile().length();
		
		FileReader fr;
		FileWriter fw;
		try {
			// open input file
			fr = new FileReader(header.getOplFile());
			BufferedReader br = new BufferedReader(fr);
			
			// set up pattern
			Pattern blockHeaderPattern = Pattern.compile(BLOCK_HEADER);
			Pattern blockElementPattern = Pattern.compile(BLOCK_ELEMENT);
			
			// create output file
			try {
				if (!outputFile.createNewFile()) {
					console.printConsoleErrorLine("Die outputFile konnte nicht erstellt werden! Pfad: " + outputFile.getAbsolutePath(), 204);
					return 204;
				}
			} catch  (Exception e) {
				console.printConsoleErrorLine("Die outputFile konnte nicht erstellt werden! Pfad: " + outputFile.getAbsolutePath(), 204);
				return 204;
			}
			
			// open output file
			fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// linecounter
			long linecounter = 1;
			
			// declare line string
			String line;
			
			// defining time values to check if the header has the same time like the body
			long headerTime = -1;
			long elementTime = -1;
			
			// define arraylist that has elements that will be in the lane
			ArrayList<OplTypeElement> blockElements;
			
			// jump to end of header
			for(; linecounter < header.getHeaderEnd()+1; linecounter++) br.readLine();
			
			// generates an order in the header elements, so that it can be sorted
			header.generateOrder();
			
			// writing first line
			// first to columns not types
			bw.write("TIME" + delimiter);
			bw.write("SZP" + delimiter);
			for (OplTypeElement element : header.getAllElements()) {
				bw.write(element.getName().trim() + delimiter);
			}
			bw.newLine();
			
			line = br.readLine();
			while(line!=null) {
				// set up matcher
				Matcher blockHeaderMatcher = blockHeaderPattern.matcher(line);
				
				if (blockHeaderMatcher.find()) { // wenn eine block header gefunden wurde
					// get time of header
					headerTime = Long.parseLong(blockHeaderMatcher.group(1));

					// convert to date
					Date time = new Date();
					time.setTime(headerTime * 1000); // date needs time in miliseconds and unix timestamp has time in seconds
					
					bw.write(time.toString() + delimiter); 				// Zeitstempel
					bw.write(blockHeaderMatcher.group(2) + delimiter);	// SZP Wert
				}
				progress += line.length()+2;
				line = br.readLine();
				
				// set up elements in line
				blockElements = new ArrayList<OplTypeElement>();
				
				// schleife geht solange wie die datei nicht leer ist und keine header gefunden wird
				while (!blockHeaderMatcher.find() && line!= null) {
					Matcher blockElementMatcher = blockElementPattern.matcher(line);

					if (blockElementMatcher.find()) {
						// extract unix time stamp
						elementTime = Long.parseLong(blockElementMatcher.group(1));
						
						if (elementTime == headerTime) {
							// extract id and value
							long id = Long.parseLong(blockElementMatcher.group(2));
							long value = Long.parseLong(blockElementMatcher.group(3));
							
							OplTypeElement element = header.getElementFromId(id);
							if (element != null) {
								element.setValue(value);
								blockElements.add(element);
							} else {
								console.printConsoleWarningLine("In Zeile (" + linecounter + ") wurde die ID der variable nicht gefunden! "
										+ "Sie wurde daraufhin übersprungen.", 205);
							}
						} else {
							linesInWrongBlock.add(linecounter);
						}
					} else {
						notMatchingLines.add(linecounter);
					}
					progress += line.length()+2;
					line = br.readLine();
					
					if (line != null) blockHeaderMatcher = blockHeaderPattern.matcher(line);
					linecounter++;
					
					if (monitor.isCanceled()) {
						console.printConsoleLine("der Vorgang wurde abgebrochen.");
						br.close();
						bw.close();
						return 206;
					}
					monitor.setProgress(getProgressinPercent(progress, fileSize));
					monitor.setNote("Fortschritt: " + progress/1000 + "/" + fileSize/1000);
				}
				
				// wenn der Block zu ende ist, dann wird er ausgegeben
				Collections.sort(blockElements);

				for (OplTypeElement element : blockElements) {
					bw.write(element.getValue() + delimiter);
				}			
				bw.newLine();
				linecounter++;
			}
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			console.printConsoleErrorLine("Es gab ein Problem beim lesen der Datei!", 202);
			return 202;
		}
		printWrongLines();
		
		return 0;
	}
	
	private void printWrongLines() {
		if (notMatchingLines.size() > 0) {
			console.printConsole("Folgende Zeilen wurden übersprungen, da kein Zellenelement oder Zeilenkopf erkannt wurde: ");
			
			for (Long item : notMatchingLines) {
				console.printConsole(", " + item);
			}
		}
		
		if (linesInWrongBlock.size() > 0) {
			console.printConsole("In folgenden Zeilen wurde eine Zeile im falschen block festgestellt: ");
			
			for (Long item : linesInWrongBlock) {
				console.printConsole(", " + item);
			}
		}
	}
	
	private int getProgressinPercent(long start, long end)  {
		return (int)(((float)start/(float)end)*100);
	}
	
	// opl file that is being converted
	public File getFile() {
		return header.getOplFile();
	}
	
	public void setFile(File file) {
		header.setOplFile(file);
	}

	public OplHeader getHeader() {
		return header;
	}

	public void setHeader(OplHeader header) {
		this.header = header;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
}
