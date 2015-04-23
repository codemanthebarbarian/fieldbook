package com.amecfw.sage.util;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ListAdapter<T> extends BaseAdapter {

	protected List<T> items;
	protected Context context;
	
	/**
	 * Create a new ListAdapter using the provided List of items as the backing source.
	 * If the list is mutated (items added, deleted, etc), you must call notifyDataSetChanged
	 * @param context
	 * @param items
	 */
	public ListAdapter(Context context, List<T> items){
		this.context = context;
		this.items = items;
	}
	
	/**
	 * Sets the list of items as the backing source. Calls notifyDataSetChanged
	 * @param items
	 */
	public void setItems(List<T> items){ 
		this.items = items; 
		notifyDataSetChanged();
	}
	
	public List<T> getItems() { return items; }
	
	@Override
	public int getCount() { return items.size(); }

	@Override
	public Object getItem(int position) {
		return get(position);
	}
	
	public T get(int position) { return items.get(position); }

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
