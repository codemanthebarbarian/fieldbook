package com.amecfw.sage.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.model.service.ProjectSiteServices;

public class SiteSelectDialogFragment extends DialogFragment implements OnItemClickListener {
	
	private SiteArrayAdapter adapter;
	private DialogListener listener;
	private ListView listView;
	
	public void setDialogListener(DialogListener listener){
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.simple_list_layout, container, false);
		listView = (ListView) v.findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		return v;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		ProjectSiteServices service = new ProjectSiteServices(SageApplication.getInstance().getDaoSession());
		adapter = new SiteArrayAdapter(activity, service.getSites());
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(listener != null) listener.onDialogCancel();
		super.onCancel(dialog);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(listener != null) listener.onDialogClick(adapter.getItem(position));		
	}

	/**
	 * an interface to communicate with the dialog fragment
	 */
	public interface DialogListener {
		/**
		 * raised when the user selected a project.
		 * @param project the selected project
		 */
		public void onDialogClick(Site site);
		/**
		 * raised when the user canceled the dialog (clicked back button)
		 */
		public void onDialogCancel();
	}
	
}
