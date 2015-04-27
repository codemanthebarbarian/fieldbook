package com.amecfw.sage.sulphur.project;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.sulphur.Constants;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.sulphur.SulphurMainActivity;
import com.amecfw.sage.sulphur.sample.SampleList;
import com.amecfw.sage.util.ApplicationCache;
import com.amecfw.sage.util.ViewState;

import android.os.Bundle;
import android.app.ListFragment;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ProjectList extends ListFragment {
	
	ProjectSiteArrayAdapter adapter;
	ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		((SulphurMainActivity) getActivity()).setActionBarTitle("Projects");
		((SulphurMainActivity) getActivity()).setActionBarIcon(R.drawable.sulphur);
		View view = inflater.inflate(R.layout.sulphur_project_list, container, false);
		listView = (ListView) view.findViewById(android.R.id.list);
		return view;
	}

	@Override
	public void onResume() {
		DaoSession session = SageApplication.getInstance().getDaoSession();
		Services service = new Services(session);
		if(adapter == null){
			adapter = new ProjectSiteArrayAdapter(getActivity(), service.getProjectSites());
			listView.setAdapter(adapter);
		}else {
			adapter.clear();
			adapter.addAll(service.getProjectSites());
			adapter.notifyDataSetChanged();
		}		
		super.onResume();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(l != null){
			ProjectSite ps = (ProjectSite) listView.getItemAtPosition(position);
			SageApplication.getInstance().setItem(Constants.SULPHUR_PROJECTSITE_CACHE_KEY, ps);
			Intent intent = new Intent(getActivity(), SampleList.class);
			intent.putExtra(SampleList.PROJECT_SITE_KEY, ps.getId());
			startActivity(intent);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.sulphur_project_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();		
		if(id == R.id.menuNewProject){
			Intent intent = new Intent(getActivity(), Create.class);
			intent.putExtra(Create.VIEW_STATE_EXTRA, ViewState.getViewStateAdd());
			getActivity().startActivity(intent);
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}
	}
}
