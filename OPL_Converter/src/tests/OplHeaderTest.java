package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import classes.OplTypeElement;
import classes.OplHeader;
import classes.OplType;

class OplHeaderTest {
	
	/*
	 *	WEitere Tests müssen noch durchgeführt werden 
	 */
	
	
	@Test
	void headerFound() {
		OplHeader header = new OplHeader(new File("C:\\Users\\T.loch\\Desktop\\OPL Dateien Konvertieren\\22_325_2017.09.27_07-00_2017.09.27_13-00.opl"));
		
		assertTrue("Es gab eine Fehlermeldung.", (header.extractHeaderInformation() == 0));
		assertTrue("es wurden keine types gespeichert", (header.getTypes().size() > 0));
		
		
		for (OplType item : header.getTypes()) {
			System.out.println(item.getType() + "  size: " + item.getElements().size());
			for (OplTypeElement elem : item.getElements()) {
				System.out.println(item.getType() + "               ;" + elem.getName());
			}
		}
		System.out.println("size: " + header.getTypes().size());
	}

}
