package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.DrawableOnTouchListener;
import com.amecfw.sage.util.ViewState;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class GroupObservationEditFragment extends DialogFragment implements ActionEvent.Listener {
	
	public static final String ARG_VIEW_STATE = "sage.GroupObservationCreateFragement.viewState";
	public static final String ARG_VIEW_MODEL = "sage.GroupObservationCreateFragement.viewModel";

	private TextView typeName;
	private ListView listView;
	private GroupObservationAllowableValuesArrayAdapter adapter;
	private ViewState viewState;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(com.amecfw.sage.model.R.layout.group_observation_edit, container, false);
		initialize(view);
		if(savedInstanceState != null) initialize(savedInstanceState);
		else initialize(getArguments());
		listView.setAdapter(adapter);
		return view;
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		outState.putParcelable(ARG_VIEW_MODEL, getViewModel());
		super.onSaveInstanceState(outState);
	}

	private void initialize(View view){
		typeName = (TextView) view.findViewById(com.amecfw.sage.model.R.id.groupObservationEdit_typeName);
		listView = (ListView) view.findViewById(android.R.id.list);
		listView.setItemsCanFocus(true);
		drawableTouchListener = new DrawableOnTouchListener.DrawableRightOnTouchListener();
		drawableTouchListener.setOnClickListener(drawableClickListenter);
		typeName.setOnTouchListener(drawableTouchListener);
	}
	
	private void initialize(Bundle args){
		if(args != null){
			viewState = args.getParcelable(ARG_VIEW_STATE);
			setViewModel((ViewModel)args.getParcelable(ARG_VIEW_MODEL));
		}else setViewModel(null);
		if(viewState == null) viewState = ViewState.getViewStateAdd();
	}
	
	public ViewModel getViewModel(){
		ViewModel viewModel = new ViewModel();
		viewModel.setTypeName(Convert.toStringOrNull(typeName));
		viewModel.setAllowableValues(adapter.allowableValues);
		return viewModel;
	}
	
	private void setViewModel(ViewModel viewModel){
		if(viewModel == null) {
			viewModel = new ViewModel();
			viewModel.setAllowableValues(new ArrayList<String>());
		}
		typeName.setText(viewModel.getTypeName());
		if(adapter == null) adapter = new GroupObservationAllowableValuesArrayAdapter(getActivity(), viewModel.allowableValues);
		else adapter.setAllowableValues(viewModel.allowableValues);
	}
	
	private DrawableOnTouchListener.DrawableRightOnTouchListener drawableTouchListener;
	private static final String TAG_OBSERVATION_TYPE_SELECT_ADD_FRAGMENT = "sage.ui.ObservationTypeSelectAddFragment";
	private OnClickListener drawableClickListenter = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ObservationTypeSelectAddFragment fragment = new ObservationTypeSelectAddFragment();
			fragment.setOnExitListener(typeOnExitListener);
			fragment.show(getFragmentManager(), TAG_OBSERVATION_TYPE_SELECT_ADD_FRAGMENT);
		}		
	};
	
	private ObservationTypeSelectAddFragment.OnExitListener typeOnExitListener = new  ObservationTypeSelectAddFragment.OnExitListener(){
		@Override
		public void onExit(ObservationType observationType) {
			if(observationType != null) typeName.setText(observationType.getName());	
			Fragment fragment = getFragmentManager().findFragmentByTag(TAG_OBSERVATION_TYPE_SELECT_ADD_FRAGMENT);
			if(fragment != null) ((ObservationTypeSelectAddFragment)fragment).dismiss();
		}		
	};
	
	/**
	 * Used to override the default onclick event for the search in the 
	 * drawable for the observation type. The default behavior is to display the 
	 * ObservationTypeSelectAddFragment in a dialog view.
	 * @param listener the listener to override with (if null nothing will happen)
	 */
	public void setDrawableOnClickListener(OnClickListener listener){
		drawableClickListenter = listener;
	}

	@Override
	public void actionPerformed(ActionEvent e){
		switch(e.getAction()){
		case ActionEvent.SAVE:
			onExit(getViewModel());			
			break;
		case ActionEvent.DO_COMMAND:
			break;
		}
	}
	
	private void onExit(ViewModel viewModel){
		if(onExitListener != null) onExitListener.onExit(viewModel, viewState);
	}
	
	private OnExitListener onExitListener;
	
	public void setOnExitListener(OnExitListener listener){
		onExitListener = listener;
	}
	
	public interface OnExitListener{
		public void onExit(ViewModel viewModel, ViewState viewState);
	}
	
	public static class ViewModel implements com.amecfw.sage.proxy.ViewModel {
		private String typeName;
		private List<String> allowableValues;
		
		public ViewModel(){}
		
		public String getTypeName() {
			return typeName;
		}
		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}
		public List<String> getAllowableValues() {
			return allowableValues;
		}
		public void setAllowableValues(List<String> allowableValues) {
			this.allowableValues = allowableValues;
		}
		public static final Parcelable.Creator<ViewModel> CREATOR =
				new Parcelable.Creator<ViewModel>(){
					@Override
					public ViewModel createFromParcel(Parcel source) { return new ViewModel(source); }
					@Override
					public ViewModel[] newArray(int size) { return new ViewModel[size]; }			
		};
		
		public ViewModel(Parcel in){
			this.typeName = in.readString();
			this.allowableValues = new ArrayList<String>();
			in.readStringList(allowableValues);
		}
		
		@Override
		public int describeContents() {	return 0; }
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(typeName);
			dest.writeStringList(allowableValues);
		}
		
	}
}
