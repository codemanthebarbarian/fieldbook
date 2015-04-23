package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.Convert;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ObservationTypeSelectAddFragment extends DialogFragment implements ActionEvent.Listener {

	private static final String KEY_TYPE_NAME = "sage.ui.ObservationTypeSelectAddFragment.typeName";
	
	private EditText typeName;
	private ObservationTypeArrayAdapter adapter;
	private ListView listView;
	private Button save;
	private Button cancel;
	
//	@SuppressLint("InflateParams")
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		View view = getActivity().getLayoutInflater().inflate(R.layout.observation_type_select_add, null);
//		initialize(view);
//		if(savedInstanceState != null) init(savedInstanceState);
//		builder.setTitle("Observation Types");
//		builder.setView(view);
//		return builder.create();
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle("Observation Type");
		View view = inflater.inflate(R.layout.observation_type_select_add, container, false);
		initialize(view);
		if(savedInstanceState != null) init(savedInstanceState);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initializeAdapter(getActivity());
		super.onActivityCreated(savedInstanceState);
	}

	private void initialize(View view){
		typeName = (EditText) view.findViewById(R.id.observationTypeSelectAdd_typeName);
		typeName.addTextChangedListener(editListener);
		listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(itemClickListener);
		save = (Button) view.findViewById(com.amecfw.sage.model.R.id.observationTypeSelectAdd_save);
		save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onSave();			
			} });
		cancel = (Button) view.findViewById(com.amecfw.sage.model.R.id.observationTypeSelectAdd_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onExit(null);
			}
		});
	}
	
	
	
	private void initializeAdapter(Context context){
		adapter = new ObservationTypeArrayAdapter(context
				, new ObservationService(SageApplication.getInstance().getDaoSession()).getObservationTypes());
		listView.setAdapter(adapter);
	}

	private void init(Bundle args){
		if(args == null) return;
		if(args.containsKey(KEY_TYPE_NAME)) typeName.setText(args.getString(KEY_TYPE_NAME));
	}
	
	private TextWatcher editListener = new TextWatcher(){
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {} //do nothing
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {}// do nothing
		@Override
		public void afterTextChanged(Editable s) {
			if(typeName.getText() == null || typeName.getText().length() < 1) adapter.reset();
			if(typeName.getText() != null) adapter.getFilter().filter(typeName.getText());;
		}		
	};

	/**
	 * responds to ActionEvent.SAVE only
	 * will call OnExitListener with a created observationtype from the edittext field
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getAction()){
		case ActionEvent.SAVE:
			onSave();
			break;
		}		
	}
	
	private void onSave(){
		String name = Convert.toStringOrNull(typeName);
		ObservationType result;
		if(name == null) result = null;
		else{
			result = new ObservationType();
			result.setName(name);
		}
		onExit(result);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ObservationType observationType = (ObservationType) listView.getItemAtPosition(position);
			onExit(observationType);
		}		
	};
	public void setOnItemClickListerner(OnItemClickListener listener){
		if(listener != null) itemClickListener = listener;
	}
	
	private void onExit(ObservationType observationType){
		if(listener != null) listener.onExit(observationType);
	}
	
	private OnExitListener listener;
	public void setOnExitListener(OnExitListener listener){
		this.listener = listener;
	}
	public interface OnExitListener{
		public void onExit(ObservationType observationType);
	}
	
}
