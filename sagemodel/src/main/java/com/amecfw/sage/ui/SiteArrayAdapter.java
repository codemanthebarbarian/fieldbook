package com.amecfw.sage.ui;

import java.util.List;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.util.Validation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SiteArrayAdapter extends ArrayAdapter<Site> {
	
	public SiteArrayAdapter(Context context, List<Site> sites){
		super(context, R.layout.site_list_item, sites);
	}
	
	static class ViewHolder{
		TextView siteName;
		TextView rootName;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(R.layout.site_list_item, parent, false);
			holder = new ViewHolder();
			holder.rootName = (TextView) convertView.findViewById(R.id.siteListItem_rootName);
			holder.siteName = (TextView) convertView.findViewById(R.id.siteListItem_siteName);
			convertView.setTag(holder);
		} else holder = (ViewHolder) convertView.getTag();
		Site current = getItem(position);
		if(! Validation.isNullOrEmpty(current.getName())) holder.siteName.setText(current.getName());
		if(current.getRoot() != null && ! Validation.isNullOrEmpty(current.getRoot().getName())) holder.rootName.setText(current.getRoot().getName());
		return convertView;
	}

}
