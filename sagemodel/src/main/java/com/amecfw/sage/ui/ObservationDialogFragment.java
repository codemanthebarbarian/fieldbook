package com.amecfw.sage.ui;

import java.util.ArrayList;

import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.ViewState;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ListView;

public class ObservationDialogFragment extends DialogFragment {
	
	/** viewState for  */
	public static final String ARG_VIEWSTATE = "ObservationDialogFragement.ViewState";
	public static final String ARG_GROUP_OBSERVATION = "ObservationDialogFragment.GroupObservation";
	/** key for list of current selection of allowable values, if multiple selection use comma separated values	 */
	public static final String ARG_SELECTED_VALUES = "ObservationDialogFragment.SelectedValues";
	/** Key for boolean value to set for multiple selection of allowable values (true for multiple selection), if not provided false is assumed */
	public static final String ARG_MULTI_SELECT = "ObservationDialogFragment.MulitSelect";
	private ViewState viewState;
	private GroupObservation groupObservation;
	private String[] allowableValues;
	private boolean[] selectedValues;
	private boolean multiSelect;
	private String selectedItems;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		init(getArguments());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if(groupObservation == null) onError(builder);
		else {
			//builder.setTitle(groupObservation.getObservationType().getName());
			builder.setPositiveButton("Save", new SaveClickListener()).setNegativeButton("Cancel", new CancelClickListener());
			if(multiSelect) builder.setMultiChoiceItems(allowableValues, selectedValues, null);
			else builder.setSingleChoiceItems(allowableValues, -1, null);
		}
		return builder.create();
	}


	private void init(Bundle args){
		viewState = args.getParcelable(ARG_VIEWSTATE);
		selectedItems = args.getString(ARG_SELECTED_VALUES);
		multiSelect = args.getBoolean(ARG_MULTI_SELECT, false);
		if(viewState == null) viewState = selectedItems == null ? ViewState.getViewStateAdd() : ViewState.getViewStateEdit();
		groupObservation = args.getParcelable(ARG_GROUP_OBSERVATION);
		allowableValues = ObservationService.parseAllowableValues(groupObservation, ",", true);
	}
	
	/**
	 * Build the dialog to display the error
	 * @param builder
	 */
	private void onError(AlertDialog.Builder builder){
		builder.setMessage("Error loading observation");
		builder.setNegativeButton("OK", new CancelClickListener());
	}
	
	private OnExitListener<String> exitListener;
	public void setExitListener(OnExitListener<String> exitListener){
		this.exitListener = exitListener;
	}
	
	private class SaveClickListener implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			ListView list = ((AlertDialog)dialog).getListView();
			if(list.getCheckedItemCount() == 0) return;
			if(multiSelect){
				SparseBooleanArray items = list.getCheckedItemPositions();
				ArrayList<String> checkedItems = new ArrayList<String>();
				for(int i = 0; i < list.getAdapter().getCount(); i++){
					if(items.get(i)) checkedItems.add((String)list.getAdapter().getItem(i));
				}
				selectedItems = ObservationService.getAllowableValues(checkedItems,',', true);
			}else{
				selectedItems = (String)list.getAdapter().getItem(list.getCheckedItemPosition());
			}
			if(exitListener != null)exitListener.onExit(selectedItems, ViewState.getViewStateAdd());
		}
		
	}
	
	private class CancelClickListener implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
		
	}
		
}
