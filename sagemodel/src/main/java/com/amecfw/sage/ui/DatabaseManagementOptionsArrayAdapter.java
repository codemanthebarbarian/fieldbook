package com.amecfw.sage.ui;

import java.util.List;

import com.amecfw.sage.model.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DatabaseManagementOptionsArrayAdapter extends ArrayAdapter<DatabaseManagementOptionsArrayAdapter.DatabaseOption> {
	
	
	public DatabaseManagementOptionsArrayAdapter(Context context, List<DatabaseManagementOptionsArrayAdapter.DatabaseOption> options){
		super(context, R.layout.database_management_item, options);
	}
	
	static class ViewHolder{
		TextView option;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(android.R.layout.simple_list_item_1, parent, false);
			holder = new ViewHolder();
			holder.option = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		}else holder = (ViewHolder) convertView.getTag();
		DatabaseOption current = getItem(position);
		holder.option.setText(current.getName());
		if(current.getIcon() != null) holder.option.setCompoundDrawablesRelativeWithIntrinsicBounds(current.icon, null, null, null);
		return convertView;
	}

	public static class DatabaseOption{
		
		private int code;
		private String name;
		private Drawable icon;
		
		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Drawable getIcon() {
			return icon;
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}		
	}

}
