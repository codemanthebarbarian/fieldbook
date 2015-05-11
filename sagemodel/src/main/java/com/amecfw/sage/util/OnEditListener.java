package com.amecfw.sage.util;

/**
 * An interface to communicate the state of a mutable object
 */
public interface OnEditListener<T> {

	/**
	 * raised the first time an object has been mutated and placed in a dirty state
	 */
	public void onDirty();

	/**
	 * raised when the object's state has been persisted (no longer dirty)
	 */
	public void onSave();
}
