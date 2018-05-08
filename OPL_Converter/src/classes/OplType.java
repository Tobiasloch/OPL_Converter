package classes;

public class OplType {
	
	private String type;
	private String typeName;
	private long typeID;
	
	OplType(String type, String typeName, long typeID) {
		this.type = type;
		this.typeName = typeName;
		this.typeID = typeID;
	}
	
	OplType() {
		this("", "", 0);
	}
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public long getTypeID() {
		return typeID;
	}
	public void setTypeID(long typeID) {
		this.typeID = typeID;
	}
	
}
