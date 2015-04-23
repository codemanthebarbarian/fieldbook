package com.amecfw.sage.ui;

import java.util.List;

import com.amecfw.sage.proxy.GroupObservationProxy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GroupObservationArrayAdapter extends ArrayAdapter<GroupObservationProxy> {

	public GroupObservationArrayAdapter(Context context, List<GroupObservationProxy> groupObservationProxies){
		super(context, android.R.layout.simple_list_item_2, groupObservationProxies);
	}

	static class ViewHolder{
		TextView observationTypeName;
		TextView allowableValues;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(android.R.layout.simple_list_item_2, parent, false);
			holder = new ViewHolder();
			holder.observationTypeName = (TextView) convertView.findViewById(android.R.id.text1);
			holder.allowableValues = (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		GroupObservationProxy current = getItem(position);
		if(current.getObservationType() == null || current.getObservationType().getName() == null) holder.observationTypeName.setText("NULL");
		else holder.observationTypeName.setText(current.getObservationType().getName());
		if(current.getModel() == null || current.getModel().getAllowableValues() == null) holder.allowableValues.setText("No Allowable Values");
		else holder.allowableValues.setText(current.getModel().getAllowableValues());
		return convertView;
	}
}
