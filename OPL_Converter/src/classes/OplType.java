package classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class OplType {
	
	private String type;
	private HashMap<String, OplTypeElement> elements;
	private OplTypeElement activeObject;
	
	OplType(String type, OplTypeElement element) {
		this.type = type;
		elements = new HashMap<String, OplTypeElement>();
		
		if (element != null) addElement(element);
	}
	
	OplType(String type) {
		this(type, null);
	}
	
	OplType() {
		this("");
	}
	
	public boolean equals(OplType type) {
		if (type.equals(type)) {
			if (elements.equals(type.elements)) return true;
		}
		
		return false;
	}
	
	public void addElement(OplTypeElement element) {
		this.elements.put(element.getName(), element);
		activeObject = element;
	}
	
	public void addElement() {
		addElement(new OplTypeElement());
	}
	
	public OplTypeElement getActiveElement() {
		return activeObject;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public OplTypeElement getElement(String key) {
		return elements.get(key);
	}
	
	public Collection<OplTypeElement> getElements() {
		return elements.values();
	}
	
	public HashMap<String, OplTypeElement> getHash() {
		return elements;
	}
}