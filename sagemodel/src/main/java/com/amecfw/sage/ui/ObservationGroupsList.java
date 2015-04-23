package com.amecfw.sage.ui;

import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.ObservationService;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ObservationGroupsList extends ListFragment {
	
	/** The key for passing the owner as parcelable (must be Owner object) */
	public static String EXTRA_OWNER = GroupObservationManagement.EXTRA_OWNER;

	private ObservationGroupArrayAdapter adapter;
	private ListView listView;
	private Owner owner;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.simple_list_layout, container, false);
		listView = (ListView) view.findViewById(android.R.id.list);
		if(savedInstanceState == null) getOwner(getArguments());
		else getOwner(savedInstanceState);
		return view;
	}
	
	@Override
	public void onResume(){
		ObservationService service = new ObservationService( SageApplication.getInstance().getDaoSession());
		if(adapter == null){
			adapter = new ObservationGroupArrayAdapter(getActivity(), owner == null ? service.getAllGroups() : service.findGroups(owner));
		}else{
			listView.setAdapter(adapter);
			adapter.clear();
			adapter.addAll(owner == null ? service.getAllGroups() : service.findGroups(owner));
			adapter.notifyDataSetChanged();
		}
		super.onResume();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(owner != null) outState.putParcelable(EXTRA_OWNER, owner);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		if(l != null){
			ObservationGroup og = (ObservationGroup) listView.getItemAtPosition(position);
			if(onItemSelectedHandler == null && getActivity() instanceof OnItemSelectedHandler) onItemSelectedHandler = (OnItemSelectedHandler) getActivity();
			if(onItemSelectedHandler != null) onItemSelectedHandler.OnItemSelected(og);
		}
	}
	
	private void getOwner(Bundle bundle){
		
		if (bundle != null && bundle.containsKey(EXTRA_OWNER))
			owner = bundle.getParcelable(EXTRA_OWNER);
	}
	
	private OnItemSelectedHandler onItemSelectedHandler;
	
	public void setOnItemSelectedHandler(OnItemSelectedHandler handler){
		onItemSelectedHandler = handler;
	}
	
	/**
	 * Interface for handling what happens when a user clicks a listed observation group
	 */
	public interface OnItemSelectedHandler {
		public void OnItemSelected(ObservationGroup observationGroup);
	}
	
}
