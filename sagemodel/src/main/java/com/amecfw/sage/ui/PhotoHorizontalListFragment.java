package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.proxy.PhotoProxy;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PhotoHorizontalListFragment extends Fragment {
	
	private static final String KEY_PROXIES = PhotoHorizontalListFragment.class.getName() + ".proxies";
	
	private LinearLayout listView;
	private List<PhotoProxy> proxies;
	private PhotoListAdapter adapter;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.photo_horizontal_fragment, container, false);
		listView = (LinearLayout) view.findViewById(android.R.id.list);
		if(savedInstanceState != null) initialize(savedInstanceState);
		initialize();
		//view.setOnLongClickListener(onLongClickListener);
		return view;
	}
	
	private void initialize(){
		if(proxies == null) proxies = new ArrayList<PhotoProxy>();
		adapter = new PhotoListAdapter(getActivity(), proxies);
		adapter.registerDataSetObserver(dataSetObserver);
		adapter.notifyDataSetChanged();
	}
	
	private void initialize(Bundle savedInstanceState){
		proxies = SageApplication.getInstance().removeItem(savedInstanceState.getString(KEY_PROXIES));
	}
	
	public void setProxies(List<PhotoProxy> proxies){
		if(this.proxies == null) this.proxies = new ArrayList<PhotoProxy>();
		else this.proxies.clear();
		if(proxies != null) this.proxies.addAll(proxies);
		if(adapter != null) adapter.setItems(this.proxies);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_PROXIES, KEY_PROXIES);
		SageApplication.getInstance().setItem(KEY_PROXIES, proxies);
	}
	
	private DataSetObserver dataSetObserver = new DataSetObserver(){
		@Override
		public void onChanged() { 
			if(adapter == null || listView == null) return;
			listView.removeAllViews();
			for(int i = 0 ; i < adapter.getCount() ; i++){
				View v = adapter.getView(i, null, listView);
				listView.addView(v);
			}
		}
		@Override
		public void onInvalidated() {
			if(listView == null) return;
			listView.removeAllViews();
		}
	};
	
}
