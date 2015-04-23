package com.amecfw.sage.model;

public interface EqualityComparator {
	public boolean equalsTo(Object objA, Object objB);
	public int getHashCode(Object obj);
}
