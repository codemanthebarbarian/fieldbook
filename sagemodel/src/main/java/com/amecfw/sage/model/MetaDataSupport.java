package com.amecfw.sage.model;

import java.util.List;

public interface MetaDataSupport<TmetaElement> extends Entity {
	public List<TmetaElement> getMetaData();
	public void setMetaData(List<TmetaElement> metaData);
	public boolean hasMetaData();
	public void resetMetaData();
}
