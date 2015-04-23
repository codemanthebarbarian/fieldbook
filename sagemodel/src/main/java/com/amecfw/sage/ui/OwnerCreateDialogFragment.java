package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;
import com.amecfw.sage.model.Owner;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

public class OwnerCreateDialogFragment extends DialogFragment implements ExpandableListView.OnChildClickListener {
	
	public static final String ARG_TITLE = "OwnerCreateDialogFragment.title";
	public static final String ARG_OWNERS = "OwnerCreateDialogFragment.owners";
	
	private String title;
	private ExpandableListView listView;
	private OwnerCreateExpandableAdapter adapter;
	private List<Owner> owners;

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(android.R.layout.expandable_list_content, null);
		listView = (ExpandableListView) view.findViewById(android.R.id.list);
		listView.setOnChildClickListener(this);
		if(savedInstanceState != null) init(savedInstanceState);
		else init(getArguments());
		builder.setTitle(title);
		builder.setView(view);
		return builder.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(owners != null){
			adapter = new OwnerCreateExpandableAdapter(getActivity(), owners);
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
		super.onActivityCreated(savedInstanceState);
	}

	private void init(Bundle bundle){
		if(bundle == null) title = getString(com.amecfw.sage.model.R.string.owenerDialogFragment_title);
		else
		 title = bundle.getString(ARG_TITLE, getString(com.amecfw.sage.model.R.string.owenerDialogFragment_title));
		if(bundle != null && bundle.containsKey(ARG_OWNERS)) owners = bundle.getParcelableArrayList(ARG_OWNERS);
	}	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_TITLE, title);
		outState.putParcelableArrayList(ARG_OWNERS, new ArrayList<Owner>(owners));
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Owner owner = (Owner) adapter.getChild(groupPosition, childPosition);
		if(listener != null) listener.onOwnerSelected(owner);
		this.dismiss();
		return true;
	}

	public void setOwners(List<Owner> owners){
		this.owners = owners;
		if(adapter != null)  { 
			adapter.setOwners(owners);
			adapter.notifyDataSetChanged();
		}
	}
	
	private OnOwnerSelectedListener listener;
	public void setListener(OnOwnerSelectedListener listener){
		this.listener = listener;
	}

	public interface OnOwnerSelectedListener{
		/**
		 * 
		 * @param owner the selected owner
		 */
		public void onOwnerSelected(Owner owner);
	}
}
