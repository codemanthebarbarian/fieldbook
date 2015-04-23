package com.amecfw.sage.util;

import android.widget.EditText;

public class Validation {

	public static boolean isNullOrEmpty(EditText editText){
		if(editText == null) return true;
		if(editText.getText() == null) return true;
		return isNullOrEmpty(editText.getText().toString());
	}
	
	public static boolean isNullOrEmpty(String string){
		return (string == null || string.isEmpty());
	}
	
}
