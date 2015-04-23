package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amecfw.sage.model.Owner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class OwnerCreateExpandableAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private int groupViewResourceID;
	private int childViewResourceID;
	private List<String> groups;
	private HashMap<String, List<Owner>> children;
	
	public OwnerCreateExpandableAdapter(Context context, List<Owner> owners){
		this.context = context;
		buildItems(owners);
		groupViewResourceID = childViewResourceID = android.R.layout.simple_expandable_list_item_1;
	}
	
	private void buildItems(List<Owner> owners){
		groups = new ArrayList<String>();
		children = new HashMap<String, List<Owner>>();
		if(owners == null) return;
		for(Owner owner: owners){
			if(! groups.contains(owner.getType())){
				groups.add(owner.getType());
				children.put(owner.getType(), new ArrayList<Owner>());
			}
			children.get(owner.getType()).add(owner);
		}
	}
	
	/**
	 * put a replacement set of owners
	 * does not automatically call notifyDataSetChanged
	 * @param owners
	 */
	public void setOwners(List<Owner> owners){
		buildItems(owners);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return children.get(groups.get(groupPosition)).size();
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return children.get(groups.get(groupPosition)).get(childPosition);
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	private static class ViewHolder {
		public TextView name;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(groupViewResourceID, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		holder.name.setText(groups.get(groupPosition));
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(childViewResourceID, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		holder.name.setText(children.get(groups.get(groupPosition)).get(childPosition).getName());
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
