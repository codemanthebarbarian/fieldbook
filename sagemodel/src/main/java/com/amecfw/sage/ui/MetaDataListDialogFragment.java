package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.amecfw.sage.proxy.ViewModelBaseEquatable;
import com.amecfw.sage.util.ActionEvent;

public class MetaDataListDialogFragment extends DialogFragment implements ActionEvent.Listener {
	/** set the edit mode, value must be from MetaDataListAdapter.VIEW_MODE values */
	public static final String ARG_EDIT_MODE = "com.amecfw.sage.ui.MetaDataListDialogFragment.editMode";
	/** boolean value if to show a blank item for creating a new meta element, valid if VIEW_MODE_EDIT */
	public static final String ARG_ADD_BLANK = "com.amecfw.sage.ui.MetaDataListDialogFragment.addNew";
	
	private static final String ARG_IS_DIRTY = "com.amecfw.sage.ui.MetaDataListDialogFragment.isDirty";
	private static final String ARG_META_ELEMENTS = "com.amecfw.sage.ui.MetaDataListDialogFragment.metaElements";

	private ArrayList<ViewModel> viewModels;
	private LinearLayout listView;
	private MetaDataListAdapter adapter;
	private int editMode;
	private boolean addNew;
	private boolean isDirty;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.simple_scrollview_list, container, false);
		listView = (LinearLayout) view.findViewById(android.R.id.list);
		if(savedInstanceState != null) initialize(savedInstanceState);
		else initialize();
		view.setOnLongClickListener(onLongClickListener);
		return view;
	}

	private void initialize(){
		Bundle args = getArguments();
		if(args != null){
			editMode = args.getInt(ARG_EDIT_MODE, MetaDataListAdapter.VIEW_MODE_READ_ONLY);
			addNew = args.getBoolean(ARG_ADD_BLANK, false);
		}else{
			editMode = MetaDataListAdapter.VIEW_MODE_READ_ONLY;
			addNew = false;
		}
		if(viewModels == null) viewModels = new ArrayList<ViewModel>();
		if(adapter == null) adapter = new MetaDataListAdapter(getActivity(), viewModels, editMode, addNew);
		adapter.registerDataSetObserver(dataSetObserver);
		adapter.notifyDataSetChanged();
		isDirty = false;
	}

	private void initialize(Bundle savedInstanceState){
		editMode = savedInstanceState.getInt(ARG_EDIT_MODE, MetaDataListAdapter.VIEW_MODE_READ_ONLY);
		addNew = savedInstanceState.getBoolean(ARG_ADD_BLANK, false);
		isDirty = savedInstanceState.getBoolean(ARG_IS_DIRTY);
		viewModels = savedInstanceState.getParcelableArrayList(ARG_META_ELEMENTS);
		if(viewModels == null) viewModels = new ArrayList<ViewModel>();
		if(adapter == null) adapter = new MetaDataListAdapter(getActivity(), viewModels, editMode, addNew);
		else adapter.setItems(viewModels);
		adapter.registerDataSetObserver(dataSetObserver);
		adapter.notifyDataSetChanged();
	}

	public List<ViewModel> getMetaElements() {
		return viewModels;
	}

	public void setMetaElements(List<ViewModel> viewModels) {
		if(this.viewModels == null) this.viewModels = new ArrayList<ViewModel>(viewModels);
		else{
			this.viewModels.clear();
			this.viewModels.addAll(viewModels);
		}
		if(adapter != null) adapter.setItems(this.viewModels);
		isDirty = false;
	}
	
	private DataSetObserver dataSetObserver = new DataSetObserver(){

		@Override
		public void onChanged() { 
			if(adapter == null || listView == null) return;
			listView.removeAllViews();
			for(int i = 0 ; i < adapter.getCount() ; i++){
				View v = adapter.getView(i, null, listView);
				if(Integer.lowestOneBit(i)  == 1) v.setBackgroundColor(Color.LTGRAY);
				listView.addView(v);
			}
		}
		@Override
		public void onInvalidated() {
			if(listView == null) return;
			listView.removeAllViews();
		}
	};
	
	public boolean isDirty(){ return isDirty; }

	public int getEditMode() { return editMode; }
	public void setEditMode(int editMode) { 
		if(editMode == MetaDataListAdapter.VIEW_MODE_EDIT 
				|| editMode == MetaDataListAdapter.VIEW_MODE_EDIT_VALUE
				|| editMode == MetaDataListAdapter.VIEW_MODE_READ_ONLY)
			this.editMode = editMode;
		//TODO: set the adpater and refresh the view
	}

	public boolean isAddNew() {	return addNew; }
	public void setAddNew(boolean addNew) {
		if(editMode == MetaDataListAdapter.VIEW_MODE_EDIT)
			this.addNew = addNew;
		//TODO: set the adpater and refresh the view
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(adapter != null) adapter.unregisterDataSetObserver(dataSetObserver);
		outState.putInt(ARG_EDIT_MODE, editMode);
		outState.putBoolean(ARG_ADD_BLANK, addNew);
		outState.putBoolean(ARG_IS_DIRTY, isDirty);
		outState.putParcelableArrayList(ARG_META_ELEMENTS, viewModels);
		super.onSaveInstanceState(outState);
	}
	
	public static final String ARG_ACTION_EVENT_ELEMENT_INDEX = "com.amecfw.sage.ui.MetaDataListDialogFragment.elementIndex";
	public static final int COMMAND_LONG_PRESS = 1;
	private OnLongClickListener onLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			if(actionListener != null){
				//Notify the listener of the event 
				int index = (int) v.getTag(com.amecfw.sage.model.R.id.sage_tag_list_position);
				Bundle args = new Bundle();
				args.putInt(ARG_ACTION_EVENT_ELEMENT_INDEX, index);
				args.putInt(ActionEvent.ARG_COMMAND, COMMAND_LONG_PRESS);
				actionListener.actionPerformed(ActionEvent.getActionDoCommand(args));
				return true;
			}
			return false;
		}
	};
	
	public ActionEvent.Listener actionListener;
	public void setActionEventListene(ActionEvent.Listener listener){
		actionListener = listener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getAction()){
		case ActionEvent.SAVE:
			
			break;
		}		
	}
	
	public static class ViewModel extends ViewModelBaseEquatable{
		public String name;
		public String value;
		public String rowGuid;
		
		public ViewModel(){}
		
		public ViewModel(Parcel in){
			name = in.readString();
			value = in.readString();
			rowGuid = in.readString();
		}
		
		public static final Parcelable.Creator<ViewModel> CREATOR = 
				new Parcelable.Creator<ViewModel>() {
			@Override
			public ViewModel createFromParcel(Parcel in) {return new ViewModel(in); }
			@Override
			public ViewModel[] newArray(int size) {return new ViewModel[size]; }
				};

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);
			dest.writeString(value);
			dest.writeString(rowGuid);
		}
	}
	
}
