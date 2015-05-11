package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class CancelSaveExitDialog extends DialogFragment {
	
	private Listener listener;

	/**
	 * Don't forget to set the listener!!
	 */
	public CancelSaveExitDialog(){	}

	public void setListener(Listener listener){
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.saveExitCancelDialog_title);
		builder.setMessage(R.string.saveExitCancelDialog_message);
		builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) { exit(); }
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) { cancel(); }
		});
		builder.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) { save(); }
		});
		return builder.create();
	}
	
	private void save(){
		if(listener != null) listener.onSave(this);
	}
	
	private void cancel(){
		if(listener != null) listener.onCancel(this);
	}
	
	private void exit(){
		if(listener != null) listener.onExit(this);
	}
	
	public interface Listener{
		public void onCancel(CancelSaveExitDialog dialog);
		public void onSave(CancelSaveExitDialog dialog);
		public void onExit(CancelSaveExitDialog dialog);
	}
	
}
