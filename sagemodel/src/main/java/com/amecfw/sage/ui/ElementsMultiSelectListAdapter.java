package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.util.SparseBooleanArray;

public class ElementsMultiSelectListAdapter extends ListAdapter<Element> implements Filterable {
	
	public static final int DISPLAY_SCODE_SCIENTIFIC = 1;
	public static final int DISPLAY_SCODE_COMMON = 2;
	public static final int DISPLAY_SCIENTIFIC_COMMON = 3;
	public static final int DISPLAY_COMMON_SCIENTIFIC = 4;
	
	private int displayMode = 1;
	private SparseBooleanArray checkedItems;

	public ElementsMultiSelectListAdapter(Context context, List<Element> elements, int displayMode){
		super(context, elements);
		if(displayMode < 1 || displayMode > 4) this.displayMode = 1;
		else this.displayMode = displayMode;
		checkedItems = new SparseBooleanArray();
		filter = new ElementFilter();
		original = new ArrayList<Element>(elements);
	}
	
	/**
	 * gets the checked items using the adapters own SparseBooleanArray
	 * @return the items from the elements that are checked or an empty list is none are checked
	 */
	public List<Element> getCheckedItems(){
		return getCheckedItems(checkedItems);
	}
	
	/**
	 * gets the checked items using a provided SparseBooleanArray
	 * @param checkedItems - make sure to use the sparsebooleanArray from the list the adapter is bound to
	 * @return the items from the elements that are checked or an empty list is none are checked
	 */
	public List<Element> getCheckedItems(SparseBooleanArray checkedItems){
		List<Element> results = new ArrayList<Element>(checkedItems.size());
		for(int i = 0 ; i < checkedItems.size() ; i++){
			if(checkedItems.valueAt(i)) results.add(get(checkedItems.keyAt(i)));
		}
		return results;
	}
	
	@Override
	public void setItems(List<Element> items) {
		checkedItems = new SparseBooleanArray();
		super.setItems(items);
		original.clear();
		original.addAll(items);
	}

	public void setDisplayMode(int displayMode){
		if(displayMode < 1 || displayMode > 4) this.displayMode = 1;
		else this.displayMode = displayMode;
	}
	
	private static class ViewHolder{
		public TextView text1;
		public TextView text2;
		public CheckBox checkBox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(com.amecfw.sage.model.R.layout.simple_list_item_2_multiple_choice, parent, false);
			convertView.setOnClickListener(clickListener);
			viewHolder = new ViewHolder();
			viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
			viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
			convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder, viewHolder);
		} else viewHolder = (ViewHolder) convertView.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
		Element item = get(position);
		setHolder(viewHolder, item);
		viewHolder.checkBox.setChecked(checkedItems.get(position, false));
		convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_position, position);
		return convertView;
	}
	
	private void setHolder(ViewHolder holder, Element item){
		switch(displayMode){
		case DISPLAY_SCODE_SCIENTIFIC:
			setText(holder, item.getScode(), item.getScientificName());
			break;
		case DISPLAY_SCODE_COMMON:
			setText(holder, item.getScode(), item.getCommonName());
			break;
		case DISPLAY_SCIENTIFIC_COMMON:
			setText(holder, item.getScientificName(), item.getCommonName());
			break;
		case DISPLAY_COMMON_SCIENTIFIC:
			setText(holder, item.getCommonName(), item.getScientificName());
			break;
		}
	}
	
	private OnClickListener clickListener =  new OnClickListener(){
		@Override
		public void onClick(View v) {
			int position = Convert.getTagAs(v, com.amecfw.sage.model.R.id.sage_tag_list_position, -1);
			boolean isChecked = checkedItems.get(position, false);
			checkedItems.put(position, !isChecked);
			ViewHolder viewHolder = (ViewHolder) v.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
			viewHolder.checkBox.setChecked(!isChecked);
			if(onItemClickListener != null) onItemClickListener.onItemClick(null, v, position, getItemId(position));
		}
	};

	private AdapterView.OnItemClickListener onItemClickListener;
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) { onItemClickListener = listener; }
	
	private void setText(ViewHolder holder, String text1, String text2){
		holder.text1.setText(text1);
		holder.text2.setText(text2);
	}
	
	// Filter code

	@Override
	public Filter getFilter() {
		return filter;
	}
	
	private ElementFilter filter;
	ArrayList<Element> original;

	private class ElementFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults retVal = new FilterResults();
			ArrayList<Element> results = new ArrayList<Element>();
			if (constraint != null) {
				if (original != null && original.size() > 0) {
					// check SCODE, scientific name and common name
					switch (displayMode){
						case DISPLAY_SCIENTIFIC_COMMON:
							for (Element element : original) {
								if (matches(element.getScientificName(), constraint)
										|| matches(element.getCommonName(), constraint))
									results.add(element);
							}
							break;
						case DISPLAY_COMMON_SCIENTIFIC:
							for (Element element : original) {
								if (matches(element.getCommonName(), constraint)
										|| matches(element.getScientificName(), constraint))
									results.add(element);
							}
							break;
						case DISPLAY_SCODE_SCIENTIFIC:
							for (Element element : original) {
								if (matches(element.getScode(), constraint)
										|| matches(element.getScientificName(), constraint))
									results.add(element);
							}
							break;
						case DISPLAY_SCODE_COMMON:
							for (Element element : original) {
								if (matches(element.getScode(), constraint)
										|| matches(element.getCommonName(), constraint))
									results.add(element);
							}
							break;
						default:
							for (Element element : original) {
								if (matches(element.getScode(), constraint)
										|| matches(element.getScientificName(), constraint)
										|| matches(element.getCommonName(), constraint))
									results.add(element);
							}
					}
				}
			}
			retVal.values = results;
			retVal.count = results.size();
			return retVal;
		}

		private Boolean matches(String code, CharSequence value) {
			if (code.toLowerCase(Locale.getDefault()).contains(value.toString().toLowerCase(Locale.getDefault())))
				return true;

			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			items.clear();
			items.addAll((ArrayList<Element>) results.values);
			notifyDataSetChanged();
		}
	}
	
}
