package com.amecfw.sage.ui;

import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.ListAdapter;
import com.amecfw.sage.util.OnEditListener;
import com.amecfw.sage.util.ViewState;

public class MetaDataListAdapter extends ListAdapter<MetaDataListDialogFragment.ViewModel> {
	
	public static final int VIEW_MODE_EDIT = 0;
	public static final int VIEW_MODE_EDIT_VALUE = 1;
	public static final int VIEW_MODE_READ_ONLY = 2;
	
	
	private int viewMode;
	private boolean mIsDirty;
	private boolean insert;

	/**
	 * 
	 * @param context
	 * @param items
	 * @param viewMode
	 * @param insert if viewMode is VIEW_MODE_EDIT, set to true to maintain a blank element at the end of the list.
	 */
	public MetaDataListAdapter(Context context, List<MetaDataListDialogFragment.ViewModel> items, int viewMode, boolean insert) {
		super(context, items);
		this.viewMode = (viewMode < 0 ||viewMode > 2) ? VIEW_MODE_READ_ONLY : viewMode;
		this.insert = viewMode == VIEW_MODE_EDIT ? insert : false;
		mIsDirty = false;
	}
	
	public boolean isDirty(){
		return mIsDirty;
	}
	
	private static class ViewHolderFullEdit{
		public EditText metaName;
		public EditText metaValue;
	}
	
	private static class ViewHolderValEdit{
		public TextView metaName;
		public EditText metaValue;
	}
	
	private static class ViewHolderReadOnly{
		public TextView metaName;
		public TextView metaValue;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (viewMode){
		case VIEW_MODE_EDIT:
			return getViewEdit(position, convertView, parent);
		case VIEW_MODE_EDIT_VALUE:
			return getViewEditValue(position, convertView, parent);
		case VIEW_MODE_READ_ONLY:
			return getViewReadOnly(position, convertView, parent);
		}
		return null;
	}
	
	public View getViewEdit(int position, View convertView, ViewGroup parent){
		ViewHolderFullEdit holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(com.amecfw.sage.model.R.layout.simple_editable_list_item_2, parent, false);
			holder = new ViewHolderFullEdit();
			holder.metaName = (EditText) convertView.findViewById(android.R.id.text1);
			holder.metaName.setOnFocusChangeListener(nameFocusChangeListener);
			holder.metaName.setOnEditorActionListener(nameDoneListener);
			holder.metaValue = (EditText) convertView.findViewById(android.R.id.text2);
			holder.metaValue.setOnFocusChangeListener(valueFocusChangeListener);
			holder.metaValue.setOnEditorActionListener(valueDoneListener);
			convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder, holder);
		} else holder = (ViewHolderFullEdit) convertView.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
		MetaDataListDialogFragment.ViewModel current = items.get(position);
		holder.metaName.setTag(com.amecfw.sage.model.R.id.sage_tag_list_position, position);
		if(current.name != null) holder.metaName.setText(current.name);
		holder.metaValue.setTag(com.amecfw.sage.model.R.id.sage_tag_list_position, position);
		if(current.value != null) holder.metaValue.setText(current.value);
		return convertView;
	}
	
	public View getViewEditValue(int position, View convertView, ViewGroup parent){
		ViewHolderValEdit holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(com.amecfw.sage.model.R.layout.simple_item2edit_list_item_2, parent, false);
			holder = new ViewHolderValEdit();
			holder.metaName = (TextView) convertView.findViewById(android.R.id.text1);
			holder.metaValue = (EditText) convertView.findViewById(android.R.id.text2);
			holder.metaValue.setOnFocusChangeListener(valueFocusChangeListener);
			holder.metaValue.setOnEditorActionListener(valueDoneListener);
			convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder, holder);
		} else holder = (ViewHolderValEdit) convertView.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
		MetaDataListDialogFragment.ViewModel current = items.get(position);
		holder.metaName.setText(current.name);
		holder.metaValue.setTag(com.amecfw.sage.model.R.id.sage_tag_list_position, position);
		if(current.value != null) holder.metaValue.setText(current.value);
		return convertView;
	}
	
	public View getViewReadOnly(int position, View convertView, ViewGroup parent){
		ViewHolderReadOnly holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
			holder = new ViewHolderReadOnly();
			holder.metaName = (TextView) convertView.findViewById(android.R.id.text1);
			holder.metaValue = (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder, holder);
		} else holder = (ViewHolderReadOnly) convertView.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
		MetaDataListDialogFragment.ViewModel current = items.get(position);
		holder.metaName.setText(current.name);
		holder.metaValue.setText(current.value);
		return convertView;
	}
	
	private View currentFocus;
	private OnFocusChangeListener nameFocusChangeListener = new OnFocusChangeListener() {	
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				currentFocus = v;
			}else{
				EditText editText = (EditText) v;
				updateName((int)editText.getTag(com.amecfw.sage.model.R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				currentFocus = null;
			}		
		}
	};
	
	private OnEditorActionListener nameDoneListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE){
				EditText editText = (EditText)v;
				updateName((int)editText.getTag( com.amecfw.sage.model.R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				return true;
			} else return false;
		}		
	};	
	
	private void updateName(int index, String name){
		MetaDataListDialogFragment.ViewModel element = items.get(index);
		element.name = name;
		mIsDirty = true;
		onEdit();
	}
	
	private OnFocusChangeListener valueFocusChangeListener = new OnFocusChangeListener() {	
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				currentFocus = v;
			}else{
				EditText editText = (EditText) v;
				updateValue((int)editText.getTag(com.amecfw.sage.model.R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				currentFocus = null;
			}		
		}
	};
	
	private OnEditorActionListener valueDoneListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT){
				EditText editText = (EditText)v;
				updateValue((int)editText.getTag( com.amecfw.sage.model.R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				return true;
			} else return false;
		}		
	};	
	
	private void updateValue(int index, String value){
		MetaDataListDialogFragment.ViewModel element = items.get(index);
		element.value = value;
		mIsDirty = true;
		onEdit();
	}
	
	private void onEdit() {

		if(editListener != null) editListener.onDirty();
	}
	private OnEditListener<MetaDataListDialogFragment.ViewModel> editListener;
	/**
	 * Set the edit listener
	 * @param listener
	 */
	public void setEditListener(OnEditListener<MetaDataListDialogFragment.ViewModel> listener){ editListener = listener; }
	

}
