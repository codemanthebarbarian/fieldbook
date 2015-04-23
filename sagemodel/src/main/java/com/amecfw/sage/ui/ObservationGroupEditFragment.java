package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.OwnerService;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.ErrorHandler;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.util.ActionEvent;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ObservationGroupEditFragment extends Fragment implements ActionEvent.Listener {

	public static final String ARG_VIEWSTATE = GroupObservationManagement.EXTRA_VIEWSTATE;
	public static final String ARG_VIEW_MODEL = "com.amecfw.sage.ui.ObservationGroupCreateFragment.viewmodel";
	
	private ViewState viewState;
	
	private EditText name;
	private TextView ownerName;
	private TextView ownerType;
	private ImageButton selectOwner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.observation_group_edit, container, false);
		initialize(view);
		if(savedInstanceState == null) initialize(getArguments());
		else initialize(savedInstanceState);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(ARG_VIEWSTATE, viewState);
		outState.putParcelable(ARG_VIEW_MODEL, getViewModel());
		super.onSaveInstanceState(outState);
	}
	
	private void initialize(View view){
		ownerName = (TextView) view.findViewById(R.id.observationGroupEdit_lblOwner);
		ownerType = (TextView) view.findViewById(R.id.observationGroupEdit_lblOwnerType);
		name = (EditText) view.findViewById(R.id.observationGroupEdit_txtName);
		selectOwner = (ImageButton) view.findViewById(R.id.observationGroupEdit_selectOwner);
		selectOwner.setOnClickListener(selectOwnerOnClickListener);
	}
	
	private void initialize(Bundle bundle){
		ViewModel vm = null;
		if(bundle != null){
			viewState = bundle.getParcelable(ARG_VIEWSTATE);
			vm = bundle.getParcelable(ARG_VIEW_MODEL);
		}
		if(viewState == null) viewState = vm == null ? ViewState.getViewStateAdd() : ViewState.getViewStateEdit();
		setViewModel(vm);
	}
	
	private void initializationError(String message) {
		Log.e("sage.ui.GroupObservationsList", message);
		if(getActivity() instanceof ErrorHandler) ((ErrorHandler)getActivity()).onError(message, null, ObservationGroupEditFragment.class);
	}
	
	public ViewModel getViewModel(){
		ViewModel viewModel = new ViewModel();
		if(name != null) viewModel.setName(name.getText().toString());
		if(ownerName != null) viewModel.setOwnerName(ownerName.getText().toString());
		if(ownerType != null) viewModel.setOwnerType(ownerType.getText().toString());
		return viewModel;
	}
	
	public void setViewModel(ViewModel viewModel){
		if(viewModel == null) return;
		if(name != null && viewModel.getName() != null) name.setText(viewModel.getName());
		if(ownerName != null && viewModel.getOwnerName() != null) ownerName.setText(viewModel.getOwnerName());
		if(ownerType != null && viewModel.getOwnerType() != null) ownerType.setText(viewModel.getOwnerType());
		
	}
	
	/**
	 * the method to respond to action events. Will respond to SAVE, DISABLE, and ENABLE ActionEvents
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) return;
		switch(e.getAction()){
		case ActionEvent.SAVE:
			onSave();
			break;
		case ActionEvent.DISABLE:
			onDisable();
			break;
		case ActionEvent.ENABLE:
			onEnable();
			break;
		case ActionEvent.DO_COMMAND:
			onDoCommand(e);
			break;
		}
	}
	/** returns the viewModal and clears the form to add another observation group 
	 * with the same owner  */
	public static final int COMMAND_SAVE_CREATE = 1;
	private void onDoCommand(ActionEvent e){
		Bundle b = e.getArgs();
		if(b == null) return;
		int command = b.getInt(ActionEvent.ARG_COMMAND);
		switch (command){
		case COMMAND_SAVE_CREATE:
			onSave();
			ViewModel vm = new ViewModel();
			vm.setOwnerName(Convert.toStringOrNull(ownerName));
			vm.setOwnerType(Convert.toStringOrNull(ownerType));
			setViewModel(vm);
			break;
		}
	}
	
	private void onSave(){
		if(onExitListener != null) onExitListener.onExit(getViewModel(), viewState);
	}
	
	private void onDisable(){
		if(name != null) name.setEnabled(false);
	}
	
	private void onEnable(){
		if(name != null) name.setEnabled(true);
	}

	private OnExitListener onExitListener;
	public void setOnExitListener(OnExitListener listener){
		onExitListener = listener;
	}
	
	private SelectOwnerOnclickListener selectOwnerOnClickListener = new SelectOwnerOnclickListener();
	
	private class SelectOwnerOnclickListener implements OnClickListener, OwnerCreateDialogFragment.OnOwnerSelectedListener{
		@Override
		public void onClick(View v) {
			OwnerCreateDialogFragment fragment = new OwnerCreateDialogFragment();
			fragment.setListener(this);
			fragment.setOwners(new OwnerService(SageApplication.getInstance().getDaoSession()).getPotentialOwners());
			fragment.show(getFragmentManager(), null);
		}
		@Override
		public void onOwnerSelected(Owner owner) {
			if(owner == null) return;
			ownerName.setText(owner.getName());
			ownerType.setText(owner.getType());
		}
		
	}
	
	public interface OnExitListener {
		public void onExit(ViewModel viewModel, ViewState viewState);
	}
	
	public static class ViewModel implements com.amecfw.sage.proxy.ViewModel{
		
		private String name;
		private String ownerName;
		private String ownerType;
		
		public ViewModel(){};
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOwnerName() {
			return ownerName;
		}

		public void setOwnerName(String ownerName) {
			this.ownerName = ownerName;
		}

		public String getOwnerType() {
			return ownerType;
		}

		public void setOwnerType(String ownerType) {
			this.ownerType = ownerType;
		}
		
		public static final Parcelable.Creator<ViewModel> CREATOR = 
				new Parcelable.Creator<ObservationGroupEditFragment.ViewModel>() {
					@Override
					public ViewModel createFromParcel(Parcel source) { return new ViewModel(source); }
					@Override
					public ViewModel[] newArray(int size) { return new ViewModel[size];}
				};
		
		public ViewModel(Parcel in){
			name = in.readString();
			ownerName = in.readString();
			ownerType = in.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);	
			dest.writeString(ownerName);
			dest.writeString(ownerType);
		}
		
	}
	
}
