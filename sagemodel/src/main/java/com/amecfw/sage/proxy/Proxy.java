package com.amecfw.sage.proxy;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.Equatable;

public abstract class Proxy<TSageModel> implements  Equatable {
	protected TSageModel model;

	public TSageModel getModel() {
		return model;
	}

	public void setModel(TSageModel model) {
		this.model = model;
	}
	
	private EqualityComparator comparator;
	
	public EqualityComparator getComparator() {
		return comparator;
	}

	public void setComparator(EqualityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(comparator != null) return comparator.equalsTo(this, o);
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		if(comparator != null) return comparator.getHashCode(this);
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ getModel().hashCode();
		return result;
	}
	
}
