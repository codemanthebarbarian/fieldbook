package com.amecfw.sage.ui;

import java.util.List;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.util.Validation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProjectArrayAdapter extends ArrayAdapter<Project> {

	public ProjectArrayAdapter(Context context, List<Project> projects){
		super(context, R.layout.project_site_list_item, projects);
	}
	
	static class ViewHolder{
		TextView projectNumber;
		TextView projectName;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layout.inflate(R.layout.project_list_item,  parent, false);
			holder = new ViewHolder();
			holder.projectNumber = (TextView) convertView.findViewById(R.id.projectListView_projectNumber);
			holder.projectName = (TextView) convertView.findViewById(R.id.projectListView_projectName);
			convertView.setTag(holder);
		}else holder = (ViewHolder) convertView.getTag();
		Project current = getItem(position);
		if(! Validation.isNullOrEmpty(current.getProjectNumber())) holder.projectNumber.setText(current.getProjectNumber());
		if(! Validation.isNullOrEmpty(current.getName())) holder.projectName.setText(current.getName());
		return convertView;
	}
	
}
