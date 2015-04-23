package com.amecfw.sage.vegetation.rareplant;

import java.util.List;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.util.Convert;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StationListAdapter extends ArrayAdapter<Station> {

	public StationListAdapter(Context context, List<Station> stations){
		super(context, android.R.layout.simple_list_item_2, stations);
	}
	
	private static class ViewHolder{
		public TextView stationName;
		public TextView stationData;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(android.R.layout.simple_list_item_2, null);
			holder = new ViewHolder();
			holder.stationName = (TextView) convertView.findViewById(android.R.id.text1);
			holder.stationData	= (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		Station item = getItem(position);
		if(item != null){
			holder.stationName.setText(item.getName());
			holder.stationData.setText(String.format("Date Collected: %s", Convert.dateToLongString(item.getSurveyDate(), "n/a")));
		}
		return convertView;
	}
	
	
	
}
