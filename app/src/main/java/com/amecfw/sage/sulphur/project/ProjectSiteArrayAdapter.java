package com.amecfw.sage.sulphur.project;

import java.util.List;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.fieldbook.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProjectSiteArrayAdapter extends ArrayAdapter<ProjectSite> {

	public ProjectSiteArrayAdapter(Context context, List<ProjectSite> projectSites) {
		super(context, R.layout.project_site_list_item, projectSites);
	}
	
	static class ViewHolder{
		TextView projectNumber;
		TextView projectName;
		TextView siteName;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(convertView == null){
			convertView = layout.inflate(R.layout.project_site_list_item, parent, false);
			holder = new ViewHolder();
			holder.projectNumber = (TextView) convertView.findViewById(R.id.projectsitelistitem_projectnumber);
			holder.projectName = (TextView) convertView.findViewById(R.id.projectsitelistitem_projectname);
			holder.siteName = (TextView) convertView.findViewById(R.id.projectsitelistitem_sitename);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		ProjectSite current = getItem(position);
		if(current.getProject() == null) holder.projectNumber.setText("NULL");
		else holder.projectNumber.setText(String.format("Project Number: %s", current.getProject().getProjectNumber()));
		if(current.getProject().getName() == null) holder.projectName.setText("NULL");
		else holder.projectName.setText(String.format("Project Name: %s", current.getProject().getName()));
		if(current.getSite().getName() == null) holder.siteName.setText("NULL");
		else holder.siteName.setText(String.format("Site Name: %s", current.getSite().getName()));
		return convertView;
	}
	
}
