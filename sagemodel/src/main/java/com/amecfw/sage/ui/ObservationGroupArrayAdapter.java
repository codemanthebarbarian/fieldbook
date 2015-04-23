package com.amecfw.sage.ui;

import java.util.List;

import com.amecfw.sage.model.ObservationGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ObservationGroupArrayAdapter extends ArrayAdapter<ObservationGroup> {
	
	public ObservationGroupArrayAdapter(Context context, List<ObservationGroup> observationGroups){
		super(context, com.amecfw.sage.model.R.layout.observation_group_listitem, observationGroups);
	}
	
	static class ViewHolder {
		TextView groupName;
		TextView ownerName;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(com.amecfw.sage.model.R.layout.observation_group_listitem, parent, false);
			holder = new ViewHolder();
			holder.groupName = (TextView) convertView.findViewById(com.amecfw.sage.model.R.id.observationGroupListItem_groupName);
			holder.ownerName = (TextView) convertView.findViewById(com.amecfw.sage.model.R.id.observationGroupListItem_ownerName);
			convertView.setTag(holder);
		}else holder = (ViewHolder) convertView.getTag();
		ObservationGroup current = getItem(position);
		if(current.getName() == null) holder.groupName.setText("NULL");
		else holder.groupName.setText(current.getName());
		if(current.getOwner() == null || current.getOwner().getName() == null) holder.ownerName.setText("NULL");
		else holder.ownerName.setText(current.getOwner().getName());
		return convertView;
	}

}
