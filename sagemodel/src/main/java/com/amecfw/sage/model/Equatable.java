package com.amecfw.sage.model;

public interface Equatable {
	public EqualityComparator getComparator();
	public void  setComparator(EqualityComparator comparator);
}
