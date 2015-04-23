package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.proxy.GroupObservationProxy;
import com.amecfw.sage.util.ErrorHandler;
import com.amecfw.sage.util.ActionEvent;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment for displaying Group Observations for a provided Observation group.
 * The group must be passed to the fragment through the arguments using the key constants 
 * in this class. The calling activity can implement com.amecfw.sage.util.ErrorHandler to 
 * receive any notifications of errors that cause a fragment failure and it will be the 
 * Responsibly of the calling activity to use the fragment manager to pop back to the previous state
 * and deal with the error (e.g. not providing a group or giving an invalid id or cache item).
 */
public class GroupObservationsList extends ListFragment implements ActionEvent.Listener {
	/** the key to used when putting proxies in SageApplication cache (easiest just use this as the cache key as well)	
	 * the proxies will be removed from the cache when retrieved must be List[ObservationGroupProxy] */
	private static final String ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY = "sage.ui.GroupObservationsList.proxies";
	
	private GroupObservationArrayAdapter adapter;
	private ListView listView;
	private TextView emptyList;
	private List<GroupObservationProxy> proxies;
	private long groupId;
	
	public List<GroupObservationProxy> getProxies(){
		return proxies;
	}
	
	public void setProxies(List<GroupObservationProxy> proxies){
		this.proxies = proxies == null ? new ArrayList<GroupObservationProxy>() : proxies;
		if(adapter != null){
			adapter.clear();
			adapter.addAll(this.proxies);
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.simple_list_layout, container, false);
		if(savedInstanceState != null){
			proxies = getProxies(savedInstanceState);
		}
		initilize(view);
		return view;
	}
	
	private List<GroupObservationProxy> getProxies(Bundle bundle){ 
		if(bundle == null || !bundle.containsKey(ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY)) return new ArrayList<GroupObservationProxy>();
		List<GroupObservationProxy> tmp = SageApplication.getInstance().removeItem(bundle.getString(ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY, ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY));
		return tmp == null ? new ArrayList<GroupObservationProxy>() : tmp;
	}
	
	private void initilize(View view){
		listView = (ListView) view.findViewById(android.R.id.list);
		emptyList = (TextView) view.findViewById(android.R.id.empty);
		if(adapter == null) adapter = new GroupObservationArrayAdapter(getActivity(), proxies);
		listView.setAdapter(adapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		SageApplication.getInstance().setItem(ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY, proxies);
		outState.putString(ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY, ARG_GROUP_OBSERVATION_PROXIES_CACHE_KEY);
		super.onSaveInstanceState(outState);
	}
	
	private void initializationError(String message){
		Log.e("sage.ui.GroupObservationsList", message);
		if(getActivity() instanceof ErrorHandler) ((ErrorHandler)getActivity()).onError(message, null, GroupObservationsList.class);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(onItemSelectedHandler != null && l.getId() == listView.getId()){
			GroupObservationProxy groupObservationProxy = (GroupObservationProxy) listView.getItemAtPosition(position);
			onItemSelectedHandler.onItemSelected(groupObservationProxy);
		}
	}
	
	public void setAdapter(GroupObservationArrayAdapter adapter){
		listView.setAdapter(adapter);
		if(adapter != null) adapter.notifyDataSetChanged();
	}
	
	public GroupObservationArrayAdapter getAdapter(){
		return adapter;
	}

	private OnItemSelectedHandler onItemSelectedHandler;
	public void setOnItemSelectedHandler(OnItemSelectedHandler handler){
		onItemSelectedHandler = handler;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getAction()){
		case ActionEvent.CANCEL: case ActionEvent.EXIT:
			break;
		case ActionEvent.DO_COMMAND:
			Bundle args = e.getArgs();
			if(args != null) onDoCommand(args.getInt(ActionEvent.ARG_COMMAND));
			break;
		}		
	}
	
	private void onDoCommand(int command){
		
	}


	/**
	 * An interface to be implemented by the activity for a callback when a list item is clicked. The
	 * GroupObservation clicked is returned from the list, a null can be returned if there was an issue getting
	 * the item selected.
	 */
	public interface OnItemSelectedHandler{
		public void onItemSelected(GroupObservationProxy groupObservationProxy);
	}
	
}
