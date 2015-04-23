package com.amecfw.sage.ui;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.text.format.DateFormat;

public class DateTimePicker {
	
	/** The key for the integer representing the year selected in the dialog */
	public static final String KEY_YEAR = "com.amecfw.sage.ui.DateTimePicker.year";
	/** The key for the integer representing the month selected in the dialog */
	public static final String KEY_MONTH = "com.amecfw.sage.ui.DateTimePicker.month";
	/** The key for the integer representing the day selected in the dialog */
	public static final String KEY_DAY = "com.amecfw.sage.ui.DateTimePicker.day";
	/** The key for the integer representing the hour selected in the dialog */
	public static final String KEY_HOUR = "com.amecfw.sage.ui.DateTimePicker.hour";
	/** The key for the integer representing the minute selected in the dialog */
	public static final String KEY_MINUTE = "com.amecfw.sage.ui.DateTimePicker.minute";
	/** The key for the integer value for the Theme to display */
	public static final String KEY_THEME = "com.amecfw.sage.ui.DateTimePicker.theme";
	
	
	public interface OnTimeSelectedListener {
		/**
		 * 
		 * @param bundle the bundle containing the result of the TimePIcker
		 */
		public void onTimeSelected(Bundle bundle);
	}
	
	public static class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {		
		
		private OnTimeSelectedListener listener;
		
		/**
		 * Set the OnTimeSelectedListner to retrieve the result from the TimePicker.
		 * IF THIS IS NOT SET: the calling activity will be set to the listener and must implement OnTimeSelectedListener
		 * or a ClassCastException will be thrown
		 * @param listener
		 * @throws ClassCastException if listener is null and calling activity does not implement OnTimeSelectedListener
		 */
		public void setOnTimeSelectedListener(OnTimeSelectedListener listener){
			this.listener = listener;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour; int minute;
			Bundle args = getArguments();
			if(args != null){
				// Use the bundle as the default values for the picker
		        hour = args.getInt(KEY_HOUR);
		        minute = args.getInt(KEY_MINUTE);
			}else{
				// Use the current time as the default values for the picker
		        final Calendar c = Calendar.getInstance();
		        hour = c.get(Calendar.HOUR_OF_DAY);
		        minute = c.get(Calendar.MINUTE);
			}
	        // Create a new instance of TimePickerDialog and return it
	        return new TimePickerDialog(getActivity(), this, hour, minute,
	                DateFormat.is24HourFormat(getActivity()));
		}

		@Override
		public void onTimeSet(android.widget.TimePicker view, int hourOfDay,
				int minute) {
			Bundle args = new Bundle();
			args.putInt(KEY_HOUR, hourOfDay);
			args.putInt(KEY_MINUTE, minute);
			if(listener == null){
				listener = (OnTimeSelectedListener) getActivity();
			}
			listener.onTimeSelected(args);
		}
	}
	
	public interface OnDateSelectedListener {
		/**
		 * 
		 * @param bundle containing the results of the DatePicker
		 */
		public void onDateSelected(Bundle bundle);
	}
	
	public static class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{

		private OnDateSelectedListener listener;
		
		public void setOnDateSelectedListener(OnDateSelectedListener listener){
			this.listener = listener;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int year; int month; int day;
			Bundle args = getArguments();
			if (args != null){
				year = args.getInt(KEY_YEAR);
				month = args.getInt(KEY_MONTH);
				day = args.getInt(KEY_DAY);
			}else{
				// Use the current time as the default values for the picker
		        final Calendar c = Calendar.getInstance();
		        year = c.get(Calendar.YEAR);
		        month = c.get(Calendar.MONTH);
		        day = c.get(Calendar.DAY_OF_MONTH);
			}	        
	        return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(android.widget.DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
			Bundle args = new Bundle();
			args.putInt(KEY_YEAR, year);
			args.putInt(KEY_MONTH, monthOfYear);
			args.putInt(KEY_DAY, dayOfMonth);
			if(listener == null){
				listener = (OnDateSelectedListener) getActivity();
			}
			listener.onDateSelected(args);
		}
		
	}
}
