package com.amecfw.sage.proxy;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.Equatable;

public abstract class ViewModelBaseEquatable implements ViewModel, Equatable {

	protected EqualityComparator comparator;

	public EqualityComparator getComparator() {
		return comparator;
	}

	public void setComparator(EqualityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public boolean equals(Object o) {
		return comparator == null ? super.equals(o) : comparator.equalsTo(this, o);
	}

	@Override
	public int hashCode() {
		return comparator == null ? super.hashCode() : comparator.getHashCode(this);
	}
	
	
}
