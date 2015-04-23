package com.amecfw.sage.model;

import com.amecfw.sage.model.Owner;

public interface Ownership {
	public void setOwner(Owner owner);
	public Owner getOwner();
	public boolean hasOwner();
}
