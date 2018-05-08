package classes;

public class OblTypeElement {
	private String name;
	private long id;
	private OplType type;
	
	OblTypeElement(String name, long id) {
		this.name = name;
		this.id = id;
	}
	
	OblTypeElement() {
		this("", 0);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public OplType getType() {
		return type;
	}

	public void setType(OplType type) {
		this.type = type;
	}
}