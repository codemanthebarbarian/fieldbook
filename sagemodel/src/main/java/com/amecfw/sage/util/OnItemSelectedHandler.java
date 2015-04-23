package com.amecfw.sage.util;

/**
 * a callback handler for when items are selected. Generally used in ListFragments allowing the hosting activity
 * to provided a handler to deal with items are selected in the list. 
 *
 * @param <T> the type of item selected
 */
public interface OnItemSelectedHandler<T> {
	/**
	 * the callback with the selected item (could be null)
	 * @param item the selected item
	 */
	public void onItemSelected(T item);
}
