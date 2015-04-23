package com.amecfw.sage.util;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class specifying an action to be carried out by an object
 */
public class ActionEvent implements Parcelable {
	
	public static final int NULL = 0;
	public static final int EXIT = 1;
	public static final int SAVE = 2;
	public static final int CANCEL = 3;
	public static final int DISABLE = 4;
	public static final int ENABLE = 5;
	public static final int DO_COMMAND = 6;
	public static final String ARG_COMMAND = "sage.ActionEvent.command";
	
	int action;
	Bundle args;
	
	private ActionEvent(int action, Bundle args){
		this.action = action;
		this.args = args;
	}
	
	/**
	 * Gets the action code
	 * @return
	 */
	public int getAction(){ return action; }
	/**
	 * gets any supplied arguments provided with the action
	 * @return args a bundle of any arguments provided with the command or null if no arguments
	 */
	public Bundle getArgs() { return args; }
	
	/** gets a NULL action event */
	public static ActionEvent getActionNull(Bundle args) { return new ActionEvent(NULL, args); }
	/** gets an EXIT action event */
	public static ActionEvent getActionExit(Bundle args) { return new ActionEvent(EXIT, args); }
	/** gets a SAVE action event */
	public static ActionEvent getActionSave(Bundle args) { return new ActionEvent(SAVE, args); }
	/** gets a CANCEL action event */
	public static ActionEvent getActionCancel(Bundle args) { return new ActionEvent(CANCEL, args); }
	/** gets a DISABLE action event */
	public static ActionEvent getActionDisable(Bundle args) { return new ActionEvent(DISABLE, args); }
	/** gets a ENABLE action event */
	public static ActionEvent getActionEnable(Bundle args) { return new ActionEvent(ENABLE, args); }
	/** gets a DO_COMMAND action event */
	public static ActionEvent getActionDoCommand(Bundle args) { return new ActionEvent(DO_COMMAND, args); }
	
	
	public static Parcelable.Creator<ActionEvent> CREATOR = new Parcelable.Creator<ActionEvent>() {
		@Override
		public ActionEvent createFromParcel(Parcel source) { return new ActionEvent(source); }
		@Override
		public ActionEvent[] newArray(int size) { return new ActionEvent[size]; }
	};
	
	public ActionEvent(Parcel in){
		action = in.readInt();
		args = in.readBundle();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(action);	
		dest.writeBundle(args);
	}
	
	public interface Listener {
		public void actionPerformed(ActionEvent e);
	}

}
