package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OplHeader {
	
	/*	errorcodes: 100-199
	 * 0: no error	
	 * 
	 * 100: keine OplFile angegeben
	 * 101: br.readline() hat exception geworfen
	 * 102: OplFile, does not exist
	 * 103: Es wurden nicht 4 Gruppen durch den Matcher gefunden
	 * 104: die ID konnte nicht geladen werden
	 * 105: kein Header wurde gefunden
	 * 106: keine Daten wurden bis jetzt ausgelesen (extractHeaderInformation wurde nicht aufgerufen)
	 * 107: Variablen wurden verändert ohne Aufruf der extractHeaderInformation
	 * 108: Problem mit den Intervallgrenzen der Header Teile
	 * 109: Header von verschiedenen Dateien stimmen nicht überein
	 * 110: Header conflict handling wurde verändert und extractHeaderInformation jedoch noch nicht
	 * 111: conflict handling out of range
	 * 
	 * */
	
	public static final String HEADER_REG_EX = "\\d{10} \\S+\\p{Blank}*"
			+ "\\d{10} (?<fileInformation>(?:\\S+\\s)+) "
			+ "\\p{Blank}*\\d{10}(?:\\s\\S+)+ "
			+ "\\p{Blank}*\\d{10} \\d{10} \\d{10}(?<typeName>(?:\\s\\S+)+) "
			+ "\\p{Blank}*(?<type>(?:\\s\\S+)+) \\p{Blank}*(?<id>\\d{10})";
	
	public static final String[] HEADER_REGEX = {"\\d{10}(?:\\s\\S+)+", // 1
			"\\d{10}(?<fileInformation>(?:\\s\\S+)+)", // 2
			"\\d{10}(?:\\s\\S+)+", // 3
			"\\d{10} \\d{10} (?<id>\\d{10})(?<typeName>(?:\\s\\S+)+)", // 4
			"(?<type>(?:\\S+\\s)+)",  // 5
			"\\d{10}"}; // 6
	
	public static final int[] HEADER_REGEX_END = {76, 152, 228, 326, 391, 401};
	
	/*
	public static final int HEADER_FIRST_END = 77;
	public static final String HEADER_SECOND_REGEX = "\\d{10} (?<fileInformation>(?:\\S+\\s)+)";
	public static final int HEADER_SECOND_END = 153;
	public static final String HEADER_THIRD_REGEX = "\\d{10}(?:\\s\\S+)+";
	public static final int HEADER_THIRD_END = 229;
	public static final String HEADER_FOURTH_REGEX = "\\d{10} \\d{10} \\d{10}(?<typeName>(?:\\s\\S+)+)";
	public static final int HEADER_FOURTH_END = 327;
	public static final String HEADER_FIFTH_REGEX = "(?<type>(?:\\s\\S+)+)";
	public static final int HEADER_FIFTH_END = 392;
	public static final String HEADER_SIXTH_REGEX = "(?<id>\\d{10})";
	public static final int HEADER_SIXTH_END = 402;
	*/
	
	public static final int DEFAULT_CONFLICT_HANDLING = 0;
	public static final int[] MIN_MAX_CONFLICT_HANDLING_TYPES = {0, 2};
	
	private HashMap<String, OplType> types; // Liste die die Opl Typen enthält
	private String fileInformation; // Informationen zur Datei. Meistens Ort der Aufnahme bzw. Dateiname
	
	private long headerStart;
	private long headerEnd;
	
	private int errorStatus;
	
	// Nach welchen verfahren mehrere Header Dateien mit unterschiedlichen Headern behandelt werden sollen
	// 0: Abbruch bei unterschiedlichen Headern
	// 1: mit null werten wird gearbeitet
	// 2: nur gleiche Variablen werden übertragen
	private int conflictHandling; 
	
	private File[] OplFiles; // Datei die ausgelesen werden soll
	
	private Console console; // optional: Konsole
	
	public OplHeader() {
		this(new File[0]);
	}
	
	public OplHeader(File[] OplFiles) {
		this.OplFiles = OplFiles;
		
		types = new HashMap<String, OplType>();
		console = new Console();
		
		errorStatus = 106;
		
		setConflictHandling(DEFAULT_CONFLICT_HANDLING);
		
		headerEnd = -1;
		headerStart = -1;
	}
	
	public OplHeader(File[] OplFiles, Console console) {
		this(OplFiles);
		
		this.console = console;
	}
	
	public int extractHeaderInformation() {
		if (OplFiles.length > 0) return extractHeaderInformation(OplFiles);
		else {
			console.printConsoleErrorLine("Es wurde keine Opl Datei angegeben!", 100);
			return 100;
		}
	}
	
	public int extractHeaderInformation(File[] OplFiles) {
		this.OplFiles = OplFiles;
		
		// header informationen resetten
		fileInformation = "";
		types = new HashMap<String, OplType>();
		
		OplHeader workingHeader = new OplHeader(OplFiles, console);
		for (File f : OplFiles) {
			int errorcode = workingHeader.extractHeaderInformation(f);
			
			// check errorlevel
			if (errorcode != 0) {
				console.printConsoleErrorLine("Der Vorgang wurde bei der Datei " + f + " abgebrochen", errorcode);
				return errorcode;
			}
			
			workingHeader.generateOrder();
			
			// wenn f das erste element ist oder die aktuelle Header mit der ausgelesenen übereinstimmt
			if (fileInformation.equals("") && types.size() == 0) {
				fileInformation = workingHeader.getFileInformation();
				types = workingHeader.getHashMapTypes();
			} else {
				if (getConflictHandling() == 0) {
					if (!workingHeader.equals(this)) {
						console.printConsoleErrorLine("Die folgende Datei stimmt nicht mit den vorherigen überein! Header: " + f, 109);
						errorStatus = 109;
						return 109;
					}
				} else if (getConflictHandling() == 1) {
					// bei auftreten einer neuen Variable wird diese hinzugefügt
					HashMap<String, OplType> workingMap = workingHeader.getHashMapTypes();
					
					for (String key : workingMap.keySet()) { // den key von jedem ausgelesenem Typ auslesen
						OplType t = workingMap.get(key);
						
						if (types.containsKey(key)) { // wenn der Typ bereits existeirt, dann dürfen nur noch nicht vorhandene Typen importiert werden
							HashMap<String, OplTypeElement> workingType = t.getHash();
							HashMap<String, OplTypeElement> typeHash = types.get(key).getHash();

							typeHash.putAll(workingType);
						} else {// wenn der Typ noch nicht existiert, dann kann er ohne Konflikte hinzugfefügt werden
							types.put(key, t);
						}
					}
					
				} else if (getConflictHandling() == 2) {
					// bei auftreten einer variable die nicht in allen maps vorkommt, dann wird diese gelöscht
					HashMap<String, OplType> workingMap = workingHeader.getHashMapTypes();
					
					for (String key : types.keySet()) { // den key von jedem ausgelesenem Typ auslesen
						if (workingMap.containsKey(key)) { // wenn der Typ bereits existeirt, dann wird überprüft ob er auch im workinHeader vorkommt, sonst wird er gelöscht
							HashMap<String, OplTypeElement> typeHash = types.get(key).getHash();
							HashMap<String, OplTypeElement> t = workingMap.get(key).getHash();
							
							for (String elemKey : typeHash.keySet()) {
								if (!t.containsKey(elemKey)) typeHash.remove(elemKey);
							}
						} else {
							types.remove(key);
						}
					}
				}
			}
		}
		
		errorStatus = 0;
		return errorStatus;
	}
	
	public boolean equals(OplHeader header) {
		if (fileInformation.equals(header.fileInformation) && types.equals(header.getHashMapTypes())) {
			return true;
		}
		
		return false;
	}
	
	public int getTypeIndex(String type) {
		int index = 0;
		for (OplType item : types.values()) {
			if (type.equals(item.getType())) {
				return index;
			}
			index++;
		}
		
		return -1;
	}
	
	public void generateOrder() {
		int i = 0;
		for (OplTypeElement element : getAllElements()) {
			element.setOrder(i);
			
			i++;
		}
	}
	
	public OplTypeElement getElementFromId(long id) {
		for (OplTypeElement element : getAllElements()) {
			if (element.getId() == id) return element;
		}
		
		return null;
	}
	
	public ArrayList<OplTypeElement> getAllElements(){
		ArrayList<OplTypeElement> elements = new ArrayList<OplTypeElement>();
		
		for (OplType item : types.values()) {
			for (OplTypeElement element : item.getElements()) {
				elements.add(element);
			}
		}
		
		return elements;
	}
	
	public OplType getType(String type) {
		return types.get(type);
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
		// Überprüfe ob die Datei existiert
		if (!OplFile.exists()) {
			console.printConsoleErrorLine("Die OPL Datei existiert nicht!", 102);
			errorStatus = 102;
			return 102;
		}
		
		// Muster erstellen, nach dem später gesucht werden kann
		Pattern[] headerPattern = new Pattern[HEADER_REGEX.length];
		// header pattern für jeden einzelnen String laden
		for (int i = 0; i < headerPattern.length; i++) headerPattern[i] = Pattern.compile(HEADER_REGEX[i]);
		
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
				boolean found = true;
				Matcher[] m = new Matcher[headerPattern.length];
				
				String[] headerParts = new String[headerPattern.length];
				
				// checks if the line has the header pattern by checking each header partition
				if (line.length() == HEADER_REGEX_END[HEADER_REGEX_END.length-1]) { // checkt ob die headerzeile die richtige länge hat
					for (int i = 0; i < headerPattern.length; i++) {
							int start = 0;
							if (i > 0) start = HEADER_REGEX_END[i-1];
							int end = HEADER_REGEX_END[i];
							
							// den aktuellen teil der headerzeile abschneiden
							if (start >= 0 && start < line.length() && end >= 0 && end <= line.length()) {
								headerParts[i] = line.substring(start, end);
							} else {
								console.printConsoleErrorLine("Es gab ein Problem mit den Teilen der Header Zeile!", 108);
								return 108;
							}
							
							// matcher erstellen
							m[i] = headerPattern[i].matcher(headerParts[i]);
			
							// nach muster suchen
							if (!m[i].find()) {
								found = false;
								break;
							}
					}
				} else {
					found = false;
				}
				
				lineCounter++;
				
				if (found) {
					if (startHeader == -1) {
						startHeader = lineCounter;
						foundHeader = true;
					}
					
					// überprüft ob genug Daten gefunden wurden
					if (m[1].groupCount() != 1 || m[3].groupCount() != 2 || m[4].groupCount() != 1) {
						console.printConsoleErrorLine("Es wurden nicht 4 Variablen in der Zeile gefunden! Zeile: " + lineCounter, 103);
						errorStatus = 103;
						return 103;
					}
					
					// load file Information
					fileInformation = m[1].group(1);
					
					OplTypeElement element = new OplTypeElement();
					
					// load typeName
					element.setName(m[3].group(2));
					
					// load type
					String type = m[4].group(1);
					
					// load type ID
					long id = 0;
					
					try {
						id = Long.parseLong(m[3].group(1));
					} catch (NumberFormatException nfe) {
						console.printConsoleErrorLine("Die ID konnte nicht richtig gelesen werden!; Zeile: " 
								+ lineCounter, 104);
						errorStatus = 104;
						nfe.printStackTrace();
						return 104;
					}
					element.setId(id);
					
					
					OplType typeObject = getType(type);
					
					if (typeObject == null) {
						typeObject = new OplType(type);
	
						types.put(typeObject.getType(), typeObject);
					}
					
					element.setType(typeObject);
					typeObject.addElement(element);
				} else if (foundHeader) {
					headerStart = startHeader;
					headerEnd = --lineCounter;
					
					console.printConsoleLine("Der Header wurde von Zeile " + headerStart 
							+ " bis Zeile " + headerEnd + " ausgelesen");
					
					break;
				}
			}
			
			br.close();
		} catch (IOException e) {
			console.printConsoleErrorLine("Es Gab ein Problem mit dem Lesen der Datei!", 101);
			errorStatus = 101;
			
			e.printStackTrace();
			
			return 101;
		}
		
		if (!foundHeader) {
			console.printConsoleErrorLine("Es wurde kein Headerblock gefunden!", 105);
			errorStatus = 105;
			return 105;
		}
		
		errorStatus = 0;
		return errorStatus;
	}

	public int checkErrorStatus() {
		return errorStatus;
	}
	
	public Collection<OplType> getTypes() {
		return (ArrayList<OplType>) types.values();
	}
	
	public HashMap<String, OplType> getHashMapTypes() {
		return types;
	}

	public String getFileInformation() {
		return fileInformation;
	}

	public File[] getOplFile() {
		return OplFiles;
	}

	public void setOplFile(File[] oplFiles) {
		this.OplFiles = oplFiles;
		errorStatus = 107;
	}

	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

	public long getHeaderEnd() {
		return headerEnd;
	}
	
	public long getHeaderStart() {
		return headerStart;
	}

	public int getConflictHandling() {
		return conflictHandling;
	}

	public void setConflictHandling(int conflictHandling) {
		if (conflictHandling < MIN_MAX_CONFLICT_HANDLING_TYPES[0] || conflictHandling > MIN_MAX_CONFLICT_HANDLING_TYPES[1]) {
			console.printConsoleError("Das angegebene Konflikt Verhalten existiert nicht!", 111);
			return;
		}
		this.conflictHandling = conflictHandling;
		this.errorStatus = 110;
	}
}