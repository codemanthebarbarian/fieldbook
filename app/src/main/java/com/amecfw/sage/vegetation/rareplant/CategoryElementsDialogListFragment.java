package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;

import com.amecfw.sage.model.EqualityComparatorOf;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.ui.PhotoActivity;
import com.amecfw.sage.ui.PhotoHorizontalListFragment;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.rareplant.CategoryElementsListAdapter.ViewModel;
import com.amecfw.sage.util.ActionEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
		adapter.setPhotoActionListener(this);
		mIsDirty = savedInstanceState.getBoolean(ARG_IS_DIRTY, true);
		dataSetChanged();
	}
	
	private void initialize(){
		if(elements == null) elements = new ArrayList<>();
		adapter = new CategoryElementsListAdapter(getActivity(), elements, SageApplication.getInstance().getElementsMode());
		adapter.setEditListener(editListener);
		adapter.setPhotoActionListener(this);
		dataSetChanged();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(ARG_ELEMENTS, elements);
		outState.putBoolean(ARG_IS_DIRTY, mIsDirty);
		//adapter.setEditListener(null);
		//adapter.setPhotoActionListener(null);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDetach() {
		if (adapter != null) adapter.setEditListener(null);
		super.onDetach();
	}

	public void setCategoryElements(ArrayList<ViewModel> categoryElements){
		elements = categoryElements == null ? elements = new ArrayList<>() : categoryElements;
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
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getAction()){
		case ActionEvent.SAVE:
			mIsDirty = false;
			if(exitListener != null) exitListener.onExit(elements, null);
			break;
			case ActionEvent.DO_COMMAND:
				int command = e.getArgs().getInt(ActionEvent.ARG_COMMAND, -1);
				if(command == PhotoService.COMMAND_TAKE_PHOTO) takePhoto(e.getArgs());
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

	public ArrayList<ViewModel> getCategoryElements() { return elements; }
	
	private OnExitListener<ArrayList<ViewModel>> exitListener;
	/**
	 * Sets the OnExitListener for the Fragment. The exit listener is called when responding to the 
	 * ActionEvent.SAVE event
	 * @param exitListener
	 */
	public void setOnExitListener(OnExitListener<ArrayList<ViewModel>> exitListener){
		this.exitListener = exitListener;
	}

	///////////////////////////////////////////////////////////////////////////
	// PHOTO

	private int photoListPosition = -1;
	private ActionEvent.Listener photoProxyActionListener;
	public static final String ARG_VIEW_MODEL = "com.amedfw.sage.vegetation.rareplant.CategoryElementsDialogListFragment.viewModel";

	/**
	 * Set an ActionEvent.Listener to retrieve a created PhotoProxy. Create the ActionEvent using
	 * PhotoService.addProxy
	 * Will add the additional argument of the ViewModel the photo is associated with.
	 * @param listener
	 */
	public void setPhotoProxyActionListener(ActionEvent.Listener listener){ photoProxyActionListener = listener; }

	private void takePhoto(Bundle args){
		photoListPosition = args.getInt(SageApplication.KEY_POSITION, -1);
		if(photoListPosition < 0) return;
		PhotoProxy proxy = PhotoService.createProxy(null);
		SageApplication.getInstance().setItem(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY, proxy);
		Intent intent = new Intent(getActivity(), PhotoActivity.class);
		intent.putExtra(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY, PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY);
		intent.putExtra(PhotoActivity.ARG_VIEW_STATE, ViewState.getViewStateAdd());
		startActivityForResult(intent, PhotoService.PHOTO_RESULT_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == PhotoService.PHOTO_RESULT_CODE){
			if(resultCode == Activity.RESULT_OK){
				PhotoProxy photoProxy = SageApplication.getInstance().removeItem(data.getExtras().getString(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY));
				if(photoProxy != null) sendPhoto(photoProxy);
			}
			if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(getActivity(), "Photo Canceled by User", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void sendPhoto(PhotoProxy proxy){
		if(photoListPosition < 0){
			new PhotoService(SageApplication.getInstance().getDaoSession()).delete(proxy);
			return;
		}
		adapter.addPhoto(photoListPosition, proxy);
		dataSetChanged();
		if(photoProxyActionListener != null){
			Bundle args = new Bundle();
			args.putParcelable(ARG_VIEW_MODEL, adapter.get(photoListPosition));
			photoProxyActionListener.actionPerformed(PhotoService.addProxy(args, proxy));
		}
	}

	// END PHOTO
	///////////////////////////////////////////////////////////////////////////
}
