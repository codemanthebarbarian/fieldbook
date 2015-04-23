package com.amecfw.sage.ui;

import java.util.List;
import java.util.regex.Pattern;

import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.util.CollectionOperations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class ObservationTypeArrayAdapter extends ArrayAdapter<ObservationType> {
	
	private List<ObservationType> observationTypes;
	
	private boolean isFiltered;
	
	public ObservationTypeArrayAdapter(Context context, List<ObservationType> observationTypes){
		super(context, android.R.layout.simple_list_item_1, observationTypes);
		this.observationTypes = observationTypes;
		isFiltered = false;
	}
	
	public void reset(){
		if(isFiltered){
			clear();
			addAll(observationTypes);
			notifyDataSetChanged();
		}
	}
	
	static class ViewHolder{
		TextView name;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(android.R.layout.simple_list_item_1, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		}else holder = (ViewHolder) convertView.getTag();
		ObservationType current = getItem(position);
		holder.name.setText(current.getName());
		return convertView;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}
	
	private Filter filter = new Filter(){
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			ObservationType ot = new ObservationType();
			ot.setName(constraint.toString());
			FilterResults results = new FilterResults();
			results.values = CollectionOperations.intersection(observationTypes
					, "getName"
					, Pattern.compile(String.format("\b%s", constraint), Pattern.CASE_INSENSITIVE));
			return results;
		}
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			clear();
			addAll((List<ObservationType>) results.values);
			notifyDataSetChanged();
			isFiltered = true;
		}		
	};
}
