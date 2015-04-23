package com.amecfw.sage.vegetation.elements;

import java.util.List;
import com.amecfw.sage.model.ElementGroup;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GroupsListArrayAdapter extends ArrayAdapter<ElementGroup> {

	public GroupsListArrayAdapter(Context context, List<ElementGroup> groups){
		super(context, android.R.layout.simple_list_item_1, groups);
	}
	
	private static class ViewHolder{
		public TextView groupName;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder();
			holder.groupName = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		ElementGroup group = getItem(position);
		if(group != null) holder.groupName.setText(group.getName());
		return convertView;
	}
}
