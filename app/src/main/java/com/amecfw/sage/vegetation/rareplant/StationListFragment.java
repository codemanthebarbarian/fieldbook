package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class StationListFragment extends ListFragment {
	
	private static final String ARG_STATIONS_CACHE_KEY = "sage.vegetation.rareplant.StationListFragment.stations";

	private StationListAdapter adapter;
	private ListView list;
	private List<Station> stations;
	
	void setStations(List<Station> stations){
		this.stations = stations;
		if(adapter != null){
			adapter.clear();
			adapter.addAll(this.stations);
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_list_layout, container, false);
		list = (ListView) view.findViewById(android.R.id.list);
		if(savedInstanceState != null) stations = SageApplication.getInstance().removeItem(ARG_STATIONS_CACHE_KEY);
		setAdapter();
		list.setOnItemClickListener(onItemClickListener);
		list.setOnItemLongClickListener(onItemLongClickListener);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(stations != null){
			SageApplication.getInstance().setItem(ARG_STATIONS_CACHE_KEY, stations);
			outState.putString(ARG_STATIONS_CACHE_KEY, ARG_STATIONS_CACHE_KEY);
		}
		super.onSaveInstanceState(outState);
	}	
	
	private void setAdapter(){
		if(stations == null) stations = new ArrayList<Station>();
		adapter = new StationListAdapter(getActivity(), stations);
		list.setAdapter(adapter);
	}
	
	private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if(stationLongClickSelectedHandler != null){
				Station s = adapter.getItem(position);
				stationLongClickSelectedHandler.onItemSelected(s);
				return true;
			}
			return false;
		}
	};
	private com.amecfw.sage.util.OnItemSelectedHandler<Station> stationLongClickSelectedHandler;
	public void setStationLongClickSelectedHandler( com.amecfw.sage.util.OnItemSelectedHandler<Station> handler){
		stationLongClickSelectedHandler = handler;
	}
	
	private OnItemClickListener onItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Station station = adapter.getItem(position);
			if(stationSelectedHandler != null) stationSelectedHandler.onItemSelected(station);
		}		
	};
	
	private com.amecfw.sage.util.OnItemSelectedHandler<Station> stationSelectedHandler;
	public void setStationSelectedHandler(com.amecfw.sage.util.OnItemSelectedHandler<Station> handler){
		stationSelectedHandler = handler;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(getActivity() instanceof OnItemSelectedHandler){ 
			if(l != null){
				Station station = (Station) list.getItemAtPosition(position);
				((OnItemSelectedHandler)getActivity()).onItemSelected(station);
			}else ((OnItemSelectedHandler)getActivity()).onItemSelected(null);
		}
	}	
	
	public interface OnItemSelectedHandler{
		public void onItemSelected(Station station);
	}
	
}
