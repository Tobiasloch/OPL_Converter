package classes;

import java.util.ArrayList;

public class OplType {
	
	private String type;
	private ArrayList<OplTypeElement> elements;
	
	OplType(String type, OplTypeElement element) {
		this.type = type;
		elements = new ArrayList<OplTypeElement>();
		
		if (element != null) elements.add(element);
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
		this.elements.add(element);
	}
	
	public void addElement() {
		addElement(new OplTypeElement());
	}
	
	public OplTypeElement getActiveElement() {
		return elements.get(elements.size()-1);
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<OplTypeElement> getElements() {
		return elements;
	}

	public void setElements(ArrayList<OplTypeElement> elements) {
		this.elements = elements;
	}
}