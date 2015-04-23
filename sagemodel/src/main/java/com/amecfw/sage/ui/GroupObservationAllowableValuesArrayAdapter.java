package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.R;
import com.amecfw.sage.util.Convert;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class GroupObservationAllowableValuesArrayAdapter extends BaseAdapter {

	List<String> allowableValues;
	Context context;
	
	public GroupObservationAllowableValuesArrayAdapter(Context context, List<String> allowableValues){
		this.context = context;
		this.allowableValues = allowableValues;
		if(allowableValues == null) allowableValues = new ArrayList<String>();
		addBlank(); //add an empty item for a last one to add additional elements
	}
	
	private static class ViewHolder{
		EditText allowableValue;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(com.amecfw.sage.model.R.layout.group_observation_allowable_value_listitem, parent, false);
			holder = new ViewHolder();
			holder.allowableValue = (EditText) convertView.findViewById(android.R.id.text1);
			convertView.setTag(R.id.sage_tag_list_viewHolder, holder);
		}else holder = (ViewHolder) convertView.getTag(R.id.sage_tag_list_viewHolder);
		String current = get(position);
		if(current == new String()) holder.allowableValue.setText("test");
		else holder.allowableValue.setText(current);
		holder.allowableValue.setTag(R.id.sage_tag_list_position, position);
		holder.allowableValue.setOnFocusChangeListener(focusChangeListener);
		holder.allowableValue.setOnEditorActionListener(editorActionListener);
		return convertView;
	}
	
	private OnEditorActionListener editorActionListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE ){
				EditText editText = (EditText) v;
				update((int)editText.getTag(R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				return true;
			} else return false;
		}};
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {	
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus){
				EditText editText = (EditText) v;
				update((int)editText.getTag(R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
			}				
		}
	};
	
	private void remove(int position){
		allowableValues.remove(position);
		notifyDataSetChanged();
	}
	
	private void update(int position, String text){
		if(text == null && position < (allowableValues.size() -1)){
			remove(position);
		}else{
			allowableValues.set(position, text);
			if(position == allowableValues.size() -1) addBlank();
			else notifyDataSetChanged();
		}
	}
	
	public void setAllowableValues(List<String> allowableValues){
		this.allowableValues.clear();
		this.allowableValues.addAll(allowableValues);
		notifyDataSetChanged();
	}
	
	private void addBlank(){
		allowableValues.add(new String());
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return allowableValues.size();
	}
	
	public String get(int position){
		return allowableValues.get(position);
	}

	@Override
	public Object getItem(int position) {
		return allowableValues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
}
