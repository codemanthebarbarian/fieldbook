package com.amecfw.sage.ui;


import java.util.List;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.persistence.DaoSession;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ProjectSiteList extends ListFragment {
	
	/** the key for filtering project site by metadata name (used in provisioning for specific components) */
	public static final String ARG_META_NAME = ProjectSiteManagement.EXTRA_META_NAME;
	/** the key for filtering projects by metadata value (used in provisioning for specific components)  */
	public static final String ARG_META_VALUE = ProjectSiteManagement.EXTRA_META_VALUE;
	
	private ProjectSiteArrayAdapter adapter;
	private ListView listView;
	private String metaName;
	private String metaValue;
	private List<ProjectSite> projectSite;
	
	public void setProjectSite (List<ProjectSite> projectSite){
		this.projectSite = projectSite;
		if(adapter != null){
			adapter.clear();
			adapter.addAll(getProjectSites());
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.simple_list_layout, container, false);
		listView = (ListView) view.findViewById(android.R.id.list);
		if(savedInstanceState != null) initialize(savedInstanceState);
		else initialize(getArguments());
		return view;
	}
	
	@Override
	public void onResume() {
		if(adapter == null){
			adapter = new ProjectSiteArrayAdapter(getActivity(), getProjectSites());
			listView.setAdapter(adapter);
		}else {
			if(listView.getAdapter() == null) listView.setAdapter(adapter);
			adapter.clear();
			adapter.addAll(getProjectSites());
			adapter.notifyDataSetChanged();
		}		
		super.onResume();
	}
	
	private List<ProjectSite> getProjectSites(){
		DaoSession session = SageApplication.getInstance().getDaoSession();
		ProjectSiteServices service = new ProjectSiteServices(session);
		if(metaName != null && metaValue != null) return service.findProjectSites(metaName, metaValue);
		else if(metaName != null) return service.findProjectSitesByMetaName(metaName);
		else if(metaValue != null) return service.findProjectSitesByMetaValue(metaValue);
		else return service.getProjectSites();
	}
	
	private void initialize(Bundle bundle){
		if(bundle == null) return;
		metaName = bundle.getString(ARG_META_NAME);
		metaValue = bundle.getString(ARG_META_VALUE);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(getActivity() instanceof OnItemSelectedHandler){ 
			if(l != null){
				ProjectSite ps = (ProjectSite) listView.getItemAtPosition(position);
				((OnItemSelectedHandler)getActivity()).onItemSelected(ps);
			}else ((OnItemSelectedHandler)getActivity()).onItemSelected(null);
		}
	}	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_META_NAME, metaName);
		outState.putString(ARG_META_VALUE, metaValue);
		super.onSaveInstanceState(outState);
	}

	/**
	 * An interface to be implemented by the activity for a callback when a list item is clicked. The
	 * ProjectSite clicked is returned from the list, a null can be returned if there was an issue getting
	 * the item selected.
	 */
	public interface OnItemSelectedHandler{
		public void onItemSelected(ProjectSite projectSite);
	}
}
