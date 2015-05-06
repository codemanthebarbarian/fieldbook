package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;

import com.amecfw.sage.model.EqualityComparatorOf;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.ui.ElementsMultiSelectListAdapter;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.vegetation.rareplant.CategoryElementsListAdapter.ViewModel;
import com.amecfw.sage.util.ActionEvent;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CategoryElementsDialogListFragment extends DialogFragment implements ActionEvent.Listener {
	
	private static final String ARG_ELEMENTS = "sage.vegetation.rareplant.CategoryElementsDialogListFragment.elements";
	private static final String ARG_IS_DIRTY = "sage.vegetation.rareplant.CategoryElementsDialogListFragment.isDirty";

	private CategoryElementsListAdapter adapter;
	private LinearLayout scrollList;
	private ArrayList<CategoryElementsListAdapter.ViewModel> elements;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.categrory_element_dialog_list, container, false);
		scrollList = (LinearLayout) view.findViewById(android.R.id.list);
		if(savedInstanceState != null) initialize(savedInstanceState);
		else initialize();
		return view;
	}
	
	private void initialize(Bundle savedInstanceState){
		elements = savedInstanceState.getParcelableArrayList(ARG_ELEMENTS);
		adapter = new CategoryElementsListAdapter(getActivity(), elements, SageApplication.getInstance().getElementsMode());
		adapter.setEditListener(editListener);
		mIsDirty = savedInstanceState.getBoolean(ARG_IS_DIRTY, true);
		dataSetChanged();
	}
	
	private void initialize(){
		if(elements == null) elements = new ArrayList<CategoryElementsListAdapter.ViewModel>();
		adapter = new CategoryElementsListAdapter(getActivity(), elements, SageApplication.getInstance().getElementsMode());
		adapter.setEditListener(editListener);
		dataSetChanged();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(ARG_ELEMENTS, elements);
		outState.putBoolean(ARG_IS_DIRTY, mIsDirty);
		adapter.setEditListener(null);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDetach() {
		if (adapter != null) adapter.setEditListener(null);
		super.onDetach();
	}

	public void setCategoryElements(ArrayList<CategoryElementsListAdapter.ViewModel> categoryElements){
		elements = categoryElements == null ? elements = new ArrayList<CategoryElementsListAdapter.ViewModel>() : categoryElements;
		if(adapter != null)	adapter.setItems(categoryElements);
		mIsDirty = false;
		dataSetChanged();
	}
	
	private void dataSetChanged(){
		if(adapter == null || scrollList == null) return;
		scrollList.removeAllViews();
		for(int i = 0 ; i < adapter.getCount() ; i++){
			View v = adapter.getView(i, null, scrollList);
			v.setOnLongClickListener(itemLongClickListener);
			v.setId(i);
			if(Integer.lowestOneBit(i) == 1) v.setBackgroundColor(Color.LTGRAY);
			scrollList.addView(v);
		}
	}
	
	private boolean mIsDirty;
	public boolean isDirty() { return mIsDirty; }
	private CategoryElementsListAdapter.OnEditListener editListener = new CategoryElementsListAdapter.OnEditListener(){
		@Override
		public void onEdit(ViewModel viewModel) {
			mIsDirty = true;
		}
	};
	
	/**
	 * Responds to ActionEvent.SAVE, all other events are ignored
	 * Save will call OnExitLister.onExit method with a list of elements and a null ViewState
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getAction()){
		case ActionEvent.SAVE:
			mIsDirty = false;
			if(exitListener != null) exitListener.onExit(elements, null);
			break;
		}		
	}
	
	private OnLongClickListener itemLongClickListener = new OnLongClickListener() {	
		@Override
		public boolean onLongClick(View v) {
			final int viewId = v.getId();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.categorySurvey_deleteElementMessage).setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							doDelete(viewId);
						}
					})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//canceled by user
							}
						});
			builder.create().show();
			return true;
		}
	};
	
	private void doDelete(int position){
		adapter.getItems().remove(position);
		mIsDirty = true;
		dataSetChanged();
	}

	public ArrayList<CategoryElementsListAdapter.ViewModel> getCategoryElements() { return elements; }
	
	private OnExitListener<ArrayList<CategoryElementsListAdapter.ViewModel>> exitListener;
	/**
	 * Sets the OnExitListener for the Fragment. The exit listener is called when responding to the 
	 * ActionEvent.SAVE event
	 * @param exitListener
	 */
	public void setOnExitListener(OnExitListener<ArrayList<CategoryElementsListAdapter.ViewModel>> exitListener){
		this.exitListener = exitListener;
	}
	
	public static class ViewModelComparator implements EqualityComparatorOf<CategoryElementsListAdapter.ViewModel>{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof CategoryElementsListAdapter.ViewModel)
					|| !(objB instanceof CategoryElementsListAdapter.ViewModel)) return false;
			CategoryElementsListAdapter.ViewModel a = (CategoryElementsListAdapter.ViewModel) objA;
			CategoryElementsListAdapter.ViewModel b = (CategoryElementsListAdapter.ViewModel) objB;
			return equals(a, b);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof CategoryElementsListAdapter.ViewModel)) return 0;
			return getHash((CategoryElementsListAdapter.ViewModel) obj);
		}

		@Override
		public boolean equals(
				com.amecfw.sage.vegetation.rareplant.CategoryElementsListAdapter.ViewModel a,
				com.amecfw.sage.vegetation.rareplant.CategoryElementsListAdapter.ViewModel b) {
			return a.getElementId() == b.getElementId();
		}

		@Override
		public int getHash(
				com.amecfw.sage.vegetation.rareplant.CategoryElementsListAdapter.ViewModel obj) {
			return (int) obj.getElementId();
		}
		
	}
}
