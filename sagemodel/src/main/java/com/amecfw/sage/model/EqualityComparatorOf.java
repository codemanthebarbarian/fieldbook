package com.amecfw.sage.model;

public interface EqualityComparatorOf<T> extends EqualityComparator {
	public boolean equals(T a, T b);
	
	public int getHash(T obj);
}
