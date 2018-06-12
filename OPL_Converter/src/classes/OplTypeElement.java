package classes;

public class OplTypeElement implements Comparable<OplTypeElement> {
	private String name;
	private long id;
	private long value;
	private OplType type;
	
	private int order;
	
	OplTypeElement(String name, long id, int order) {
		this.name = name;
		this.id = id;
	}
	
	OplTypeElement() {
		this("", 0, -1);
	}
	
	public boolean equals(OplTypeElement element) {
		if (name.equals(element.name) && id == element.id && value == element.value) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		int code =  name.hashCode() + (int)id + (int)value;
		
		return code;
	}
	
	@Override
	public int compareTo(OplTypeElement element) {
		return getOrder() - ((OplTypeElement) element).getOrder();
	}
	
	@Override
	public String toString() {
		return name;
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

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}