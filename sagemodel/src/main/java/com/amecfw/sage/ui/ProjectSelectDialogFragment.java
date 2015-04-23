package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.ProjectSiteServices;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A DialogFragment used to select existing projects. The activity/fragment initializing the dialog 
 * must set the ProjectSelectDialogFragment.DialogListener to get the project that
 * was selected or respond to the event the user canceled the dialog (clicked the back button)
 */
public class ProjectSelectDialogFragment extends DialogFragment implements OnItemClickListener {
	
	private DialogListener listener;
	private ProjectArrayAdapter adapter;
	private ListView listView;
	
	public void setDialogListener(DialogListener listener){
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.simple_list_layout, container, false);
		listView = (ListView) v.findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		getDialog().getWindow().setTitle("Projects");
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ProjectSiteServices service = new ProjectSiteServices(SageApplication.getInstance().getDaoSession());
		adapter = new ProjectArrayAdapter(activity, service.getProjects());
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(listener != null) listener.onDialogCancel();
		super.onCancel(dialog);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
		public void onDialogClick(Project project);
		/**
		 * raised when the user canceled the dialog (clicked back button)
		 */
		public void onDialogCancel();
	}

}
