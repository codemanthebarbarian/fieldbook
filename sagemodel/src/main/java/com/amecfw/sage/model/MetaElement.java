package com.amecfw.sage.model;

public interface MetaElement extends Entity, UUIDSupport {
	public void setParentID(long parentID);
	public long getParentID();
	public String getName();
	public void setName(String name);
	public String getValue();
	public void setValue(String value);
}
