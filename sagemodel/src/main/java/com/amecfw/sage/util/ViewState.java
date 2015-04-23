package com.amecfw.sage.util;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ViewState implements Parcelable {
	public static final int VIEW = 0;
	public static final int ADD = 1;
	public static final int EDIT = 2;
	public static final int DELETE = 4;
	
	private List<ViewStateListener> listeners;
	
	private int state;
	
	protected ViewState(int state){
		this.state = state;
	}
	
	public static ViewState getViewStateView(){
		return new ViewState(VIEW);
	}
	
	public static ViewState getViewStateAdd(){
		return new ViewState(ADD);
	}
	
	public static ViewState getViewStateEdit(){
		return new ViewState(EDIT);
	}
	
	public static ViewState getViewStateDelete(){
		return new ViewState(DELETE);
	}
	
	public int setStateAdd(){
		int result = state;
		state = ADD;
		notifyListeners(result);
		return result;
	}
	
	public int setStateView(){
		int result = state;
		state = VIEW;
		notifyListeners(result);
		return result;
	}
	
	public int setStateEdit(){
		int result = state;
		state = EDIT;
		notifyListeners(result);
		return result;
	}
	
	public int setStateDelete(){
		int result = state;
		state = DELETE;
		notifyListeners(result);
		return result;
	}
	
	public int getState(){
		return state;
	}
	
	public void addListener(ViewStateListener listener){
		if(listeners == null) listeners = new ArrayList<ViewStateListener>();
		if(! listeners.contains(listener)) listeners.add(listener);
	}
	
	public void removeListener(ViewStateListener listener){
		if(listeners != null && listeners.contains(listener)) listeners.remove(listener);
	}
	
	private void notifyListeners(int previousState){
		if(previousState == state) return;
		if(listeners != null && listeners.size() > 0){
			for (ViewStateListener listener : listeners) {
				listener.onStateChange(previousState, state);
			}
		}
	}
	
	public interface ViewStateListener{
		public void onStateChange(int previousState, int newState);
	}
	
	//Parcelable methods
	
	public static final Parcelable.Creator<ViewState> CREATOR =
			new Parcelable.Creator<ViewState>() {
		public ViewState createFromParcel(Parcel in) { return new ViewState(in); }
		public ViewState[] newArray(int size) { return new ViewState[size]; }
			};
	
	public ViewState(Parcel source){
		state = source.readInt();
	}
	
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(state);
	}
	
	public int describeContents(){
		return 0;
	}
	
	//END Parcelable methods
}
