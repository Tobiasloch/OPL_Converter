package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OplHeader {
	
	/*	errorcodes: 100-200
	 * 0: no error	
	 * 
	 * 100: keine OplFile angegeben
	 * 101: br.readline() hat exception geworfen
	 * 102: OplFile, does not exist
	 * 103: Es wurden nicht 4 Gruppen durch den Matcher gefunden
	 * 104: die ID konnte nicht geladen werden
	 * 
	 * */
	
	final String HEADER_REG_EX = "\\d{10} \\S+\\p{Blank}*"
			+ "\\d{10}(?<fileInformation>(?:\\s\\S+)+) "
			+ "\\p{Blank}*\\d{10}(?:\\s\\S+)+ "
			+ "\\p{Blank}*\\d{10} \\d{10} \\d{10}(?<typeName>(?:\\s\\S+)+) "
			+ "\\p{Blank}*(?<type>(?:\\s\\S+)+) \\p{Blank}*(?<ID>\\d{10})";
	
	ArrayList<OplType> types; // Liste die die Opl Typen enthält
	String fileInformation; // Informationen zur Datei. Meistens Ort der Aufnahme bzw. Dateiname
	
	File OplFile; // Datei die ausgelesen werden soll
	
	Console console; // optional: Konsole
	
	OplHeader() {
		this(new File(""));
	}
	
	OplHeader(File OplFile) {
		this.OplFile = OplFile;
		
		types = new ArrayList<OplType>();
		console = new Console();
	}
	
	OplHeader(File OplFile, Console console) {
		this(OplFile);
		
		this.console = console;
	}
	
	
	public int extractHeaderInformation() {
		if (OplFile != null) return extractHeaderInformation(OplFile);
		else {
			console.printConsoleErrorLine("Es wurde keine Opl Datei angegeben!", 100);
			return 100;
		}
	}
	
	/**
	 * 
	 * @author t.loch
	 * @param OplFile reads the data from the OplFile
	 * @exception if the buffered reader cant read the line it throws an IOException
	 * @return returns the errorcode
	 * 
	 */
	public int extractHeaderInformation(File OplFile) {
		this.OplFile = OplFile;
		
		// Überprüfe ob die Datei existiert
		if (!OplFile.exists()) {
			console.printConsoleErrorLine("Die OPL Datei existiert nicht!", 102);
			return 102;
		}
		
		// Muster erstellen, nach dem später gesucht werden kann
		Pattern headerPattern = Pattern.compile(HEADER_REG_EX);
		
		// wird true wenn header gefunden; wichtig, damit der alg. den Header eindeutig findet
		boolean foundHeader = false;
		
		// für das Fehlerfinden
		long startHeader = -1;
		long lineCounter = 0;
		
		try {
			// Die Datei Laden
			FileReader fr = new FileReader(OplFile);
			BufferedReader br = new BufferedReader(fr);
			
			for (String line = br.readLine(); line!=null; line = br.readLine()) {
				Matcher m = headerPattern.matcher(line);
				lineCounter++;
				
				if (m.find()) {
					if (startHeader == -1) startHeader = lineCounter;
					
					// überprüft ob genug Daten gefunden wurden
					if (m.groupCount() != 4) {
						console.printConsoleErrorLine("Es wurden nicht 4 Variablen in der Zeile gefunden, sondern" + 
								m.groupCount() + "!; Zeile: " + lineCounter, 103);
						return 103;
					}
					
					// load file Information
					fileInformation = m.group(1);
					
					OplType type = new OplType();
					
					// load typeName
					type.setTypeName(m.group(2));
					
					// load type
					type.setType(m.group(3));
					
					// load type ID
					long id = 0;
					
					try {
						id = Long.parseLong(m.group(4));
					} catch (NumberFormatException nfe) {
						console.printConsoleErrorLine("Die ID konnte nicht richtig gelesen werden!; Zeile: " 
								+ lineCounter, 104);
						nfe.printStackTrace();
						return 104;
					}
					
					type.setTypeID(id);
					
					types.add(type);
				} else if (foundHeader) {
					console.printConsoleLine("Der Header wurde von Zeile " + startHeader 
							+ " bis Zeile " + --lineCounter + "ausgelesen");
					break;
				}
			}
		} catch (IOException e) {
			console.printConsoleErrorLine("Es Gab ein Problem mit dem Lesen der Datei!", 101);
			e.printStackTrace();
			
			return 101;
		}
		
		return 0;
	}
}