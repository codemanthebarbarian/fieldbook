package com.amecfw.sage.vegetation.elements;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.ElementGroup;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.util.OnItemSelectedHandler;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class GroupsListDialogFragment extends DialogFragment {
	
	private GroupsListArrayAdapter adapter;
	private List<ElementGroup> groups;
	private ListView list;
	
	/** the key for storing the groups in the application cache */
	private static final String KEY_ELEMENT_GROUPS = "sage.vegetation.GroupsListDialogFragment.groups";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.fieldbook.R.layout.simple_list_layout, container, false);
		if(savedInstanceState != null && savedInstanceState.containsKey(KEY_ELEMENT_GROUPS)){
			groups = SageApplication.getInstance().removeItem(KEY_ELEMENT_GROUPS);
		}
		initialize(view);
		return view;
	}
	
	private void initialize(View view){
		if(groups == null) groups = new ArrayList<ElementGroup>();
		adapter = new GroupsListArrayAdapter(getActivity(), groups);
		list = (ListView) view.findViewById(android.R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(itemClickListener);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		SageApplication.getInstance().setItem(KEY_ELEMENT_GROUPS, groups);
		outState.putString(KEY_ELEMENT_GROUPS, KEY_ELEMENT_GROUPS);
		super.onSaveInstanceState(outState);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(groupSelectedHandler != null) groupSelectedHandler.onItemSelected(adapter.getItem(position));			
		}		
	};
	
	public void setElementGroups(List<ElementGroup> groups){
		this.groups = groups;
		if(adapter != null){
			adapter.clear();
			adapter.addAll(this.groups);
			adapter.notifyDataSetChanged();
		}
	}
	
	private OnItemSelectedHandler<ElementGroup> groupSelectedHandler;
	public void setGroupSelectedHandler(OnItemSelectedHandler<ElementGroup> handler){
		groupSelectedHandler = handler;
	}
}
