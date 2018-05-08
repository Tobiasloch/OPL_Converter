package classes;

import java.util.ArrayList;

public class OplType {
	
	private String type;
	private ArrayList<OblTypeElement> elements;
	
	OplType(String type, OblTypeElement element) {
		this.type = type;
		elements = new ArrayList<OblTypeElement>();
		
		if (element != null) elements.add(element);
	}
	
	OplType(String type) {
		this(type, null);
	}
	
	OplType() {
		this("");
	}
	
	public void addElement(OblTypeElement element) {
		this.elements.add(element);
	}
	
	public void addElement() {
		addElement(new OblTypeElement());
	}
	
	public OblTypeElement getActiveElement() {
		return elements.get(elements.size()-1);
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<OblTypeElement> getElements() {
		return elements;
	}

	public void setElements(ArrayList<OblTypeElement> elements) {
		this.elements = elements;
	}
}