package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.OnItemSelectedHandler;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class ElementsListDialogFragment extends DialogFragment implements ActionEvent.Listener {
	
	public static final String ARG_CHOICE_MODE = "sage.vegetation.rareplant.ElementsListDialogFragment.choiceMode";
	
	private List<Element> elements;
	private ListView list;
	private ElementsListAdapter adapter;
	private int choiceMode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.simple_list_layout, container, false);
		initialize(view, savedInstanceState == null ? getArguments() : savedInstanceState);
		return view;
	}
	
	private void initialize(View view, Bundle args){
		if(args != null){
			choiceMode = args.getInt(ARG_CHOICE_MODE, AbsListView.CHOICE_MODE_MULTIPLE);
		}
		if(elements == null) elements = new ArrayList<Element>();
		adapter = new ElementsListAdapter(getActivity(), elements, ElementsListAdapter.DISPLAY_COMMON_SCIENTIFIC);
		list = (ListView) view.findViewById(android.R.id.list);
		list.setChoiceMode(choiceMode);
		list.setAdapter(adapter);
		list.setOnItemClickListener(itemClickListener);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) { 
			view.callOnClick();
			if(externalItemClickListener != null) externalItemClickListener.onItemClick(parent, view, position, id); 
			if(onItemSelectedHandler != null) onItemSelectedHandler.onItemSelected(adapter.get(position));
		}
	};
	private OnItemClickListener externalItemClickListener;
	public void setOnItemClickListener(OnItemClickListener listener){
		externalItemClickListener = listener;
	}
	
	public void setElements(List<Element> elements){
		this.elements = (elements == null ? new ArrayList<Element>() : elements);
		if(adapter != null) adapter.setItems(this.elements);
	}
	
	private OnItemSelectedHandler<Element> onItemSelectedHandler;
	public void setOnItemSelectedHandler(OnItemSelectedHandler<Element> handler){
		onItemSelectedHandler = handler;
	}
	
	public void setChoiceMode(int choiceMode){
		this.choiceMode = choiceMode;
		if(list != null) list.setChoiceMode(this.choiceMode);
	}
	
	public SparseBooleanArray getCheckedItemPositions(){
		if(list.getChoiceMode() != AbsListView.CHOICE_MODE_NONE) return list.getCheckedItemPositions();
		return null;
	}
	
	public List<Element> getCheckedItems(){
		if(list == null || adapter == null) return null;
		return adapter.getCheckedItems();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getAction()){
		case ActionEvent.SAVE:
			
			break;
		}		
	}
	
	public SearchView.OnQueryTextListener getOnQueryTextListener(){ return queryListener; }
	SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {		
		@Override
		public boolean onQueryTextSubmit(String query) {
			if(adapter != null){
				adapter.getFilter().filter(query);
				return true;
			}
			return false;
		}		
		@Override
		public boolean onQueryTextChange(String newText) {
			if(adapter != null){
				adapter.getFilter().filter(newText);
				return true;
			}
			return false;
		}
	};
	
	
}
